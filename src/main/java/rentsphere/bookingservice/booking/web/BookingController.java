package rentsphere.bookingservice.booking.web;

import jakarta.validation.Valid;
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
    public Flux<Booking> getAllOrders() {
        return bookingService.getAllBookings();
    }

    @PostMapping
    public Mono<Booking> submitOrder(
            @RequestBody @Valid BookingRequest bookingRequest
    ) {
        return bookingService.submitBooking(
                bookingRequest.code(), bookingRequest.quantity()
        );
    }
}
