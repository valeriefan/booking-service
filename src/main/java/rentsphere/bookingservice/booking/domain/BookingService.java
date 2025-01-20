package rentsphere.bookingservice.booking.domain;

import reactor.core.publisher.Flux;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rentsphere.bookingservice.house.House;
import rentsphere.bookingservice.house.HouseClient;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final HouseClient houseClient;

    public BookingService(BookingRepository bookingRepository,
                          HouseClient houseClient) {
        this.bookingRepository = bookingRepository;
        this.houseClient = houseClient;
    }

    public Flux<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Mono<Booking> submitBooking(String code, int quantity) {
        return houseClient.getHouseByCode(code)
                .map(house -> buildAcceptedBooking(house, quantity))
                .defaultIfEmpty(
                        buildRejectedBooking(code, quantity)
                )
                .flatMap(bookingRepository::save);
    }

    public static Booking buildAcceptedBooking(
            House house, int quantity
    ) {
        return Booking.of(house.code(), house.name(), house.city(), house.state(),
                house.photo(), quantity, house.wifi(), house.laundry(), BookingStatus.ACCEPTED);
    }

    public static Booking buildRejectedBooking(
            String code, int quantity
    ) {
        return Booking.of(code, null, null, null, null, quantity,
                false, false, BookingStatus.REJECTED);
    }
}