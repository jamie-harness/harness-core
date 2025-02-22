/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.grpc.server;

import io.harness.grpc.InterceptorPriority;
import io.harness.logging.LoggingListener;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.services.HealthStatusManager;
import java.io.File;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ParametersAreNonnullByDefault
public class GrpcServer extends AbstractIdleService {
  private final Server server;
  private final HealthStatusManager healthStatusManager;

  public GrpcServer(Connector connector, Set<BindableService> services, Set<ServerInterceptor> interceptors,
      HealthStatusManager healthStatusManager) {
    ServerBuilder<?> builder = ServerBuilder.forPort(connector.getPort());
    builder.maxInboundMessageSize(GrpcInProcessServer.GRPC_MAXIMUM_MESSAGE_SIZE);
    if (connector.isSecure()) {
      File certChain = new File(connector.getCertFilePath());
      File privateKey = new File(connector.getKeyFilePath());
      builder = builder.useTransportSecurity(certChain, privateKey);
    }
    sortedInterceptors(interceptors).forEach(builder::intercept);
    services.forEach(builder::addService);
    server = builder.build();
    this.healthStatusManager = healthStatusManager;
    addListener(new LoggingListener(this), MoreExecutors.directExecutor());
  }

  @Override
  protected void startUp() throws Exception {
    log.info("Starting server: {}", server);
    server.start();
    healthStatusManager.setStatus(HealthStatusManager.SERVICE_NAME_ALL_SERVICES, ServingStatus.SERVING);
    log.info("Server started successfully");
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Stopping server: {}", server);
    healthStatusManager.setStatus(HealthStatusManager.SERVICE_NAME_ALL_SERVICES, ServingStatus.NOT_SERVING);
    server.shutdown();
    server.awaitTermination();
    log.info("Server stopped");
  }

  private Stream<ServerInterceptor> sortedInterceptors(Set<ServerInterceptor> interceptorSet) {
    return interceptorSet.stream().sorted(
        Comparator
            .comparingInt(interceptor
                -> Optional.ofNullable(interceptor.getClass().getAnnotation(InterceptorPriority.class))
                       .map(InterceptorPriority::value)
                       .orElse(Integer.MAX_VALUE))
            .reversed());
  }
}
