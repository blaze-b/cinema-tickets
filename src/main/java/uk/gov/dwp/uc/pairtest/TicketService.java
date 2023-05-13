package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

/**
 * The interface Ticket service.
 */
public interface TicketService {

    /**
     * Purchase tickets for the given account ID and ticket type requests.
     *
     * @param accountId         The ID of the account making the ticket purchase.
     * @param ticketTypeRequests The array of ticket type requests.
     *
     * @throws InvalidPurchaseException If the ticket purchase is invalid or payment fails.
     */
    void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests);

}
