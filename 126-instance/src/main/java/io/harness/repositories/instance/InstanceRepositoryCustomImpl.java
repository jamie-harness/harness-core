/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.repositories.instance;

import static io.harness.entities.Instance.InstanceKeysAdditional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.entities.Instance;
import io.harness.entities.Instance.InstanceKeys;
import io.harness.models.ActiveServiceInstanceInfo;
import io.harness.models.CountByOrgIdProjectIdAndServiceId;
import io.harness.models.CountByServiceIdAndEnvType;
import io.harness.models.EnvBuildInstanceCount;
import io.harness.models.InstancesByBuildId;
import io.harness.models.constants.InstanceSyncConstants;
import io.harness.mongo.helper.SecondaryMongoTemplateHolder;
import io.harness.ng.core.entities.Project;
import io.harness.service.stats.model.InstanceCountByServiceAndEnv;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Singleton
@OwnedBy(HarnessTeam.DX)
public class InstanceRepositoryCustomImpl implements InstanceRepositoryCustom {
  private final MongoTemplate mongoTemplate;
  private final MongoTemplate secondaryMongoTemplate;

  @Inject
  public InstanceRepositoryCustomImpl(
      MongoTemplate mongoTemplate, SecondaryMongoTemplateHolder secondaryMongoTemplateHolder) {
    this.mongoTemplate = mongoTemplate;
    this.secondaryMongoTemplate = secondaryMongoTemplateHolder.getSecondaryMongoTemplate();
  }

  @Override
  public Instance findAndReplace(Criteria criteria, Instance instance) {
    Query query = new Query(criteria);
    return mongoTemplate.findAndReplace(query, instance, FindAndReplaceOptions.options().returnNew());
  }

  public Instance findAndModify(Criteria criteria, Update update) {
    Query query = new Query(criteria);
    return mongoTemplate.findAndModify(query, update, Instance.class);
  }

  @Override
  public AggregationResults<InstanceCountByServiceAndEnv> getActiveInstancesByServiceAndEnv(
      Project project, long timestamp) {
    GroupOperation groupByEnvId = group(InstanceKeys.serviceIdentifier, InstanceKeys.envIdentifier)
                                      .first(InstanceSyncConstants.ROOT)
                                      .as(InstanceSyncConstants.FIRST_DOCUMENT)
                                      .count()
                                      .as(InstanceSyncConstants.COUNT);
    if (timestamp <= 0) {
      Criteria nonDeletedCriteria = Criteria.where(InstanceKeys.accountIdentifier)
                                        .is(project.getAccountIdentifier())
                                        .and(InstanceKeys.orgIdentifier)
                                        .is(project.getOrgIdentifier())
                                        .and(InstanceKeys.projectIdentifier)
                                        .is(project.getIdentifier())
                                        .and(InstanceKeys.isDeleted)
                                        .is(false);
      MatchOperation match = Aggregation.match(nonDeletedCriteria);
      return secondaryMongoTemplate.aggregate(
          newAggregation(match, groupByEnvId), Instance.class, InstanceCountByServiceAndEnv.class);
    }
    Criteria createdBeforeCriteria = Criteria.where(InstanceKeys.accountIdentifier)
                                         .is(project.getAccountIdentifier())
                                         .and(InstanceKeys.orgIdentifier)
                                         .is(project.getOrgIdentifier())
                                         .and(InstanceKeys.projectIdentifier)
                                         .is(project.getIdentifier())
                                         .and(InstanceKeys.isDeleted)
                                         .is(false)
                                         .and(InstanceKeys.createdAt)
                                         .lte(timestamp);
    Criteria deletedAfterCriteria = Criteria.where(InstanceKeys.accountIdentifier)
                                        .is(project.getAccountIdentifier())
                                        .and(InstanceKeys.orgIdentifier)
                                        .is(project.getOrgIdentifier())
                                        .and(InstanceKeys.projectIdentifier)
                                        .is(project.getIdentifier())
                                        .and(InstanceKeys.isDeleted)
                                        .is(true)
                                        .and(InstanceKeys.createdAt)
                                        .lte(timestamp)
                                        .and(InstanceKeys.deletedAt)
                                        .gte(timestamp);
    MatchOperation match = Aggregation.match(new Criteria().orOperator(createdBeforeCriteria, deletedAfterCriteria));
    return secondaryMongoTemplate.aggregate(
        newAggregation(match, groupByEnvId), Instance.class, InstanceCountByServiceAndEnv.class);
  }

  /*
    Returns instances that are active for all services at a given timestamp for specified accountIdentifier,
    projectIdentifier and orgIdentifier
  */
  @Override
  public List<Instance> getActiveInstances(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, long timestampInMs) {
    Criteria criteria =
        getCriteriaForActiveInstances(accountIdentifier, orgIdentifier, projectIdentifier, timestampInMs);

    Query query = new Query().addCriteria(criteria);
    return secondaryMongoTemplate.find(query, Instance.class);
  }

  @Override
  public List<Instance> getActiveInstancesByInstanceInfo(
      String accountIdentifier, String instanceInfoNamespace, String instanceInfoPodName) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(InstanceKeysAdditional.instanceInfoPodName)
                            .is(instanceInfoPodName)
                            .and(InstanceKeysAdditional.instanceInfoNamespace)
                            .is(instanceInfoNamespace);
    Query query = new Query().addCriteria(criteria).with(Sort.by(Sort.Direction.DESC, InstanceKeys.createdAt));
    return secondaryMongoTemplate.find(query, Instance.class);
  }

  /*
    Returns instances that are active at a given timestamp for specified accountIdentifier, projectIdentifier,
    orgIdentifier and serviceId
  */
  @Override
  public List<Instance> getActiveInstancesByServiceId(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String serviceId, long timestampInMs) {
    Criteria criteria =
        getCriteriaForActiveInstances(accountIdentifier, orgIdentifier, projectIdentifier, timestampInMs)
            .and(InstanceKeys.serviceIdentifier)
            .is(serviceId);
    Query query = new Query().addCriteria(criteria);
    return secondaryMongoTemplate.find(query, Instance.class);
  }

  @Override
  public List<Instance> getActiveInstancesByServiceId(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String serviceId) {
    Criteria criteria = getCriteriaForActiveInstances(accountIdentifier, orgIdentifier, projectIdentifier)
                            .and(InstanceKeys.serviceIdentifier)
                            .is(serviceId);
    Query query = new Query(criteria);
    return secondaryMongoTemplate.find(query, Instance.class);
  }

  /*
    Return instances that are active currently for specified accountIdentifier, projectIdentifier,
    orgIdentifier and infrastructure mapping id
  */
  @Override
  public List<Instance> getActiveInstancesByInfrastructureMappingId(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String infrastructureMappingId) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(InstanceKeys.orgIdentifier)
                            .is(orgIdentifier)
                            .and(InstanceKeys.projectIdentifier)
                            .is(projectIdentifier)
                            .and(InstanceKeys.infrastructureMappingId)
                            .is(infrastructureMappingId)
                            .and(InstanceKeys.isDeleted)
                            .is(false);
    Query query = new Query().addCriteria(criteria);
    return secondaryMongoTemplate.find(query, Instance.class);
  }

  @Override
  public AggregationResults<EnvBuildInstanceCount> getEnvBuildInstanceCountByServiceId(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String serviceId, long timestampInMs) {
    Criteria criteria =
        getCriteriaForActiveInstances(accountIdentifier, orgIdentifier, projectIdentifier, timestampInMs)
            .and(InstanceKeys.serviceIdentifier)
            .is(serviceId);

    MatchOperation matchStage = Aggregation.match(criteria);
    GroupOperation groupEnvId =
        group(InstanceKeys.envIdentifier, InstanceKeys.envName, InstanceSyncConstants.PRIMARY_ARTIFACT_TAG)
            .count()
            .as(InstanceSyncConstants.COUNT);
    return secondaryMongoTemplate.aggregate(
        newAggregation(matchStage, groupEnvId), Instance.class, EnvBuildInstanceCount.class);
  }

  @Override
  public AggregationResults<ActiveServiceInstanceInfo> getActiveServiceInstanceInfo(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String serviceId) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(InstanceKeys.orgIdentifier)
                            .is(orgIdentifier)
                            .and(InstanceKeys.projectIdentifier)
                            .is(projectIdentifier)
                            .and(InstanceKeys.serviceIdentifier)
                            .is(serviceId)
                            .and(InstanceKeysAdditional.instanceInfoClusterIdentifier)
                            .exists(false)
                            .and(InstanceKeys.isDeleted)
                            .is(false);

    MatchOperation matchStage = Aggregation.match(criteria);
    GroupOperation groupEnvId = group(InstanceKeys.infraIdentifier, InstanceKeys.infraName,
        InstanceKeys.lastPipelineExecutionId, InstanceKeys.lastPipelineExecutionName, InstanceKeys.lastDeployedAt,
        InstanceKeys.envIdentifier, InstanceKeys.envName, InstanceSyncConstants.PRIMARY_ARTIFACT_TAG,
        InstanceSyncConstants.PRIMARY_ARTIFACT_DISPLAY_NAME)
                                    .count()
                                    .as(InstanceSyncConstants.COUNT);
    return secondaryMongoTemplate.aggregate(
        newAggregation(matchStage, groupEnvId), Instance.class, ActiveServiceInstanceInfo.class);
  }

  @Override
  public AggregationResults<ActiveServiceInstanceInfo> getActiveServiceGitOpsInstanceInfo(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String serviceId) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountIdentifier)
                            .and(InstanceKeys.orgIdentifier)
                            .is(orgIdentifier)
                            .and(InstanceKeys.projectIdentifier)
                            .is(projectIdentifier)
                            .and(InstanceKeys.serviceIdentifier)
                            .is(serviceId)
                            .and(InstanceKeysAdditional.instanceInfoClusterIdentifier)
                            .exists(true)
                            .and(InstanceKeys.isDeleted)
                            .is(false);

    MatchOperation matchStage = Aggregation.match(criteria);
    GroupOperation groupClusterEnvId =
        group(InstanceKeysAdditional.instanceInfoClusterIdentifier, InstanceKeysAdditional.instanceInfoAgentIdentifier,
            InstanceKeys.lastPipelineExecutionId, InstanceKeys.lastPipelineExecutionName, InstanceKeys.lastDeployedAt,
            InstanceKeys.envIdentifier, InstanceKeys.envName, InstanceSyncConstants.PRIMARY_ARTIFACT_TAG,
            InstanceSyncConstants.PRIMARY_ARTIFACT_DISPLAY_NAME)
            .count()
            .as(InstanceSyncConstants.COUNT);
    return mongoTemplate.aggregate(
        newAggregation(matchStage, groupClusterEnvId), "instanceNG", ActiveServiceInstanceInfo.class);
  }

  /*
    Return instances that are active at a given timestamp for specified accountIdentifier, projectIdentifier,
    orgIdentifier, serviceId, envId and list of buildIds
  */
  @Override
  public AggregationResults<InstancesByBuildId> getActiveInstancesByServiceIdEnvIdAndBuildIds(String accountIdentifier,
      String orgIdentifier, String projectIdentifier, String serviceId, String envId, List<String> buildIds,
      long timestampInMs, int limit) {
    Criteria criteria =
        getCriteriaForActiveInstances(accountIdentifier, orgIdentifier, projectIdentifier, timestampInMs)
            .and(InstanceKeys.envIdentifier)
            .is(envId)
            .and(InstanceKeys.serviceIdentifier)
            .is(serviceId);

    // in case artifact tag is missing
    if (EmptyPredicate.isNotEmpty(buildIds)) {
      criteria = criteria.and(InstanceSyncConstants.PRIMARY_ARTIFACT_TAG).in(buildIds);
    }

    MatchOperation matchStage = Aggregation.match(criteria);
    GroupOperation group = group(InstanceSyncConstants.PRIMARY_ARTIFACT_TAG)
                               .push(InstanceSyncConstants.PRIMARY_ARTIFACT_TAG)
                               .as(InstanceSyncConstants.buildId)
                               .push(Aggregation.ROOT)
                               .as(InstanceSyncConstants.INSTANCES);

    ProjectionOperation projection = Aggregation.project()
                                         .andExpression(InstanceSyncConstants.ID)
                                         .as(InstanceSyncConstants.buildId)
                                         .andExpression(InstanceSyncConstants.INSTANCES)
                                         .slice(limit)
                                         .as(InstanceSyncConstants.INSTANCES);

    return secondaryMongoTemplate.aggregate(
        newAggregation(matchStage, group, projection), Instance.class, InstancesByBuildId.class);
  }

  /*
    Returns breakup of active instances by envType at a given timestamp for specified accountIdentifier,
    projectIdentifier, orgIdentifier and serviceId
  */
  @Override
  public AggregationResults<CountByServiceIdAndEnvType> getActiveServiceInstanceCountBreakdown(String accountIdentifier,
      String orgIdentifier, String projectIdentifier, List<String> serviceId, long timestampInMs) {
    Criteria criteria =
        getCriteriaForActiveInstances(accountIdentifier, orgIdentifier, projectIdentifier, timestampInMs)
            .and(InstanceKeys.serviceIdentifier)
            .in(serviceId);

    MatchOperation matchStage = Aggregation.match(criteria);
    GroupOperation groupEnvId =
        group(InstanceKeys.serviceIdentifier, InstanceKeys.envType).count().as(InstanceSyncConstants.COUNT);

    ProjectionOperation projection =
        Aggregation.project()
            .andExpression(InstanceSyncConstants.ID + "." + InstanceSyncConstants.ENV_TYPE)
            .as(InstanceSyncConstants.ENV_TYPE)
            .andExpression(InstanceSyncConstants.ID + "." + InstanceSyncConstants.SERVICE_ID)
            .as(InstanceSyncConstants.SERVICE_ID)
            .andExpression(InstanceSyncConstants.COUNT)
            .as(InstanceSyncConstants.COUNT);

    return secondaryMongoTemplate.aggregate(
        newAggregation(matchStage, groupEnvId, projection), Instance.class, CountByServiceIdAndEnvType.class);
  }

  /*
    Create criteria to query for all active service instances for given accountIdentifier, orgIdentifier,
    projectIdentifier
  */
  private Criteria getCriteriaForActiveInstances(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, long timestampInMs) {
    Criteria filterNotDeleted = Criteria.where(InstanceKeys.accountIdentifier)
                                    .is(accountIdentifier)
                                    .and(InstanceKeys.orgIdentifier)
                                    .is(orgIdentifier)
                                    .and(InstanceKeys.projectIdentifier)
                                    .is(projectIdentifier)
                                    .and(InstanceKeys.isDeleted)
                                    .is(false)
                                    .and(InstanceKeys.createdAt)
                                    .lte(timestampInMs);

    Criteria filterDeletedAfter = Criteria.where(InstanceKeys.accountIdentifier)
                                      .is(accountIdentifier)
                                      .and(InstanceKeys.orgIdentifier)
                                      .is(orgIdentifier)
                                      .and(InstanceKeys.projectIdentifier)
                                      .is(projectIdentifier)
                                      .and(InstanceKeys.isDeleted)
                                      .is(true)
                                      .and(InstanceKeys.createdAt)
                                      .lte(timestampInMs)
                                      .and(InstanceKeys.deletedAt)
                                      .gte(timestampInMs);

    return new Criteria().orOperator(filterNotDeleted, filterDeletedAfter);
  }

  private Criteria getCriteriaForActiveInstances(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    return Criteria.where(InstanceKeys.accountIdentifier)
        .is(accountIdentifier)
        .and(InstanceKeys.orgIdentifier)
        .is(orgIdentifier)
        .and(InstanceKeys.projectIdentifier)
        .is(projectIdentifier)
        .and(InstanceKeys.isDeleted)
        .is(false);
  }

  @Override
  public Instance findFirstInstance(Criteria criteria) {
    Query query = new Query().addCriteria(criteria);
    return secondaryMongoTemplate.findOne(query, Instance.class);
  }

  @Override
  public void updateInfrastructureMapping(String instanceId, String infrastructureMappingId) {
    Criteria criteria = Criteria.where(InstanceKeys.id).is(instanceId);
    Query query = new Query();
    query.addCriteria(criteria);

    Update update = new Update();
    update.set(InstanceKeys.infrastructureMappingId, infrastructureMappingId);
    mongoTemplate.findAndModify(query, update, Instance.class);
  }

  @Override
  public long countServiceInstancesDeployedInInterval(String accountId, long startTS, long endTS) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountId)
                            .and(InstanceKeys.lastDeployedAt)
                            .gte(startTS)
                            .lte(endTS);
    return secondaryMongoTemplate.count(new Query().addCriteria(criteria), Instance.class);
  }

  @Override
  public long countServiceInstancesDeployedInInterval(
      String accountId, String orgId, String projectId, long startTS, long endTS) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountId)
                            .and(InstanceKeys.orgIdentifier)
                            .is(orgId)
                            .and(InstanceKeys.projectIdentifier)
                            .is(projectId)
                            .and(InstanceKeys.lastDeployedAt)
                            .gte(startTS)
                            .lte(endTS);
    return secondaryMongoTemplate.count(new Query().addCriteria(criteria), Instance.class);
  }

  @Override
  public long countDistinctActiveServicesDeployedInInterval(
      String accountId, String orgId, String projectId, long startTS, long endTS) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountId)
                            .and(InstanceKeys.orgIdentifier)
                            .is(orgId)
                            .and(InstanceKeys.projectIdentifier)
                            .is(projectId)
                            .and(InstanceKeys.lastDeployedAt)
                            .gte(startTS)
                            .lte(endTS);

    return secondaryMongoTemplate
        .findDistinct(new Query(criteria), InstanceKeys.serviceIdentifier, Instance.class, String.class)
        .size();
  }

  @Override
  public long countDistinctActiveServicesDeployedInInterval(String accountId, long startTS, long endTS) {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(accountId)
                            .and(InstanceKeys.lastDeployedAt)
                            .gte(startTS)
                            .lte(endTS);
    MatchOperation matchStage = Aggregation.match(criteria);
    GroupOperation groupByOrgIdProjectIdServiceId =
        group(InstanceKeys.orgIdentifier, InstanceKeys.projectIdentifier, InstanceKeys.serviceIdentifier)
            .count()
            .as(InstanceSyncConstants.COUNT);
    return secondaryMongoTemplate
        .aggregate(newAggregation(matchStage, groupByOrgIdProjectIdServiceId), Instance.class,
            CountByOrgIdProjectIdAndServiceId.class)
        .getMappedResults()
        .size();
  }
}
