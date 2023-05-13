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

/**
 * The type Ticket service.
 */
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

    /**
     * Instantiates a new Ticket service.
     *
     * @param ticketPaymentService   the ticket payment service
     * @param seatReservationService the seat reservation service
     */
    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        // Convert the array of ticket type requests into a stream
        final Stream<TicketTypeRequest> ticketTypeStream = Arrays.stream(ticketTypeRequests);

        // Initialize variables to track the total number of tickets, total price, and number of tickets per type
        Map<TicketTypeRequest.Type, Integer> totalNumberOfTicketsPerTypes = new HashMap<>();
        AtomicInteger totalNumberOfTickets = new AtomicInteger();
        AtomicInteger totalPrice = new AtomicInteger();

        // Process each ticket type request
        ticketTypeStream.forEach(ticketTypeRequest -> {
            totalNumberOfTickets.addAndGet(ticketTypeRequest.noOfTickets());
            totalNumberOfTicketsPerTypes.compute(ticketTypeRequest.type(), (type, total) -> Objects.nonNull(total)
                    ? total + ticketTypeRequest.noOfTickets() : ticketTypeRequest.noOfTickets());
            totalPrice.addAndGet(calculatePricePerRequest(ticketTypeRequest));
        });

        logger.info("Total number of adult tickets:: " + totalNumberOfTicketsPerTypes);
        logger.info("Total price:: " + totalPrice.get());

        // Validate the ticket purchase before making the payment
        validateBeforePurchasing(totalNumberOfTickets, totalNumberOfTicketsPerTypes);

        // Make the payment using the ticket payment service
        ticketPaymentService.makePayment(accountId, totalPrice.get());

        // Calculate the total number of seats to be reserved
        int totalNumberOfSeatsToBeReserved = totalNumberOfTicketsPerTypes.entrySet().stream()
                .mapToInt(totalNumberOfTicketsPerType -> Optional.ofNullable(totalNumberOfTicketsPerType.getKey())
                        .filter(type -> type != TicketTypeRequest.Type.INFANT)
                        .map(type -> totalNumberOfTicketsPerType.getValue()).orElse(0)).sum();

        logger.info("Total number of tickets:: " + totalNumberOfSeatsToBeReserved);

        // Reserve the seats using the seat reservation service
        seatReservationService.reserveSeat(accountId, totalNumberOfSeatsToBeReserved);
    }

    /**
     * Validates the ticket purchase before making the payment.
     *
     * @param totalNumberOfTickets         The total number of tickets to be purchased.
     * @param totalNumberOfTicketsPerTypes The map containing the number of tickets per ticket type.
     * @throws InvalidPurchaseException If the ticket purchase is invalid.
     */
    private void validateBeforePurchasing(AtomicInteger totalNumberOfTickets,
                                          Map<TicketTypeRequest.Type, Integer> totalNumberOfTicketsPerTypes)
            throws InvalidPurchaseException {
        // Check if the total number of tickets exceeds the maximum limit
        if (totalNumberOfTickets.get() > MAX_TICKETS_PER_PURCHASE)
            throw new InvalidPurchaseException("Maximum limit of tickets per purchase exceeded");

        // Get the number of infants and adults
        int numberOfInfants = totalNumberOfTicketsPerTypes.getOrDefault(TicketTypeRequest.Type.INFANT, 0);
        int numberOfAdults = totalNumberOfTicketsPerTypes.getOrDefault(TicketTypeRequest.Type.ADULT, 0);

        // Check if the number of infants exceeds the number of adults
        if (numberOfInfants > numberOfAdults)
            throw new InvalidPurchaseException("Number of infants cannot exceed the number of adults");

        // Check if child or infant tickets are being purchased without an adult ticket
        if ((totalNumberOfTicketsPerTypes.containsKey(TicketTypeRequest.Type.CHILD) ||
                totalNumberOfTicketsPerTypes.containsKey(TicketTypeRequest.Type.INFANT)) &&
                !totalNumberOfTicketsPerTypes.containsKey(TicketTypeRequest.Type.ADULT))
            throw new InvalidPurchaseException("Child or infant ticket cannot be purchased without an adult ticket");
    }

    /**
     * Calculates the price for the given ticket type request.
     *
     * @param ticketTypeRequest The ticket type request for which to calculate the price.
     * @return The calculated price for the ticket type request.
     */
    private int calculatePricePerRequest(TicketTypeRequest ticketTypeRequest) {
        final BigDecimal ticketRate = ticketFare.getOrDefault(ticketTypeRequest.type(), BigDecimal.ZERO);
        return ticketRate.intValue() * ticketTypeRequest.noOfTickets();
    }


}
