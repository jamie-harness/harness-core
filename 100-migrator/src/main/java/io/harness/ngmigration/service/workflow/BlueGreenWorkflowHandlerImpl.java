/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ngmigration.service.workflow;

import io.harness.cdng.service.beans.ServiceDefinitionType;
import io.harness.ngmigration.service.step.StepMapperFactory;

import software.wings.beans.CanaryOrchestrationWorkflow;
import software.wings.beans.GraphNode;
import software.wings.beans.Workflow;
import software.wings.beans.WorkflowPhase.Yaml;
import software.wings.service.impl.yaml.handler.workflow.BlueGreenWorkflowYamlHandler;
import software.wings.yaml.workflow.BlueGreenWorkflowYaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import java.util.List;

public class BlueGreenWorkflowHandlerImpl extends WorkflowHandler {
  @Inject BlueGreenWorkflowYamlHandler blueGreenWorkflowYamlHandler;

  @Inject private StepMapperFactory stepMapperFactory;

  @Override
  public List<Yaml> getPhases(Workflow workflow) {
    BlueGreenWorkflowYaml blueGreenWorkflowYaml = blueGreenWorkflowYamlHandler.toYaml(workflow, workflow.getAppId());
    return blueGreenWorkflowYaml.getPhases();
  }

  @Override
  public List<GraphNode> getSteps(Workflow workflow) {
    CanaryOrchestrationWorkflow orchestrationWorkflow =
        (CanaryOrchestrationWorkflow) workflow.getOrchestrationWorkflow();
    return getSteps(orchestrationWorkflow.getWorkflowPhases(), orchestrationWorkflow.getPreDeploymentSteps(),
        orchestrationWorkflow.getPostDeploymentSteps());
  }

  @Override
  public List<Yaml> getRollbackPhases(Workflow workflow) {
    BlueGreenWorkflowYaml blueGreenWorkflowYaml = blueGreenWorkflowYamlHandler.toYaml(workflow, workflow.getAppId());
    return blueGreenWorkflowYaml.getRollbackPhases();
  }

  @Override
  public boolean areSimilar(Workflow workflow1, Workflow workflow2) {
    return areSimilar(stepMapperFactory, workflow1, workflow2);
  }

  @Override
  public JsonNode getTemplateSpec(Workflow workflow) {
    return getDeploymentStageTemplateSpec(workflow, stepMapperFactory);
  }

  @Override
  public ServiceDefinitionType inferServiceDefinitionType(Workflow workflow) {
    // We can infer the type based on the service, infra & sometimes based on the steps used.
    // TODO: Deepak Puthraya
    return ServiceDefinitionType.KUBERNETES;
  }
}
