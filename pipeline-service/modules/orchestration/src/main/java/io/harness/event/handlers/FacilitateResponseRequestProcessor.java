/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.event.handlers;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.engine.OrchestrationEngine;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.events.FacilitatorResponseRequest;
import io.harness.pms.contracts.execution.events.SdkResponseEventProto;
import io.harness.pms.contracts.facilitators.FacilitatorResponseProto;
import io.harness.pms.contracts.steps.io.StepResponseProto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.PIPELINE)
@Singleton
@Slf4j
public class FacilitateResponseRequestProcessor implements SdkResponseProcessor {
  @Inject private OrchestrationEngine orchestrationEngine;

  @Override
  public void handleEvent(SdkResponseEventProto event) {
    log.info("Starting to process facilitation response");
    FacilitatorResponseRequest request = event.getFacilitatorResponseRequest();
    FacilitatorResponseProto facilitatorResponseProto = request.getFacilitatorResponse();
    if (facilitatorResponseProto.getIsSuccessful()) {
      orchestrationEngine.processFacilitatorResponse(event.getAmbiance(), facilitatorResponseProto);
    } else {
      StepResponseProto stepResponseProto = StepResponseProto.newBuilder().setStatus(Status.FAILED).build();
      orchestrationEngine.processStepResponse(event.getAmbiance(), stepResponseProto);
    }
  }
}
