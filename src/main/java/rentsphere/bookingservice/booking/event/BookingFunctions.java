package rentsphere.bookingservice.booking.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import rentsphere.bookingservice.booking.domain.BookingService;

import java.util.function.Consumer;

@Configuration
public class BookingFunctions {

    private static final Logger log =
            LoggerFactory.getLogger(BookingFunctions.class);

    @Bean
    public Consumer<Flux<BookingNotifiedMessage>> notifyBooking(
            BookingService bookingService
    ) {
        return flux ->
                bookingService.consumeBookingNotifiedEvent(flux)
                        .doOnNext(booking -> log.info("The booking with id {} is notified",
                                booking.id()))
                        .subscribe();
    }
}
