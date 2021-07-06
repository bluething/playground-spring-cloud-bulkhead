Resilience4j provides two implementations of a bulkhead pattern that can be used to limit the number of concurrent execution:  
- A SemaphoreBulkhead which uses Semaphores.  
- A FixedThreadPoolBulkhead which uses a bounded queue, and a fixed thread pool.

The BulkHead emits a stream of BulkHeadEvents. There are two types of events emitted: permitted execution, rejected execution & finished execution.  
```text
2021-07-05T11:08:35.582658+07:00[Asia/Jakarta]: Bulkhead 'flightSearchService' permitted a call.
2021-07-05T11:08:36.083074+07:00[Asia/Jakarta]: Bulkhead 'flightSearchService' rejected a call.
2021-07-05T11:08:36.585053+07:00[Asia/Jakarta]: Bulkhead 'flightSearchService' has finished a call.
```

Config property | Default Value | Description
--- | --- | ---
maxConcurrentCalls | 25 | Max amount of parallel executions allowed by the bulkhead
maxWaitDuration | 0s | Max amount of time a thread should be blocked for when attempting to enter a saturated bulkhead.

#### SemaphoreBulkhead

Config property | Default Value | Description
--- | --- | ---
fairCallHandlingEnabled | true | When there are multiple threads waiting for permits, the fairCallHandlingEnabled configuration determines if the waiting threads acquire permits in a [first-in, first-out order](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Semaphore.html#Semaphore-int-boolean-).
writableStackTraceEnabled | true | Enables writable stack traces. When set to false, Exception.getStackTrace() returns a zero length array. This may be used to reduce log spam when the circuit breaker is open as the cause of the exceptions is already known (the circuit breaker is short-circuiting calls).

Any thread which attempts to call the remote service over `maxConcurrentCalls` can either get a BulkheadFullException immediately or wait for some time for a permit to be released by another thread. This is determined by the `maxWaitDuration` value.

```text
Searching for flights; current time = 10:38:12 879; current thread = ForkJoinPool.commonPool-worker-3
Searching for flights; current time = 10:38:12 879; current thread = ForkJoinPool.commonPool-worker-5
Flight search successful at 10:38:12 921
Flight search successful at 10:38:12 921
Received results
Received results
Searching for flights; current time = 10:38:13 922; current thread = ForkJoinPool.commonPool-worker-9
Searching for flights; current time = 10:38:13 922; current thread = ForkJoinPool.commonPool-worker-7
Flight search successful at 10:38:13 922
Flight search successful at 10:38:13 922
Received results
Received results
```  
With `maxConcurrentCalls` set to 2 and maxWaitDuration set to 2s the third, and the fourth requests were able to acquire permits only 1s later, after the previous requests completed.

```text
io.github.resilience4j.bulkhead.BulkheadFullException: Bulkhead 'flightSearchService' is full and does not permit further calls
	at io.github.resilience4j.bulkhead.BulkheadFullException.createBulkheadFullException(BulkheadFullException.java:49)
	at io.github.resilience4j.bulkhead.internal.SemaphoreBulkhead.acquirePermission(SemaphoreBulkhead.java:164)
	at io.github.resilience4j.bulkhead.Bulkhead.lambda$decorateSupplier$5(Bulkhead.java:194)
	at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1764)
	at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1756)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:290)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1016)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1665)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1598)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:177)
```  
With `maxConcurrentCalls` set to 2 and maxWaitDuration set to 1s we will get this exception for the 3rd thread.  
Turn off the stack trace with `writableStackTraceEnabled` set to false.

For `SemaphoreBulkhead` there are two metrics exposed

Metric name | Description
--- | ---
resilience4j.bulkhead.max.allowed.concurrent.calls | the maximum number of available permissions
resilience4j.bulkhead.available.concurrent.calls | the number of allowed concurrent calls

```text
The maximum number of available permissions - resilience4j.bulkhead.max.allowed.concurrent.calls: 8.0
The maximum number of available permissions - resilience4j.bulkhead.max.allowed.concurrent.calls: 8.0
The maximum number of available permissions - resilience4j.bulkhead.max.allowed.concurrent.calls: 8.0
The number of available permissions - resilience4j.bulkhead.available.concurrent.calls: 3.0
The maximum number of available permissions - resilience4j.bulkhead.max.allowed.concurrent.calls: 8.0
The maximum number of available permissions - resilience4j.bulkhead.max.allowed.concurrent.calls: 8.0
The number of available permissions - resilience4j.bulkhead.available.concurrent.calls: 3.0
The number of available permissions - resilience4j.bulkhead.available.concurrent.calls: 3.0
The number of available permissions - resilience4j.bulkhead.available.concurrent.calls: 3.0
The number of available permissions - resilience4j.bulkhead.available.concurrent.calls: 3.0
```

#### ThreadPoolBulkhead

Config property | Default Value | Description
--- | --- | ---
maxThreadPoolSize | Runtime.getRuntime().availableProcessors() | Configures the max thread pool size
coreThreadPoolSize | Runtime.getRuntime().availableProcessors() - 1 | Configures the core thread pool size
keepAliveDuration | 200ms | When the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
queueCapacity | 100 | Configures the capacity of the queue.

ThreadPoolBulkhead internally uses these configurations to construct a [ThreadPoolExecutor](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html).

The internalThreadPoolExecutor executes incoming tasks using one of the available, free threads. If no thread is free to execute an incoming task, the task is enqueued for executing later when a thread becomes available. If the `queueCapacity` has been reached, then the remote call is rejected with a `BulkheadFullException`.

If there are no free threads and no capacity in the queue, a BulkheadFullException is thrown:  
```text
Exception in thread "main" io.github.resilience4j.bulkhead.BulkheadFullException: Bulkhead 'flightSearchService' is full and does not permit further calls
	at io.github.resilience4j.bulkhead.BulkheadFullException.createBulkheadFullException(BulkheadFullException.java:64)
	at io.github.resilience4j.bulkhead.internal.FixedThreadPoolBulkhead.submit(FixedThreadPoolBulkhead.java:158)
	at io.github.resilience4j.bulkhead.internal.FixedThreadPoolBulkhead.submit(FixedThreadPoolBulkhead.java:47)
	at io.github.resilience4j.bulkhead.ThreadPoolBulkhead.lambda$decorateSupplier$1(ThreadPoolBulkhead.java:69)
	at io.github.bluething.spring.cloud.bulkhead.resilience4j.ThreadPoolBulkheadSample.threadPoolWithException(ThreadPoolBulkheadSample.java:93)
	at io.github.bluething.spring.cloud.bulkhead.resilience4j.ThreadPoolBulkheadSample.main(ThreadPoolBulkheadSample.java:152)
```  
To reduce the amount of information that is generated in the stack trace set `writableStackTraceEnabled` to false.

ThreadPoolBulkhead exposes five metrics

Metric name | Description
--- | ---
resilience4j.bulkhead.queue.depth | The current length of the queue
resilience4j.bulkhead.thread.pool.size | The current size of the thread pool
resilience4j.bulkhead.core.thread.pool.size | The core sizes of the thread pool
resilience4j.bulkhead.max.thread.pool.size | The maximum sizes of the thread pool
resilience4j.bulkhead.queue.capacity | The capacity of the queue

```text
The queue depth - resilience4j.bulkhead.queue.depth: 0.0
The thread pool size - resilience4j.bulkhead.thread.pool.size: 3.0
The maximum thread pool size - resilience4j.bulkhead.max.thread.pool.size: 5.0
The core thread pool size - resilience4j.bulkhead.core.thread.pool.size: 3.0
The queue capacity - resilience4j.bulkhead.queue.capacity: 5.0
```