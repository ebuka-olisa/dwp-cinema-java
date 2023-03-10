package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    /**
     * Should only have private methods other than the one below.
     */

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService){
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        // Maximum of 20 tickets per purchase

        // check for the presence of an invalid purchase
        PurchaseCheckOutput invalidCheckResult = checkForInvalidPurchase(accountId, ticketTypeRequests);
        if(invalidCheckResult.getIsInvalid()){
            throw new InvalidPurchaseException(invalidCheckResult.getErrorMessage());
        }

        // Calculate the correct amount for the requested tickets
        int amountToPay = calculateAmountToPay(ticketTypeRequests);
        // make payment
        ticketPaymentService.makePayment(accountId, amountToPay);

        // Calculate the correct number of seats to reserve
        int seatsToReserve = calculateNumberOfSeatsToReserve(ticketTypeRequests);
        // make seat reservation
        seatReservationService.reserveSeat(accountId, seatsToReserve);

    }

    // Check for certain constraints before processing payment and reserving seats
    private PurchaseCheckOutput checkForInvalidPurchase(long accountId, TicketTypeRequest... ticketTypeRequests){
        int infantCount = 0;
        int childCount = 0;
        int adultCount = 0;
        for(TicketTypeRequest request : ticketTypeRequests){
            switch(request.getTicketType()){
                case INFANT:
                    infantCount += request.getNoOfTickets();
                    break;
                case CHILD:
                    childCount += request.getNoOfTickets();
                    break;
                case ADULT:
                    adultCount += request.getNoOfTickets();
                    break;
            }
        }
        int totalCount = infantCount + childCount + adultCount;
        if(totalCount == 0){
            return new PurchaseCheckOutput(true, "You have purchase a minimum of one ticket.");
        }
        else if(totalCount > 20){
            return new PurchaseCheckOutput(true, "You have requested" + totalCount + " tickets, however, only a maximum of 20 tickets that can be purchased at a time");
        }
        else if((infantCount > 0 || childCount > 0 ) && adultCount == 0) {
            // error - no adult tickets
            return new PurchaseCheckOutput(true, "Child and Infant tickets cannot be purchased without purchasing an Adult ticket");
        }
        else if(infantCount > adultCount){
            // error - infants are more than adults
            return new PurchaseCheckOutput(true, "Infant tickets cannot exceed Adult tickets because infants will be sitting on an Adult's lap.");
        }
        else if(accountId <= 0){
            // error - invalid account id
            return new PurchaseCheckOutput(true, accountId + " is not a valid account Id");
        } else{
            return new PurchaseCheckOutput(false, "");
        }
    }

    // Calculate the amount to be paid for the requested tickets
    private int calculateAmountToPay(TicketTypeRequest... ticketTypeRequests){
        int amount = 0;
        for(TicketTypeRequest request : ticketTypeRequests){
            amount += request.getTicketType().price * request.getNoOfTickets();
        }
        return amount;
    }

    // Calculate the number of seats that need to be reserved for the requested tickets
    private int calculateNumberOfSeatsToReserve(TicketTypeRequest... ticketTypeRequests){
        int seats = 0;
        for(TicketTypeRequest request : ticketTypeRequests){
            // Do not allocate seats to infants
            if(!request.getTicketType().equals(TicketTypeRequest.Type.INFANT)){
                seats += request.getNoOfTickets();
            }
        }
        return seats;
    }

}
