package io.github.bluething.spring.cloud.bulkhead.resilience4j;

import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Flight;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.SearchRequest;
import io.github.bluething.spring.cloud.bulkhead.resilience4j.flight.Service;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedThreadPoolBulkheadMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

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
    void threadPoolWithException() {
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

        for (int i = 0; i < 4; i++) {
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
    void threadPoolWithExceptionTurnOffTheStackTrace() {
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(2)
                .coreThreadPoolSize(1)
                .queueCapacity(1)
                .writableStackTraceEnabled(false)
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

        for (int i = 0; i < 4; i++) {
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
    void threadPoolBulkheadMetrics() {
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(3)
                .queueCapacity(5)
                .writableStackTraceEnabled(false)
                .build();
        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = ThreadPoolBulkheadRegistry.of(threadPoolBulkheadConfig);
        ThreadPoolBulkhead bulkhead = threadPoolBulkheadRegistry.bulkhead("flightSearchService");

        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        TaggedThreadPoolBulkheadMetrics.ofThreadPoolBulkheadRegistry(threadPoolBulkheadRegistry).bindTo(meterRegistry);

        bulkhead.getEventPublisher().onCallPermitted(event -> printMetricDetails(meterRegistry));
        bulkhead.getEventPublisher().onCallRejected(event -> printMetricDetails(meterRegistry));
        bulkhead.getEventPublisher().onCallFinished(event -> printMetricDetails(meterRegistry));

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
        ThreadPoolBulkheadSample threadPoolBulkhead = new ThreadPoolBulkheadSample();
        threadPoolBulkhead.printDefaultValue();
        System.out.println(" ====== ");
        threadPoolBulkhead.threadPoolBasicUsage();
        delay(5);
        System.out.println(" ====== ");
        threadPoolBulkhead.threadPoolWithException();
        delay(10);
        System.out.println(" ====== ");
        threadPoolBulkhead.threadPoolWithExceptionTurnOffTheStackTrace();
        delay(10);
        System.out.println(" ====== ");
        threadPoolBulkhead.threadPoolBulkheadMetrics();
        delay(15);
        System.out.println(" ====== ");
    }

    static void delay(int second) throws InterruptedException {
        Thread.sleep(1000 * second);
    }
}
