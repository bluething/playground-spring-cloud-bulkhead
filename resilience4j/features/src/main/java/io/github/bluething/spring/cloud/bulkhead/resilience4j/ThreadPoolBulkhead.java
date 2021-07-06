package io.github.bluething.spring.cloud.bulkhead.resilience4j;

import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;

public class ThreadPoolBulkhead {
    void printDefaultValue() {
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = ThreadPoolBulkheadConfig.ofDefaults();
        System.out.println("Maximum thread pool size " + threadPoolBulkheadConfig.getMaxThreadPoolSize());
        System.out.println("Maximum core thread pool size " + threadPoolBulkheadConfig.getCoreThreadPoolSize());
        System.out.println("Keep alive duration " + threadPoolBulkheadConfig.getKeepAliveDuration());
        System.out.println("Queue capacity " + threadPoolBulkheadConfig.getQueueCapacity());
    }

    public static void main(String[] args) {
        ThreadPoolBulkhead threadPoolBulkhead = new ThreadPoolBulkhead();
        threadPoolBulkhead.printDefaultValue();
        System.out.println(" ====== ");
    }
}
