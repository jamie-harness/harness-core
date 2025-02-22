/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.cdng.k8s;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotation.RecasterAlias;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.CDStepHelper;
import io.harness.cdng.manifest.yaml.ManifestConfigWrapper;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.k8s.K8sCommandUnitConstants;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.pms.yaml.ParameterField;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(CDP)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TypeAlias("k8sApplyStepParameters")
@RecasterAlias("io.harness.cdng.k8s.K8sApplyStepParameters")
public class K8sApplyStepParameters extends K8sApplyBaseStepInfo implements K8sSpecParameters {
  @Builder(builderMethodName = "infoBuilder")
  public K8sApplyStepParameters(ParameterField<Boolean> skipDryRun, ParameterField<Boolean> skipSteadyStateCheck,
      ParameterField<List<String>> filePaths, ParameterField<List<TaskSelectorYaml>> delegateSelectors,
      List<ManifestConfigWrapper> overrides, ParameterField<Boolean> skipRendering) {
    super(skipDryRun, skipSteadyStateCheck, filePaths, delegateSelectors, overrides, skipRendering);
  }

  @Nonnull
  @Override
  @JsonIgnore
  public List<String> getCommandUnits() {
    List<String> commandUnits = K8sSpecParameters.super.getCommandUnits();
    if (!ParameterField.isNull(skipSteadyStateCheck)
        && CDStepHelper.getParameterFieldBooleanValue(skipSteadyStateCheck,
            K8sApplyBaseStepInfoKeys.skipSteadyStateCheck,
            String.format("%s step", ExecutionNodeType.K8S_APPLY.getYamlType()))) {
      commandUnits.remove(K8sCommandUnitConstants.WaitForSteadyState);
    }
    return commandUnits;
  }
}
