package uk.gov.dwp.uc.pairtest.domain;

/**
 * The type Ticket type request which is immutable.
 */
public record TicketTypeRequest(int noOfTickets, Type type) {

    /**
     * The enum Type.
     */
    public enum Type {
        /**
         * Adult type.
         */
        ADULT,
        /**
         * Child type.
         */
        CHILD,
        /**
         * Infant type.
         */
        INFANT;
    }
}

