/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.jobs;

import io.harness.cvng.activity.entities.Activity;
import io.harness.cvng.cdng.entities.CVNGStepTask;
import io.harness.cvng.core.entities.CVConfig;
import io.harness.cvng.core.entities.MetricPack;
import io.harness.cvng.core.entities.MonitoredService;
import io.harness.cvng.core.entities.MonitoringSourcePerpetualTask;
import io.harness.cvng.core.entities.ServiceDependency;
import io.harness.cvng.core.entities.TimeSeriesThreshold;
import io.harness.cvng.core.entities.Webhook;
import io.harness.cvng.core.entities.changeSource.ChangeSource;
import io.harness.cvng.core.services.api.CVConfigService;
import io.harness.cvng.core.services.api.DeleteEntityByHandler;
import io.harness.cvng.core.services.api.MonitoringSourcePerpetualTaskService;
import io.harness.cvng.core.services.api.monitoredService.ChangeSourceService;
import io.harness.cvng.core.services.api.monitoredService.MonitoredServiceService;
import io.harness.cvng.dashboard.entities.HeatMap;
import io.harness.cvng.notification.entities.NotificationRule;
import io.harness.cvng.servicelevelobjective.entities.AbstractServiceLevelObjective;
import io.harness.cvng.servicelevelobjective.entities.SLOErrorBudgetReset;
import io.harness.cvng.servicelevelobjective.entities.SLOHealthIndicator;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelIndicator;
import io.harness.cvng.servicelevelobjective.entities.ServiceLevelObjective;
import io.harness.cvng.servicelevelobjective.entities.UserJourney;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelObjectiveService;
import io.harness.cvng.servicelevelobjective.services.api.ServiceLevelObjectiveV2Service;
import io.harness.cvng.verificationjob.entities.VerificationJob;
import io.harness.persistence.PersistentEntity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class EntityChangeEventMessageProcessor implements ConsumerMessageProcessor {
  @VisibleForTesting
  static final Map<Class<? extends PersistentEntity>, Class<? extends DeleteEntityByHandler>> ENTITIES_MAP;
  @VisibleForTesting static final Set<Class<? extends PersistentEntity>> EXCEPTIONS;

  static {
    // Add the service for project level default deletion
    final List<Class<? extends PersistentEntity>> deleteEntitiesWithDefaultHandler =
        Arrays.asList(VerificationJob.class, Activity.class, MetricPack.class, HeatMap.class, TimeSeriesThreshold.class,
            CVNGStepTask.class, UserJourney.class, Webhook.class, ServiceDependency.class, SLOHealthIndicator.class,
            SLOErrorBudgetReset.class, NotificationRule.class);
    ENTITIES_MAP = new LinkedHashMap<>();
    deleteEntitiesWithDefaultHandler.forEach(entity -> ENTITIES_MAP.put(entity, DeleteEntityByHandler.class));

    // Add the service for project level custom deletion
    ENTITIES_MAP.put(MonitoringSourcePerpetualTask.class, MonitoringSourcePerpetualTaskService.class);
    ENTITIES_MAP.put(ServiceLevelObjective.class, ServiceLevelObjectiveService.class);
    ENTITIES_MAP.put(MonitoredService.class, MonitoredServiceService.class);
    ENTITIES_MAP.put(CVConfig.class, CVConfigService.class);
    ENTITIES_MAP.put(ChangeSource.class, ChangeSourceService.class);
    ENTITIES_MAP.put(AbstractServiceLevelObjective.class, ServiceLevelObjectiveV2Service.class);

    EXCEPTIONS = new HashSet<>();
    EXCEPTIONS.add(ServiceLevelIndicator.class);
  }
}
