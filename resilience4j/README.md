Resilience4j provides two implementations of a bulkhead pattern that can be used to limit the number of concurrent execution:  
- A SemaphoreBulkhead which uses Semaphores.  
- A FixedThreadPoolBulkhead which uses a bounded queue, and a fixed thread pool.

#### SemaphoreBulkhead

#### FixedThreadPoolBulkhead