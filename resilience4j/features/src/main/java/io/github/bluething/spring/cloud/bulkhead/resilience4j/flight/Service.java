package io.github.bluething.spring.cloud.bulkhead.resilience4j.flight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Service {
    private final Random random;
    private final DateTimeFormatter dateTimeFormatter;

    public Service(Random random, DateTimeFormatter dateTimeFormatter) {
        this.random = random;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public List<Flight> searchFlightsTakingOneSecond(SearchRequest searchRequest) throws InterruptedException {
        Thread.sleep(1000);

        System.out.println("Searching for flights; "
                + "current time = " + LocalDateTime.now().format(dateTimeFormatter) +
                "; current thread = " + Thread.currentThread().getName());

        List<Flight> flights = Arrays.asList(new Flight("XY 765", searchRequest.getFlightDate(), searchRequest.getFrom(), searchRequest.getTo()),
                new Flight("XY 746", searchRequest.getFlightDate(), searchRequest.getFrom(), searchRequest.getTo()));

        System.out.println("Flight search successful at " + LocalDateTime.now().format(dateTimeFormatter));

        return flights;
    }

    public List<Flight> searchFlightsTakingRandomTime(SearchRequest searchRequest) throws InterruptedException {
        Thread.sleep(random.nextInt(3000));

        System.out.println("Searching for flights; "
                + "current time = " + LocalDateTime.now().format(dateTimeFormatter) +
                "; current thread = " + Thread.currentThread().getName());

        List<Flight> flights = Arrays.asList(new Flight("XY 765", searchRequest.getFlightDate(), searchRequest.getFrom(), searchRequest.getTo()),
                new Flight("XY 746", searchRequest.getFlightDate(), searchRequest.getFrom(), searchRequest.getTo()));

        System.out.println("Flight search successful at " + LocalDateTime.now().format(dateTimeFormatter));

        return flights;
    }
}
