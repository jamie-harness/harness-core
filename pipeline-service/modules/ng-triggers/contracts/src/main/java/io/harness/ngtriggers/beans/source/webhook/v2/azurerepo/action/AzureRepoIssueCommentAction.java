/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.ngtriggers.beans.source.webhook.v2.azurerepo.action;

import static io.harness.annotations.dev.HarnessTeam.CI;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ngtriggers.beans.source.webhook.v2.git.GitAction;

import com.fasterxml.jackson.annotation.JsonProperty;

@OwnedBy(CI)
public enum AzureRepoIssueCommentAction implements GitAction {
  @JsonProperty("Create") CREATE("create", "Create"),
  @JsonProperty("Edit") EDIT("edit", "Edit"),
  @JsonProperty("Delete") DELETE("delete", "Delete");

  private String value;
  private String parsedValue;

  AzureRepoIssueCommentAction(String parsedValue, String value) {
    this.parsedValue = parsedValue;
    this.value = value;
  }

  @Override
  public String getParsedValue() {
    return parsedValue;
  }

  @Override
  public String getValue() {
    return value;
  }
}
