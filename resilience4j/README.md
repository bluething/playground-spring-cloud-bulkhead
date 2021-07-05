Resilience4j provides two implementations of a bulkhead pattern that can be used to limit the number of concurrent execution:  
- A SemaphoreBulkhead which uses Semaphores.  
- A FixedThreadPoolBulkhead which uses a bounded queue, and a fixed thread pool.

Config property | Default Value | Description
--- | --- | ---
maxConcurrentCalls | 25 | Max amount of parallel executions allowed by the bulkhead
maxWaitDuration | | Max amount of time a thread should be blocked for when attempting to enter a saturated bulkhead.

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
With `maxConcurrentCalls` set to 2 the third, and the fourth requests were able to acquire permits only 1s later, after the previous requests completed.

#### FixedThreadPoolBulkhead