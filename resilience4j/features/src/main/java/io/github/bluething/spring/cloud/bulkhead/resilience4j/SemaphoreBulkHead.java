package io.github.bluething.spring.cloud.bulkhead.resilience4j;

import io.github.resilience4j.bulkhead.BulkheadConfig;

public class SemaphoreBulkHead {
    void printDefaultValues() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.ofDefaults();
        System.out.println("Max concurrent calls " + bulkheadConfig.getMaxConcurrentCalls());
        System.out.println("Max wait duration " + bulkheadConfig.getMaxWaitDuration());
        System.out.println("Writeable stacktrace enabled " + bulkheadConfig.isWritableStackTraceEnabled());
        System.out.println("Fair call handling enabled " + bulkheadConfig.isFairCallHandlingEnabled());
    }
    public static void main(String[] args) {
        SemaphoreBulkHead semaphoreBulkHead = new SemaphoreBulkHead();
        semaphoreBulkHead.printDefaultValues();
    }
}
