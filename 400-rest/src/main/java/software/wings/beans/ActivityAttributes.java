/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.beans;

import software.wings.beans.Activity.Type;
import software.wings.beans.artifact.Artifact;
import software.wings.beans.command.CommandUnit;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ActivityAttributes {
  private Type type;
  private String commandName;
  private String commandType;
  private List<CommandUnit> commandUnits;
  private Artifact artifact;
}
