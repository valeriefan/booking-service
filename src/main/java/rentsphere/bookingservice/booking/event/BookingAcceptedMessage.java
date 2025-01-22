package rentsphere.bookingservice.booking.event;

public record BookingAcceptedMessage(
        Long bookingId
) {
}
