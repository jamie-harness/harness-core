package io.harness.cdng.gitops.beans;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.gitops.GitOpsSpecParameters;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.pms.yaml.ParameterField;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@OwnedBy(HarnessTeam.GITOPS)
public class FetchLinkedAppsStepParams implements GitOpsSpecParameters {
  @Override
  public ParameterField<List<TaskSelectorYaml>> getDelegateSelectors() {
    return null;
  }
}
