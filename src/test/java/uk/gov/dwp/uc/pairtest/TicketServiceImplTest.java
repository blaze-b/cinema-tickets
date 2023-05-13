package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPurchaseTickets_success() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(2, TicketTypeRequest.Type.ADULT);
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(1, TicketTypeRequest.Type.CHILD);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketRequest, childTicketRequest};

        // Act
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        // Assert
        Mockito.verify(ticketPaymentService).makePayment(accountId, 50);
        Mockito.verify(seatReservationService).reserveSeat(accountId, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPurchaseTickets_accountNull() {
        // Arrange
        TicketTypeRequest ticketTypeRequests = new TicketTypeRequest(2, TicketTypeRequest.Type.ADULT);

        // Act
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        ticketService.purchaseTickets(null, ticketTypeRequests);

        // Assert
        // This should throw an IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPurchaseTickets_ticketTypeRequestsNull() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest ticketTypeRequests = null;

        // Act
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        // Assert
        // This should throw an IllegalArgumentException
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_totalNumberOfTicketsExceedsMaximumLimit() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(21, TicketTypeRequest.Type.ADULT);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketRequest};

        // Act
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        // Assert
        // This should throw an InvalidPurchaseException
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_numberOfInfantsExceedsNumberOfAdults() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(1, TicketTypeRequest.Type.ADULT);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(2, TicketTypeRequest.Type.INFANT);
        TicketTypeRequest[] ticketTypeRequests = {adultTicketRequest, infantTicketRequest};

        // Act
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        // Assert
        // This should throw an InvalidPurchaseException
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_childOrInfantTicketsAreBeingPurchasedWithoutAnAdultTicket() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(1, TicketTypeRequest.Type.CHILD);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(2, TicketTypeRequest.Type.INFANT);
        TicketTypeRequest[] ticketTypeRequests = {childTicketRequest, infantTicketRequest};

        // Act
        TicketServiceImpl ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        // Assert
        // This should throw an InvalidPurchaseException

    }

}