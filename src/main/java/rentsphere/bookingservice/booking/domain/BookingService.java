package rentsphere.bookingservice.booking.domain;

import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rentsphere.bookingservice.booking.event.BookingAcceptedMessage;
import rentsphere.bookingservice.booking.event.BookingNotifiedMessage;
import rentsphere.bookingservice.house.House;
import rentsphere.bookingservice.house.HouseClient;

import org.slf4j.Logger;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final HouseClient houseClient;
    private final StreamBridge streamBridge;

    public BookingService(BookingRepository bookingRepository,
                          HouseClient houseClient,
                          StreamBridge streamBridge) {
        this.bookingRepository = bookingRepository;
        this.houseClient = houseClient;
        this.streamBridge = streamBridge;
    }

    public Flux<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Mono<Booking> submitBooking(String code, int quantity) {
        return houseClient.getHouseByCode(code)
                .map(house -> buildAcceptedBooking(house, quantity))
                .defaultIfEmpty(
                        buildRejectedBooking(code, quantity)
                )
                .flatMap(bookingRepository::save)
                .doOnNext(this::publishBookingAcceptedEvent);
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

    public Flux<Booking> consumeBookingNotifiedEvent(
            Flux<BookingNotifiedMessage> flux
    ) {
        return flux
                .flatMap(message ->
                        bookingRepository.findById(message.bookingId()))
                .map(this::buildNotifiedBooking)
                .flatMap(bookingRepository::save);
    }

    private Booking buildNotifiedBooking(Booking existingBooking) {
        return new Booking(
                existingBooking.id(),
                existingBooking.houseCode(),
                existingBooking.houseName(),
                existingBooking.houseCity(),
                existingBooking.houseState(),
                existingBooking.housePhoto(),
                existingBooking.quantity(),
                existingBooking.wifi(),
                existingBooking.laundry(),
                BookingStatus.NOTIFIED,
                existingBooking.createdDate(),
                existingBooking.lastModifiedDate(),
                existingBooking.version()
        );
    }

    private void publishBookingAcceptedEvent(Booking booking) {
        if (!booking.status().equals(BookingStatus.ACCEPTED)) {
            return;
        }
        var bookingAcceptedMessage =
                new BookingAcceptedMessage(booking.id());
        log.info("Sending booking accepted event with id: {}", booking.id());
        var result = streamBridge.send("acceptBooking-out-0",
                bookingAcceptedMessage);
        log.info("Result of sending data for booking with id {}: {}",
                booking.id(), result);
    }
}