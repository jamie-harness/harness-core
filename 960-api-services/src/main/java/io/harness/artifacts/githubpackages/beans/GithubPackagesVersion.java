/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.artifacts.githubpackages.beans;

import static io.harness.annotations.dev.HarnessTeam.CDC;

import io.harness.annotations.dev.OwnedBy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@OwnedBy(CDC)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubPackagesVersion {
  /**
   * Version ID
   */
  String versionId;

  /**
   * Version Name(SHA-256 Value)
   */
  String versionName;

  /**
   * Version URL
   */
  String versionUrl;

  /**
   * Version Html URL
   */
  String versionHtmlUrl;

  /**
   * Package URL
   */
  String packageUrl;

  /**
   * Created At
   */
  String createdAt;

  /**
   * Last Updated At
   */
  String lastUpdatedAt;

  /**
   * Package Type
   */
  String packageType;

  /**
   * Tags
   */
  List<String> tags;
}
