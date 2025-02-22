/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.serializer;

import io.harness.EntityType;
import io.harness.ModuleType;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.stages.SecurityStageNode;
import io.harness.beans.steps.STOStepInfo;
import io.harness.beans.steps.STOStepType;
import io.harness.beans.steps.nodes.RunStepNode;
import io.harness.beans.steps.nodes.SecurityNode;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.yaml.schema.beans.SchemaNamespaceConstants;
import io.harness.yaml.schema.beans.YamlGroup;
import io.harness.yaml.schema.beans.YamlSchemaMetadata;
import io.harness.yaml.schema.beans.YamlSchemaRootClass;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import lombok.experimental.UtilityClass;

@OwnedBy(HarnessTeam.STO)
@UtilityClass
public class StoBeansRegistrars {
  private YamlSchemaRootClass createStepYaml(STOStepType stepType) {
    return YamlSchemaRootClass.builder()
        .entityType(stepType.getEntityType())
        .availableAtProjectLevel(true)
        .availableAtOrgLevel(false)
        .yamlSchemaMetadata(YamlSchemaMetadata.builder()
                                .modulesSupported(Collections.singletonList(ModuleType.STO))
                                .yamlGroup(YamlGroup.builder().group(StepCategory.STEP.name()).build())
                                .build())
        .availableAtAccountLevel(false)
        .clazz(stepType.getNode())
        .build();
  }

  private ImmutableList<YamlSchemaRootClass> createSecurityStepYamlDefinitions() {
    ImmutableList.Builder<YamlSchemaRootClass> stepPaletteListBuilder = ImmutableList.<YamlSchemaRootClass>builder();

    Arrays.asList(STOStepType.values()).forEach(e -> stepPaletteListBuilder.add(createStepYaml(e)));

    return stepPaletteListBuilder.build();
  }

  public static final ImmutableList<YamlSchemaRootClass> yamlSchemaRegistrars =
      ImmutableList.<YamlSchemaRootClass>builder()
          .add(YamlSchemaRootClass.builder()
                   .entityType(EntityType.SECURITY_STAGE)
                   .availableAtProjectLevel(true)
                   .availableAtOrgLevel(false)
                   .availableAtAccountLevel(false)
                   .clazz(SecurityStageNode.class)
                   .yamlSchemaMetadata(YamlSchemaMetadata.builder()
                                           .namespace(SchemaNamespaceConstants.STO)
                                           .modulesSupported(ImmutableList.of(ModuleType.STO, ModuleType.PMS))
                                           .yamlGroup(YamlGroup.builder().group(StepCategory.STAGE.name()).build())
                                           .build())
                   .build())
          .add(YamlSchemaRootClass.builder()
                   .entityType(EntityType.SECURITY_STEPS)
                   .availableAtProjectLevel(true)
                   .availableAtOrgLevel(false)
                   .availableAtAccountLevel(false)
                   .clazz(STOStepInfo.class)
                   .build())
          .add(YamlSchemaRootClass.builder()
                   .entityType(EntityType.SECURITY)
                   .availableAtProjectLevel(true)
                   .availableAtOrgLevel(false)
                   .yamlSchemaMetadata(YamlSchemaMetadata.builder()
                                           .modulesSupported(Collections.singletonList(ModuleType.STO))
                                           .yamlGroup(YamlGroup.builder().group(StepCategory.STEP.name()).build())
                                           .build())
                   .availableAtAccountLevel(false)
                   .clazz(SecurityNode.class)
                   .build())
          .add(YamlSchemaRootClass.builder()
                   .entityType(EntityType.RUN_STEP)
                   .availableAtProjectLevel(true)
                   .availableAtOrgLevel(false)
                   .yamlSchemaMetadata(YamlSchemaMetadata.builder()
                                           .modulesSupported(Collections.singletonList(ModuleType.STO))
                                           .yamlGroup(YamlGroup.builder().group(StepCategory.STEP.name()).build())
                                           .build())
                   .availableAtAccountLevel(false)
                   .clazz(RunStepNode.class)
                   .build())
          .addAll(createSecurityStepYamlDefinitions())
          .build();
}
