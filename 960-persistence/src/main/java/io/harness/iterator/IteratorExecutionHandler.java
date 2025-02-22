package io.harness.iterator;

import lombok.Builder;
import lombok.Value;

/**
 * IteratorExecutionHandler provides an interface that allows -
 * 1. Services to register their iterator and iterator handler.
 * 2. Configuration watcher to invoke a handler to apply the iterator configuration.
 */
public interface IteratorExecutionHandler {
  @Value
  @Builder
  class DynamicIteratorConfig {
    String name;
    boolean enabled;
    int threadPoolSize;
    int threadPoolIntervalInSeconds;
    String nextIterationMode;
    int targetIntervalInSeconds;
    int throttleIntervalInSeconds;
  }

  /**
   * This method allows to register an iterator handler
   * for an iterator.
   * @param iteratorName Name of the iterator
   * @param iteratorHandler Handler for the iterator
   */
  void registerIteratorHandler(String iteratorName, IteratorBaseHandler iteratorHandler);

  /**
   * This method allows to start all the iterators
   * that are registered with the iterator handler.
   */
  void startIterators();

  /**
   * All the executors or handlers of iterator configuration
   * should implement this method. The executor should also
   * define failure strategies in case it fails to apply the
   * configuration.
   */
  void applyConfiguration(DynamicIteratorConfig iteratorConfigOption);
}
