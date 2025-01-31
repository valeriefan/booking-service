package rentsphere.bookingservice.booking.web;

import jakarta.validation.Valid;
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
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public Flux<Booking> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        return bookingService.getAllBookings(jwt.getSubject());
    }

    @PostMapping
    public Mono<Booking> submitBooking(
            @RequestBody @Valid BookingRequest bookingRequest
    ) {
        return bookingService.submitBooking(
                bookingRequest.code(), bookingRequest.quantity()
        );
    }
}
