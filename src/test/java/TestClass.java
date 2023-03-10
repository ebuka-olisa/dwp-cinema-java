import org.junit.Before;
import org.junit.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.*;

public class TestClass {
    private TicketService ticketService;
    private TicketPaymentService paymentService;
    private SeatReservationService seatReservationService;

    @Before
    public void setup() {
        paymentService = mock(TicketPaymentServiceImpl.class);
        seatReservationService = mock(SeatReservationServiceImpl.class);
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testOnlyInfantAndChildTickets() {
        TicketTypeRequest[] requests = new TicketTypeRequest[2];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        ticketService.purchaseTickets(1L, requests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testOnlyInfantTickets() {
        TicketTypeRequest[] requests = new TicketTypeRequest[1];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        ticketService.purchaseTickets(1L, requests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testOnlyChildTickets() {
        TicketTypeRequest[] requests = new TicketTypeRequest[1];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        ticketService.purchaseTickets(1L, requests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testMoreInfantsThanAdultTickets() {
        TicketTypeRequest[] requests = new TicketTypeRequest[3];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        ticketService.purchaseTickets(1L, requests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testMoreThan20Tickets() {
        TicketTypeRequest[] requests = new TicketTypeRequest[3];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 8);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 11);
        ticketService.purchaseTickets(1L, requests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidAccountId() {
        TicketTypeRequest[] requests = new TicketTypeRequest[3];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 6);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 6);
        ticketService.purchaseTickets(0L, requests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testNoTicket() {
        TicketTypeRequest[] requests = new TicketTypeRequest[0];
        ticketService.purchaseTickets(10L, requests);
    }

    @Test
    public void testOnlyAdultTicket() {
        TicketTypeRequest[] requests = new TicketTypeRequest[1];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 6);
        ticketService.purchaseTickets(10L, requests);
        verify(paymentService).makePayment(10L, 120);
        verify(seatReservationService).reserveSeat(10L, 6);
    }

    @Test
    public void testAdultAndChildTicketsNoProblems() {
        TicketTypeRequest[] requests = new TicketTypeRequest[2];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 6);
        ticketService.purchaseTickets(10L, requests);
        verify(paymentService).makePayment(10L, 150);
        verify(seatReservationService).reserveSeat(10L, 9);
    }

    @Test
    public void testInfantTicketsChildTicketsAndAdultTicketsNoProblems() {
        TicketTypeRequest[] requests = new TicketTypeRequest[3];
        requests[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 4);
        requests[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        requests[2] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 6);
        ticketService.purchaseTickets(10L, requests);
        verify(paymentService).makePayment(10L, 150);
        verify(seatReservationService).reserveSeat(10L, 9);
    }
}
