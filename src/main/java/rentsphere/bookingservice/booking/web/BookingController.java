package rentsphere.bookingservice.booking.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.*;
import rentsphere.bookingservice.booking.domain.Booking;
import rentsphere.bookingservice.booking.domain.BookingService;

@RestController
@RequestMapping("bookings")
public class BookingController {
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public Flux<Booking> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        log.info("Fetching all bookings");
        return bookingService.getAllBookings(jwt.getSubject());
    }

    @PostMapping
    public Mono<Booking> submitBooking(
            @RequestBody @Valid BookingRequest bookingRequest
    ) {
        log.info("Booking for {} copies of the house with reference code {}",
                bookingRequest.quantity(), bookingRequest.code());
        return bookingService.submitBooking(
                bookingRequest.code(), bookingRequest.quantity()
        );
    }
}
