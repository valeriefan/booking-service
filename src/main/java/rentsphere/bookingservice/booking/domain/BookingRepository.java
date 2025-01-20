package rentsphere.bookingservice.booking.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface BookingRepository
        extends ReactiveCrudRepository<Booking,Long> {}