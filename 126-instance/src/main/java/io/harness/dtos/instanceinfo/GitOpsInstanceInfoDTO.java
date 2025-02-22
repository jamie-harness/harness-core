/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.dtos.instanceinfo;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.k8s.model.K8sContainer;
import io.harness.util.InstanceSyncKey;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
@OwnedBy(HarnessTeam.GITOPS)
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class GitOpsInstanceInfoDTO extends InstanceInfoDTO {
  @NotNull private String namespace;
  @NotNull private String podName;
  @NotEmpty private String appIdentifier;
  @NotEmpty private String clusterIdentifier;
  @NotEmpty private String agentIdentifier;
  private String podId;
  @NotNull private List<K8sContainer> containerList;

  @Override
  public String prepareInstanceKey() {
    return InstanceSyncKey.builder().part(podName).part(namespace).build().toString();
  }

  @Override
  public String prepareInstanceSyncHandlerKey() {
    return InstanceSyncKey.builder()
        .clazz(GitOpsInstanceInfoDTO.class)
        .part(appIdentifier)
        .part(agentIdentifier)
        .build()
        .toString();
  }

  @Override
  public String getPodName() {
    return podName;
  }

  @Override
  public String getType() {
    return "K8s";
  }
}
