/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.hsqs.client.model;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;

import lombok.Builder;
import lombok.Value;

/**
 * UnAckRequest Object used for block listing subTopic from processing or marking a message processing is failed
 */
@OwnedBy(PIPELINE)
@Value
@Builder
public class UnAckRequest {
  String itemID;

  Integer retryTimeAfterDuration;

  String subTopic;

  String topic;

  Integer type;
}
