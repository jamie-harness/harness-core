/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ci.integrationstage;

import static io.harness.ci.commonconstants.CIExecutionConstants.MAXIMUM_EXPANSION_LIMIT;
import static io.harness.ci.commonconstants.CIExecutionConstants.MAXIMUM_EXPANSION_LIMIT_FREE_ACCOUNT;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.executionargs.CIExecutionArgs;
import io.harness.beans.serializer.RunTimeInputHandler;
import io.harness.beans.stages.IntegrationStageNode;
import io.harness.beans.steps.stepinfo.InitializeStepInfo;
import io.harness.beans.yaml.extended.infrastrucutre.Infrastructure;
import io.harness.ci.license.CILicenseService;
import io.harness.cimanager.stages.IntegrationStageConfig;
import io.harness.licensing.Edition;
import io.harness.licensing.beans.summary.LicensesWithSummaryDTO;
import io.harness.plancreator.execution.ExecutionElementConfig;
import io.harness.plancreator.execution.ExecutionWrapperConfig;
import io.harness.steps.matrix.ExpandedExecutionWrapperInfo;
import io.harness.steps.matrix.StrategyExpansionData;
import io.harness.steps.matrix.StrategyHelper;
import io.harness.yaml.extended.ci.codebase.CodeBase;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@OwnedBy(HarnessTeam.CI)
public class InitializeStepGenerator {
  private static final String INITIALIZE_TASK = InitializeStepInfo.STEP_TYPE.getType();
  @Inject private BuildJobEnvInfoBuilder buildJobEnvInfoBuilder;
  @Inject private CILicenseService ciLicenseService;
  @Inject private StrategyHelper strategyHelper;

  InitializeStepInfo createInitializeStepInfo(ExecutionElementConfig executionElement, CodeBase ciCodebase,
      IntegrationStageNode stageNode, CIExecutionArgs ciExecutionArgs, Infrastructure infrastructure,
      String accountId) {
    IntegrationStageConfig integrationStageConfig = IntegrationStageUtils.getIntegrationStageConfig(stageNode);

    List<ExecutionWrapperConfig> expandedExecutionElement = new ArrayList<>();
    Map<String, StrategyExpansionData> strategyExpansionMap = new HashMap<>();

    LicensesWithSummaryDTO licensesWithSummaryDTO = ciLicenseService.getLicenseSummary(accountId);
    Optional<Integer> maxExpansionLimit = Optional.of(Integer.valueOf(MAXIMUM_EXPANSION_LIMIT));
    if (licensesWithSummaryDTO != null && licensesWithSummaryDTO.getEdition() == Edition.FREE) {
      maxExpansionLimit = Optional.of(Integer.valueOf(MAXIMUM_EXPANSION_LIMIT_FREE_ACCOUNT));
    }

    for (ExecutionWrapperConfig config : executionElement.getSteps()) {
      // Inject the envVariables before calling strategy expansion
      IntegrationStageUtils.injectLoopEnvVariables(config);
      ExpandedExecutionWrapperInfo expandedExecutionWrapperInfo =
          strategyHelper.expandExecutionWrapperConfig(config, maxExpansionLimit);
      expandedExecutionElement.addAll(expandedExecutionWrapperInfo.getExpandedExecutionConfigs());
      strategyExpansionMap.putAll(expandedExecutionWrapperInfo.getUuidToStrategyExpansionData());
    }

    boolean gitClone = RunTimeInputHandler.resolveGitClone(integrationStageConfig.getCloneCodebase());
    return InitializeStepInfo.builder()
        .identifier(INITIALIZE_TASK)
        .name(INITIALIZE_TASK)
        .infrastructure(infrastructure)
        .stageIdentifier(stageNode.getIdentifier())
        .variables(stageNode.getVariables())
        .stageElementConfig(integrationStageConfig)
        .executionSource(ciExecutionArgs.getExecutionSource())
        .ciCodebase(ciCodebase)
        .skipGitClone(!gitClone)
        .strategyExpansionMap(strategyExpansionMap)
        .executionElementConfig(ExecutionElementConfig.builder().steps(expandedExecutionElement).build())
        .timeout(buildJobEnvInfoBuilder.getTimeout(infrastructure))
        .build();
  }
}
