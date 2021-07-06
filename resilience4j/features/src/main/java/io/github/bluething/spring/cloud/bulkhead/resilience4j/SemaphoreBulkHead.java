package io.github.bluething.spring.cloud.bulkhead.resilience4j;

import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Flight;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.SearchRequest;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Service;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

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

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "07/03/2021");

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
    void semaphoreWithException() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(2)
                .maxWaitDuration(Duration.ofSeconds(1))
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("flightSearchService");

        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");
        Service flightSearchService = new Service(random, dateTimeFormatter);

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "07/05/2021");

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
            CompletableFuture.supplyAsync(decoratedFlightSupplier)
                    .whenComplete((r, t) -> {
                        if (t != null) {
                            Throwable cause = t.getCause();
                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                        if (r != null) {
                            System.out.println("Received results");
                        }
                    });
        }
    }
    void semaphoreWithExceptionTurnOffTheStackTrace() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(2)
                .maxWaitDuration(Duration.ofSeconds(1))
                .writableStackTraceEnabled(false)
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("flightSearchService");

        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");
        Service flightSearchService = new Service(random, dateTimeFormatter);

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "07/05/2021");

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
            CompletableFuture.supplyAsync(decoratedFlightSupplier)
                    .whenComplete((r, t) -> {
                        if (t != null) {
                            Throwable cause = t.getCause();
                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                        if (r != null) {
                            System.out.println("Received results");
                        }
                    });
        }
    }
    void semaphoteEvent() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(3)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("flightSearchService");

        bulkhead.getEventPublisher()
                .onCallPermitted(event -> System.out.println(event.toString()));
        bulkhead.getEventPublisher()
                .onCallRejected(event -> System.out.println(event.toString()));
        bulkhead.getEventPublisher()
                .onCallFinished(event -> System.out.println(event.toString()));

        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");
        Service flightSearchService = new Service(random, dateTimeFormatter);

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "07/05/2021");

        Supplier<List<Flight>> flightSupplier = () -> {
            List<Flight> flights = new ArrayList<>();
            try {
                flights =  flightSearchService.searchFlightsTakingRandomTime(searchRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return flights;
        };
        Supplier<List<Flight>> decoratedFlightSupplier = Bulkhead.decorateSupplier(bulkhead, flightSupplier);

        for (int i = 0; i < 4; i++) {
            CompletableFuture.supplyAsync(decoratedFlightSupplier)
                    .whenComplete((r, t) -> {
                        if (t != null) {
                            Throwable cause = t.getCause();
                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                        if (r != null) {
                            System.out.println("Received results");
                        }
                    });
        }
    }
    void semaphoreMetrics() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(8)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("flightSearchService");

        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        TaggedBulkheadMetrics.ofBulkheadRegistry(bulkheadRegistry).bindTo(meterRegistry);

        bulkhead.getEventPublisher()
                .onCallPermitted(event -> printMetricDetails(meterRegistry));
        bulkhead.getEventPublisher()
                .onCallRejected(event -> printMetricDetails(meterRegistry));
        bulkhead.getEventPublisher()
                .onCallFinished(event -> printMetricDetails(meterRegistry));

        Random random = new Random();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss SSS");
        Service flightSearchService = new Service(random, dateTimeFormatter);

        SearchRequest searchRequest = new SearchRequest("NYC", "LAX", "07/05/2021");

        Supplier<List<Flight>> flightSupplier = () -> {
            List<Flight> flights = new ArrayList<>();
            try {
                flights =  flightSearchService.searchFlightsTakingRandomTime(searchRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return flights;
        };
        Supplier<List<Flight>> decoratedFlightSupplier = Bulkhead.decorateSupplier(bulkhead, flightSupplier);

        for (int i = 0; i < 5; i++) {
            CompletableFuture.supplyAsync(decoratedFlightSupplier)
                    .whenComplete((r , t) -> {
                        if (r != null) {
                            System.out.println("Received results");
                        }
                    });
        }
    }
    void printMetricDetails(MeterRegistry meterRegistry) {
        Consumer<Meter> meterConsumer = meter -> {
          String desc = meter.getId().getDescription();
          String metricName = meter.getId().getName();
          Double metricValue = StreamSupport.stream(meter.measure().spliterator(), false)
                  .filter(m -> m.getStatistic().name().equals("VALUE"))
                  .findFirst()
                  .map(m -> m.getValue())
                  .orElse(0.0);
            System.out.println(desc + " - " + metricName + ": " + metricValue);
        };
        meterRegistry.forEachMeter(meterConsumer);
    }
    public static void main(String[] args) throws InterruptedException {
        SemaphoreBulkHead semaphoreBulkHead = new SemaphoreBulkHead();
        semaphoreBulkHead.printDefaultValues();
        System.out.println(" ======");
        semaphoreBulkHead.semaphoreWithBasicUsage();
        delay(3);
        System.out.println(" ======");
        semaphoreBulkHead.semaphoreWithException();
        delay(3);
        System.out.println(" ====== ");
        semaphoreBulkHead.semaphoreWithExceptionTurnOffTheStackTrace();
        delay(3);
        System.out.println(" ====== ");
        semaphoreBulkHead.semaphoteEvent();
        delay(5);
        System.out.println(" ====== ");
        semaphoreBulkHead.semaphoreMetrics();
        delay(15);
    }
    static void delay(int second) throws InterruptedException {
        Thread.sleep(second * 1000);
    }
}
