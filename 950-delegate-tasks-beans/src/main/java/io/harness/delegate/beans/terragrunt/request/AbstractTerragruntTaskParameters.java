/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.delegate.beans.terragrunt.request;

import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.expression.Expression.ALLOW_SECRETS;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.connector.scm.GitCapabilityHelper;
import io.harness.delegate.beans.connector.scm.adapter.ScmConnectorMapper;
import io.harness.delegate.beans.executioncapability.ExecutionCapability;
import io.harness.delegate.beans.executioncapability.ExecutionCapabilityDemander;
import io.harness.delegate.beans.storeconfig.GitStoreDelegateConfig;
import io.harness.delegate.beans.storeconfig.StoreDelegateConfig;
import io.harness.delegate.beans.storeconfig.StoreDelegateConfigType;
import io.harness.expression.Expression;
import io.harness.expression.ExpressionEvaluator;
import io.harness.reflection.ExpressionReflectionUtils.NestedAnnotationResolver;
import io.harness.security.encryption.EncryptedDataDetail;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@OwnedBy(CDP)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractTerragruntTaskParameters
    implements ExecutionCapabilityDemander, NestedAnnotationResolver {
  @NonNull String accountId;
  @NonNull String entityId;
  @NonNull TerragruntRunConfiguration runConfiguration;
  @NonNull StoreDelegateConfig configFilesStore;
  StoreDelegateConfig backendFilesStore;
  List<StoreDelegateConfig> varFiles;

  @Expression(ALLOW_SECRETS) List<String> targets;
  @Expression(ALLOW_SECRETS) String workspace;
  String stateFileId;

  List<EncryptedDataDetail> encryptedDataDetailList;

  @Override
  public List<ExecutionCapability> fetchRequiredExecutionCapabilities(ExpressionEvaluator maskingEvaluator) {
    List<ExecutionCapability> executionCapabilities = new ArrayList<>();

    addStoreCapabilities(configFilesStore, executionCapabilities);
    if (backendFilesStore != null) {
      addStoreCapabilities(backendFilesStore, executionCapabilities);
    }

    if (varFiles != null) {
      for (StoreDelegateConfig varFileStoreConfig : varFiles) {
        addStoreCapabilities(varFileStoreConfig, executionCapabilities);
      }
    }

    return executionCapabilities;
  }

  private void addStoreCapabilities(StoreDelegateConfig storeConfig, List<ExecutionCapability> executionCapabilities) {
    if (storeConfig.getType() == StoreDelegateConfigType.GIT) {
      GitStoreDelegateConfig gitStoreConfig = (GitStoreDelegateConfig) storeConfig;
      executionCapabilities.addAll(GitCapabilityHelper.fetchRequiredExecutionCapabilities(
          ScmConnectorMapper.toGitConfigDTO(gitStoreConfig.getGitConfigDTO()), encryptedDataDetailList,
          gitStoreConfig.getSshKeySpecDTO()));
    }
  }
}
