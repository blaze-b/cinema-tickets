import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public class TicketPaymentApplication {
    public static void main(String[] args) {
        TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
        SeatReservationService seatReservationService = new SeatReservationServiceImpl();
        TicketService ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(10, TicketTypeRequest.Type.ADULT);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(5, TicketTypeRequest.Type.INFANT);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(5, TicketTypeRequest.Type.CHILD);
        TicketTypeRequest ticketTypeRequest4 = new TicketTypeRequest(5, TicketTypeRequest.Type.ADULT);
        ticketService.purchaseTickets(2L, ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3,
                ticketTypeRequest4);
    }


}
