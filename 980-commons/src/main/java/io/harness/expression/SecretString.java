/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.expression;

import lombok.Builder;

@Builder
public class SecretString {
  public static final String SECRET_MASK = "**************";

  private String value;

  @Override
  public String toString() {
    return value;
  }
}
