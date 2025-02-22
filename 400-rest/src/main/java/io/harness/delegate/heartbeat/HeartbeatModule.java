/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.heartbeat;

import io.harness.delegate.heartbeat.polling.DelegatePollingHeartbeatService;
import io.harness.delegate.heartbeat.stream.DelegateStreamHeartbeatService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class HeartbeatModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(DelegateStreamHeartbeatService.class).in(Scopes.SINGLETON);
    bind(DelegatePollingHeartbeatService.class).in(Scopes.SINGLETON);
  }
}
