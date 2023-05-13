package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class TicketServiceImpl implements TicketService {

    private final Logger logger = Logger.getLogger(TicketService.class.getName());
    private static final int MAX_TICKETS_PER_PURCHASE = 20;
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    private final Map<TicketTypeRequest.Type, BigDecimal> ticketFare = new HashMap<>() {{
        put(TicketTypeRequest.Type.INFANT, BigDecimal.ZERO);
        put(TicketTypeRequest.Type.CHILD, BigDecimal.TEN);
        put(TicketTypeRequest.Type.ADULT, BigDecimal.valueOf(20));
    }};

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        final Stream<TicketTypeRequest> ticketTypeStream = Arrays.stream(ticketTypeRequests);
        Map<TicketTypeRequest.Type, Integer> totalNumberOfTicketsPerTypes = new HashMap<>();
        AtomicInteger totalNumberOfTickets = new AtomicInteger();
        AtomicInteger totalPrice = new AtomicInteger();
        ticketTypeStream.forEach(ticketTypeRequest -> {
            totalNumberOfTickets.addAndGet(ticketTypeRequest.noOfTickets());
            totalNumberOfTicketsPerTypes.compute(ticketTypeRequest.type(), (type, total) -> Objects.nonNull(total)
                    ? total + ticketTypeRequest.noOfTickets() : ticketTypeRequest.noOfTickets());
            totalPrice.addAndGet(calculatePricePerRequest(ticketTypeRequest));
        });
        logger.info("Total number of adult tickets:: " + totalNumberOfTicketsPerTypes);
        logger.info("Total price:: " + totalPrice.get());
        validateBeforePurchasing(totalNumberOfTickets, totalNumberOfTicketsPerTypes);
        ticketPaymentService.makePayment(accountId, totalPrice.get());
        int totalSeats = totalNumberOfTicketsPerTypes.entrySet().stream().mapToInt(totalNumberOfTicketsPerType ->
                Optional.ofNullable(totalNumberOfTicketsPerType.getKey()).filter(type -> type
                        != TicketTypeRequest.Type.INFANT)
                        .map(type -> totalNumberOfTicketsPerType.getValue()).orElse(0)).sum();
        logger.info("Total number of tickets:: " + totalSeats);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private void validateBeforePurchasing(AtomicInteger totalNumberOfTickets,
                                          Map<TicketTypeRequest.Type, Integer> totalNumberOfTicketsPerTypes) {
        if (totalNumberOfTickets.get() > MAX_TICKETS_PER_PURCHASE)
            throw new InvalidPurchaseException("Maximum limit of tickets is 20.");
        int numberOfInfants = totalNumberOfTicketsPerTypes.getOrDefault(TicketTypeRequest.Type.INFANT, 0);
        int numberOfAdults = totalNumberOfTicketsPerTypes.getOrDefault(TicketTypeRequest.Type.ADULT, 0);
        if (numberOfInfants > numberOfAdults)
            throw new InvalidPurchaseException("Number of infants cannot exceed the number of adults");
        if ((totalNumberOfTicketsPerTypes.containsKey(TicketTypeRequest.Type.CHILD) ||
                totalNumberOfTicketsPerTypes.containsKey(TicketTypeRequest.Type.INFANT))
                && !totalNumberOfTicketsPerTypes.containsKey(TicketTypeRequest.Type.ADULT))
            throw new InvalidPurchaseException("Child or infant ticket cannot be purchased without an adult ticket");
    }

    private int calculatePricePerRequest(TicketTypeRequest ticketTypeRequest) {
        final BigDecimal ticketRate = ticketFare.getOrDefault(ticketTypeRequest.type(), BigDecimal.ZERO);
        return ticketRate.intValue() * ticketTypeRequest.noOfTickets();
    }

}
