/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.plan.consumers;

import static io.harness.OrchestrationEventsFrameworkConstants.PARTIAL_PLAN_EVENT_CONSUMER;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.eventsframework.api.Consumer;
import io.harness.pms.events.base.PmsAbstractRedisConsumer;
import io.harness.queue.QueueController;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javax.cache.Cache;

@OwnedBy(HarnessTeam.PIPELINE)
@Singleton
public class PartialPlanResponseRedisConsumer
    extends PmsAbstractRedisConsumer<PartialPlanResponseEventMessageListener> {
  @Inject
  public PartialPlanResponseRedisConsumer(@Named(PARTIAL_PLAN_EVENT_CONSUMER) Consumer redisConsumer,
      PartialPlanResponseEventMessageListener sdkResponseMessageListener,
      @Named("pmsEventsCache") Cache<String, Integer> eventsCache, QueueController queueController) {
    super(redisConsumer, sdkResponseMessageListener, eventsCache, queueController);
  }
}
