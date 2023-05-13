package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public interface TicketService {

    void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests);

}
