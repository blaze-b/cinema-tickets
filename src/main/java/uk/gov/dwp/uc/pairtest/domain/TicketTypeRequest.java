package uk.gov.dwp.uc.pairtest.domain;

public record TicketTypeRequest(int noOfTickets, Type type) {
    private static final int MAX_TICKETS_PER_PURCHASE = 20;

    public enum Type {
        ADULT, CHILD, INFANT;
    }
}

