package io.github.bluething.spring.cloud.bulkhead.resilience4j;

import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Flight;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.SearchRequest;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Service;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class ThreadPoolBulkheadSample {
    void printDefaultValue() {
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = ThreadPoolBulkheadConfig.ofDefaults();
        System.out.println("Maximum thread pool size " + threadPoolBulkheadConfig.getMaxThreadPoolSize());
        System.out.println("Maximum core thread pool size " + threadPoolBulkheadConfig.getCoreThreadPoolSize());
        System.out.println("Keep alive duration " + threadPoolBulkheadConfig.getKeepAliveDuration());
        System.out.println("Queue capacity " + threadPoolBulkheadConfig.getQueueCapacity());
    }

    void threadPoolBasicUsage() {
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(2)
                .coreThreadPoolSize(1)
                .queueCapacity(1)
                .build();
        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = ThreadPoolBulkheadRegistry.of(threadPoolBulkheadConfig);
        ThreadPoolBulkhead bulkhead = threadPoolBulkheadRegistry.bulkhead("flightSearchService");

        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");
        Service flightSearchService = new Service(random, dateTimeFormatter);

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "07/06/2021");

        Supplier<List<Flight>> flightSupplier = () -> {
            List<Flight> flights = new ArrayList<>();
            try {
                flights = flightSearchService.searchFlightsTakingOneSecond(searchRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return flights;
        };
        Supplier<CompletionStage<List<Flight>>> decoratedFlightSupplier = ThreadPoolBulkhead.decorateSupplier(bulkhead, flightSupplier);

        for (int i = 0; i < 3; i++) {
            decoratedFlightSupplier
                    .get()
                    .whenComplete((r, t) -> {
                        if (r != null) {
                            System.out.println("Received results");
                        }
                        if (t != null) {
                            t.printStackTrace();
                        }
                    });
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolBulkheadSample threadPoolBulkhead = new ThreadPoolBulkheadSample();
        threadPoolBulkhead.printDefaultValue();
        System.out.println(" ====== ");
        threadPoolBulkhead.threadPoolBasicUsage();
        delay(5);
    }

    static void delay(int second) throws InterruptedException {
        Thread.sleep(second * 1000);
    }
}
