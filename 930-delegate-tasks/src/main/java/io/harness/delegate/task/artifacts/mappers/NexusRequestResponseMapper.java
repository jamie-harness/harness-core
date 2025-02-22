/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.delegate.task.artifacts.mappers;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.artifacts.beans.BuildDetailsInternal;
import io.harness.data.structure.EmptyPredicate;
import io.harness.delegate.beans.connector.nexusconnector.NexusUsernamePasswordAuthDTO;
import io.harness.delegate.task.artifacts.ArtifactSourceType;
import io.harness.delegate.task.artifacts.nexus.NexusArtifactDelegateRequest;
import io.harness.delegate.task.artifacts.nexus.NexusArtifactDelegateResponse;
import io.harness.nexus.NexusRequest;
import io.harness.utils.FieldWithPlainTextOrSecretValueHelper;

import software.wings.helpers.ext.jenkins.BuildDetails;

import lombok.experimental.UtilityClass;

@UtilityClass
@OwnedBy(HarnessTeam.CDP)
public class NexusRequestResponseMapper {
  public NexusRequest toNexusInternalConfig(NexusArtifactDelegateRequest request) {
    char[] password = null;
    String username = null;
    boolean hasCredentials = false;
    if (request.getNexusConnectorDTO().getAuth() != null
        && request.getNexusConnectorDTO().getAuth().getCredentials() != null) {
      NexusUsernamePasswordAuthDTO credentials =
          (NexusUsernamePasswordAuthDTO) request.getNexusConnectorDTO().getAuth().getCredentials();
      if (credentials.getPasswordRef() != null) {
        password = EmptyPredicate.isNotEmpty(credentials.getPasswordRef().getDecryptedValue())
            ? credentials.getPasswordRef().getDecryptedValue()
            : null;
      }
      username = FieldWithPlainTextOrSecretValueHelper.getSecretAsStringFromPlainTextOrSecretRef(
          credentials.getUsername(), credentials.getUsernameRef());
      hasCredentials = true;
    }
    return NexusRequest.builder()
        .nexusUrl(request.getNexusConnectorDTO().getNexusServerUrl())
        .username(username)
        .password(password)
        .version(request.getNexusConnectorDTO().getVersion())
        .hasCredentials(hasCredentials)
        .artifactRepositoryUrl(request.getArtifactRepositoryUrl())
        .build();
  }

  public NexusArtifactDelegateResponse toNexusResponse(
      BuildDetailsInternal buildDetailsInternal, NexusArtifactDelegateRequest request) {
    return NexusArtifactDelegateResponse.builder()
        .buildDetails(ArtifactBuildDetailsMapper.toBuildDetailsNG(buildDetailsInternal))
        .repositoryName(request.getRepositoryName())
        .artifactPath(request.getArtifactPath())
        .repositoryFormat(request.getRepositoryFormat())
        .tag(buildDetailsInternal.getNumber())
        .sourceType(ArtifactSourceType.NEXUS3_REGISTRY)
        .build();
  }

  public NexusArtifactDelegateResponse toNexusResponse(
      BuildDetails buildDetailsInternal, NexusArtifactDelegateRequest request) {
    return NexusArtifactDelegateResponse.builder()
        .buildDetails(ArtifactBuildDetailsMapper.toBuildDetailsNG(buildDetailsInternal))
        .repositoryName(request.getRepositoryName())
        .artifactPath(request.getArtifactPath())
        .repositoryFormat(request.getRepositoryFormat())
        .tag(buildDetailsInternal.getNumber())
        .sourceType(request.getSourceType())
        .build();
  }
}
