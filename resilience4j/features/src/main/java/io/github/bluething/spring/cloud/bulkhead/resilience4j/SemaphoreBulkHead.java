package io.github.bluething.spring.cloud.bulkhead.resilience4j;

import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Flight;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.SearchRequest;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Service;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SemaphoreBulkHead {
    void printDefaultValues() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.ofDefaults();
        System.out.println("Max concurrent calls " + bulkheadConfig.getMaxConcurrentCalls());
        System.out.println("Max wait duration " + bulkheadConfig.getMaxWaitDuration());
        System.out.println("Writeable stacktrace enabled " + bulkheadConfig.isWritableStackTraceEnabled());
        System.out.println("Fair call handling enabled " + bulkheadConfig.isFairCallHandlingEnabled());
    }
    void semaphoreWithBasicUsage() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(2)
                .maxWaitDuration(Duration.ofSeconds(2))
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("flightSearchService");

        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");
        Service flightSearchService = new Service(random, dateTimeFormatter);

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "08/30/2020");

        Supplier<List<Flight>> flightSupplier = () -> {
            List<Flight> flights = new ArrayList<>();
            try {
                flights =  flightSearchService.searchFlightsTakingOneSecond(searchRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return flights;
        };
        Supplier<List<Flight>> decoratedFlightSupplier = Bulkhead.decorateSupplier(bulkhead, flightSupplier);

        for (int i = 0; i < 4; i++) {
            CompletableFuture
                    .supplyAsync(decoratedFlightSupplier)
                    .thenAccept(flights -> System.out.println("Received results"));
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SemaphoreBulkHead semaphoreBulkHead = new SemaphoreBulkHead();
        semaphoreBulkHead.printDefaultValues();
        System.out.println(" ======");
        semaphoreBulkHead.semaphoreWithBasicUsage();
        delay(3);
        System.out.println(" ======");
    }
    static void delay(int second) throws InterruptedException {
        Thread.sleep(second * 1000);
    }
}
