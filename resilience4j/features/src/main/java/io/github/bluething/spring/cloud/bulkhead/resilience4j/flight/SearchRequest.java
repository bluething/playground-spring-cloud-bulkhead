package io.github.bluething.spring.cloud.bulkhead.resilience4j.flight;

public class SearchRequest {
    private final String from;
    private final String to;
    private final String flightDate;

    public SearchRequest(String from, String to, String flightDate) {
        this.from = from;
        this.to = to;
        this.flightDate = flightDate;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFlightDate() {
        return flightDate;
    }
}
