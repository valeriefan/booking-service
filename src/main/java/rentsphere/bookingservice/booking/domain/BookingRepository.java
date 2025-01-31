package rentsphere.bookingservice.booking.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BookingRepository
        extends ReactiveCrudRepository<Booking, Long> {
    Flux<Booking> findAllByCreatedBy(String userId);
}