/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.repositories.instance;

import static io.harness.rule.OwnerRule.PIYUSH_BHUWALKA;
import static io.harness.rule.OwnerRule.VIKYATH_HAREKAL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.harness.InstancesTestBase;
import io.harness.category.element.UnitTests;
import io.harness.entities.Instance;
import io.harness.entities.Instance.InstanceKeys;
import io.harness.entities.Instance.InstanceKeysAdditional;
import io.harness.models.CountByServiceIdAndEnvType;
import io.harness.models.EnvBuildInstanceCount;
import io.harness.models.InstancesByBuildId;
import io.harness.mongo.helper.SecondaryMongoTemplateHolder;
import io.harness.ng.core.entities.Project;
import io.harness.ng.core.environment.beans.EnvironmentType;
import io.harness.rule.Owner;
import io.harness.service.stats.model.InstanceCountByServiceAndEnv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class InstanceRepositoryCustomImplTest extends InstancesTestBase {
  private final String ACCOUNT_ID = "acc";
  private final String ORGANIZATION_ID = "org";
  private final String PROJECT_ID = "project";
  private final String SERVICE_ID = "service";
  private final String INFRASTRUCTURE_MAPPING_ID = "infra";
  private final String INSTANCE_INFO_PODNAME = "podname";
  private final String INSTANCE_INFO_POD_NAMESPACE = "podnamespace";
  private final String ENVIRONMENT_ID = "envID";
  private final String ENVIRONMENT_NAME = "envName";
  private final String TAG = "tag";
  private final int COUNT = 3;
  private final long TIMESTAMP = 123L;
  private final long START_TIMESTAMP = 124L;
  private final long END_TIMESTAMP = 125L;
  @Mock MongoTemplate mongoTemplate;
  @Mock MongoTemplate secondaryMongoTemplate;
  @Mock SecondaryMongoTemplateHolder secondaryMongoTemplateHolder;
  InstanceRepositoryCustomImpl instanceRepositoryCustom;

  @Before
  public void setup() {
    when(secondaryMongoTemplateHolder.getSecondaryMongoTemplate()).thenReturn(secondaryMongoTemplate);
    instanceRepositoryCustom = new InstanceRepositoryCustomImpl(mongoTemplate, secondaryMongoTemplateHolder);
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void findAndReplaceTest() {
    Criteria criteria = Criteria.where("key");
    Instance instance = Instance.builder().build();
    Query query = new Query(criteria);
    when(mongoTemplate.findAndReplace(eq(query), eq(instance), any(FindAndReplaceOptions.class))).thenReturn(instance);
    assertThat(instanceRepositoryCustom.findAndReplace(criteria, instance)).isEqualTo(instance);
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void findAndModifyTest() {
    Criteria criteria = Criteria.where("key");
    Update update = new Update();
    Instance instance = Instance.builder().build();
    Query query = new Query(criteria);
    when(mongoTemplate.findAndModify(query, update, Instance.class)).thenReturn(instance);
    assertThat(instanceRepositoryCustom.findAndModify(criteria, update)).isEqualTo(instance);
  }

  @Test
  @Owner(developers = VIKYATH_HAREKAL)
  @Category(UnitTests.class)
  public void getActiveInstancesByEnvWithoutTimestamp() {
    Project project = mockProject();
    Instance instance = Instance.builder().instanceKey("abc").build();
    InstanceCountByServiceAndEnv instanceCountByServiceAndEnv = mockInstanceCountByServiceAndEnv(instance);
    AggregationResults<InstanceCountByServiceAndEnv> results =
        new AggregationResults<>(Collections.singletonList(instanceCountByServiceAndEnv), new Document());
    when(secondaryMongoTemplate.aggregate(
             any(Aggregation.class), eq(Instance.class), eq(InstanceCountByServiceAndEnv.class)))
        .thenReturn(results);

    assertThat(instanceRepositoryCustom.getActiveInstancesByServiceAndEnv(project, -1))
        .containsExactlyInAnyOrderElementsOf(results);
  }

  @Test
  @Owner(developers = VIKYATH_HAREKAL)
  @Category(UnitTests.class)
  public void getActiveInstancesByEnv() {
    Project project = mockProject();
    Instance instance1 = Instance.builder().instanceKey("abc").build();
    InstanceCountByServiceAndEnv instanceCountByServiceAndEnv1 = mockInstanceCountByServiceAndEnv(instance1);
    Instance instance2 = Instance.builder().instanceKey("def").build();
    InstanceCountByServiceAndEnv instanceCountByServiceAndEnv2 = mockInstanceCountByServiceAndEnv(instance2);
    AggregationResults<InstanceCountByServiceAndEnv> results = new AggregationResults<>(
        Arrays.asList(instanceCountByServiceAndEnv1, instanceCountByServiceAndEnv2), new Document());
    when(secondaryMongoTemplate.aggregate(
             any(Aggregation.class), eq(Instance.class), eq(InstanceCountByServiceAndEnv.class)))
        .thenReturn(results);

    assertThat(instanceRepositoryCustom.getActiveInstancesByServiceAndEnv(project, TIMESTAMP))
        .containsExactlyInAnyOrderElementsOf(results);
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getActiveInstancesTest() {
    Criteria filterNotDeleted = Criteria.where(InstanceKeys.accountIdentifier)
                                    .is(ACCOUNT_ID)
                                    .and(InstanceKeys.orgIdentifier)
                                    .is(ORGANIZATION_ID)
                                    .and(InstanceKeys.projectIdentifier)
                                    .is(PROJECT_ID)
                                    .and(InstanceKeys.isDeleted)
                                    .is(false)
                                    .and(InstanceKeys.createdAt)
                                    .lte(TIMESTAMP);

    Criteria filterDeletedAfter = Criteria.where(InstanceKeys.accountIdentifier)
                                      .is(ACCOUNT_ID)
                                      .and(InstanceKeys.orgIdentifier)
                                      .is(ORGANIZATION_ID)
                                      .and(InstanceKeys.projectIdentifier)
                                      .is(PROJECT_ID)
                                      .and(InstanceKeys.isDeleted)
                                      .is(true)
                                      .and(InstanceKeys.createdAt)
                                      .lte(TIMESTAMP)
                                      .and(InstanceKeys.deletedAt)
                                      .gte(TIMESTAMP);

    Criteria criteria = new Criteria().orOperator(filterNotDeleted, filterDeletedAfter);
    Instance instance = Instance.builder().build();
    Query query = new Query().addCriteria(criteria);
    when(secondaryMongoTemplate.find(query, Instance.class)).thenReturn(Arrays.asList(instance));
    assertThat(instanceRepositoryCustom.getActiveInstances(ACCOUNT_ID, ORGANIZATION_ID, PROJECT_ID, TIMESTAMP))
        .isEqualTo(Arrays.asList(instance));
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getActiveInstancesByInstanceInfoTest() {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(ACCOUNT_ID)
                            .and(InstanceKeysAdditional.instanceInfoPodName)
                            .is(INSTANCE_INFO_PODNAME)
                            .and(InstanceKeysAdditional.instanceInfoNamespace)
                            .is(INSTANCE_INFO_POD_NAMESPACE);
    Query query = new Query().addCriteria(criteria).with(Sort.by(Sort.Direction.DESC, InstanceKeys.createdAt));
    Instance instance = Instance.builder().build();
    when(secondaryMongoTemplate.find(query, Instance.class)).thenReturn(Arrays.asList(instance));
    assertThat(instanceRepositoryCustom.getActiveInstancesByInstanceInfo(
                   ACCOUNT_ID, INSTANCE_INFO_POD_NAMESPACE, INSTANCE_INFO_PODNAME))
        .isEqualTo(Arrays.asList(instance));
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getActiveInstancesByServiceIdTest() {
    Criteria filterNotDeleted = Criteria.where(InstanceKeys.accountIdentifier)
                                    .is(ACCOUNT_ID)
                                    .and(InstanceKeys.orgIdentifier)
                                    .is(ORGANIZATION_ID)
                                    .and(InstanceKeys.projectIdentifier)
                                    .is(PROJECT_ID)
                                    .and(InstanceKeys.isDeleted)
                                    .is(false)
                                    .and(InstanceKeys.createdAt)
                                    .lte(TIMESTAMP);

    Criteria filterDeletedAfter = Criteria.where(InstanceKeys.accountIdentifier)
                                      .is(ACCOUNT_ID)
                                      .and(InstanceKeys.orgIdentifier)
                                      .is(ORGANIZATION_ID)
                                      .and(InstanceKeys.projectIdentifier)
                                      .is(PROJECT_ID)
                                      .and(InstanceKeys.isDeleted)
                                      .is(true)
                                      .and(InstanceKeys.createdAt)
                                      .lte(TIMESTAMP)
                                      .and(InstanceKeys.deletedAt)
                                      .gte(TIMESTAMP);

    Criteria criteria = new Criteria()
                            .orOperator(filterNotDeleted, filterDeletedAfter)
                            .and(InstanceKeys.serviceIdentifier)
                            .is(SERVICE_ID);
    Query query = new Query().addCriteria(criteria);
    Instance instance = Instance.builder().build();
    when(secondaryMongoTemplate.find(query, Instance.class)).thenReturn(Arrays.asList(instance));
    assertThat(instanceRepositoryCustom.getActiveInstancesByServiceId(
                   ACCOUNT_ID, ORGANIZATION_ID, PROJECT_ID, SERVICE_ID, TIMESTAMP))
        .isEqualTo(Arrays.asList(instance));
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getActiveInstancesByInfrastructureMappingIdTest() {
    Criteria criteria = Criteria.where(InstanceKeys.accountIdentifier)
                            .is(ACCOUNT_ID)
                            .and(InstanceKeys.orgIdentifier)
                            .is(ORGANIZATION_ID)
                            .and(InstanceKeys.projectIdentifier)
                            .is(PROJECT_ID)
                            .and(InstanceKeys.infrastructureMappingId)
                            .is(INFRASTRUCTURE_MAPPING_ID)
                            .and(InstanceKeys.isDeleted)
                            .is(false);
    Query query = new Query().addCriteria(criteria);
    Instance instance = Instance.builder().build();
    when(secondaryMongoTemplate.find(query, Instance.class)).thenReturn(Arrays.asList(instance));
    assertThat(instanceRepositoryCustom.getActiveInstancesByInfrastructureMappingId(
                   ACCOUNT_ID, ORGANIZATION_ID, PROJECT_ID, INFRASTRUCTURE_MAPPING_ID))
        .isEqualTo(Arrays.asList(instance));
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getEnvBuildInstanceCountByServiceIdTest() {
    EnvBuildInstanceCount envBuildInstanceCount =
        new EnvBuildInstanceCount(ENVIRONMENT_ID, ENVIRONMENT_NAME, TAG, COUNT);
    AggregationResults<EnvBuildInstanceCount> aggregationResults =
        new AggregationResults<>(Arrays.asList(envBuildInstanceCount), new Document());
    when(secondaryMongoTemplate.aggregate(any(Aggregation.class), eq(Instance.class), eq(EnvBuildInstanceCount.class)))
        .thenReturn(aggregationResults);
    assertThat(instanceRepositoryCustom.getEnvBuildInstanceCountByServiceId(
                   ACCOUNT_ID, ORGANIZATION_ID, PROJECT_ID, SERVICE_ID, TIMESTAMP))
        .isEqualTo(aggregationResults);
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getActiveInstancesByServiceIdEnvIdAndBuildIdsTest() {
    List<String> buildIds = Arrays.asList("build1", "build2");
    int limit = 5;
    Instance instance = Instance.builder().build();
    InstancesByBuildId instancesByBuildId = new InstancesByBuildId("buildId", Arrays.asList(instance));
    AggregationResults<InstancesByBuildId> aggregationResults =
        new AggregationResults<>(Arrays.asList(instancesByBuildId), new Document());
    when(secondaryMongoTemplate.aggregate(any(Aggregation.class), eq(Instance.class), eq(InstancesByBuildId.class)))
        .thenReturn(aggregationResults);
    assertThat(instanceRepositoryCustom.getActiveInstancesByServiceIdEnvIdAndBuildIds(
                   ACCOUNT_ID, ORGANIZATION_ID, PROJECT_ID, SERVICE_ID, ENVIRONMENT_ID, buildIds, TIMESTAMP, limit))
        .isEqualTo(aggregationResults);
  }

  @Test
  @Owner(developers = PIYUSH_BHUWALKA)
  @Category(UnitTests.class)
  public void getActiveServiceInstanceCountBreakdownTest() {
    CountByServiceIdAndEnvType countByServiceIdAndEnvType =
        new CountByServiceIdAndEnvType(SERVICE_ID, EnvironmentType.Production, COUNT);
    AggregationResults<CountByServiceIdAndEnvType> aggregationResults =
        new AggregationResults<>(Arrays.asList(countByServiceIdAndEnvType), new Document());
    when(secondaryMongoTemplate.aggregate(
             any(Aggregation.class), eq(Instance.class), eq(CountByServiceIdAndEnvType.class)))
        .thenReturn(aggregationResults);
    assertThat(instanceRepositoryCustom.getActiveServiceInstanceCountBreakdown(
                   ACCOUNT_ID, ORGANIZATION_ID, PROJECT_ID, Arrays.asList(SERVICE_ID), TIMESTAMP))
        .isEqualTo(aggregationResults);
  }

  @Test
  @Owner(developers = VIKYATH_HAREKAL)
  @Category(UnitTests.class)
  public void testUpdateInfrastructureMapping() {
    String infraMappingId = "2";
    String instanceId = "1";
    Criteria criteria = Criteria.where(InstanceKeys.id).is(instanceId);
    Query query = new Query().addCriteria(criteria);
    Update update = new Update();
    update.set(InstanceKeys.infrastructureMappingId, infraMappingId);

    instanceRepositoryCustom.updateInfrastructureMapping(instanceId, infraMappingId);
    verify(mongoTemplate).findAndModify(query, update, Instance.class);
  }

  private Project mockProject() {
    return Project.builder()
        .accountIdentifier(ACCOUNT_ID)
        .orgIdentifier(ORGANIZATION_ID)
        .identifier(PROJECT_ID)
        .build();
  }

  private InstanceCountByServiceAndEnv mockInstanceCountByServiceAndEnv(Instance instance) {
    return InstanceCountByServiceAndEnv.builder()
        .serviceIdentifier(SERVICE_ID)
        .envIdentifier(ENVIRONMENT_ID)
        .firstDocument(instance)
        .count(2)
        .build();
  }
}
