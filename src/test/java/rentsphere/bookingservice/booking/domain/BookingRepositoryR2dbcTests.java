package rentsphere.bookingservice.booking.domain;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;
import rentsphere.bookingservice.config.DataConfig;

import java.util.Objects;

@DataR2dbcTest
@Import(DataConfig.class)
@Testcontainers
class BookingRepositoryR2dbcTests {

    @Container
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres"));

    @Autowired
    private BookingRepository bookingRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", BookingRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.getDatabaseName());
    }

    @Test
    void createRejectedOrder() {
        var rejectedBooking = BookingService.buildRejectedBooking("123456789", 3);
        StepVerifier
                .create(bookingRepository.save(rejectedBooking))
                .expectNextMatches(
                        order -> order.status().equals(BookingStatus.REJECTED))
                .verifyComplete();
    }

    @Test
    void whenCreateOrderNotAuthenticatedThenNoAuditMetadata() {
        var rejectedBooking = BookingService.buildRejectedBooking( "1234567890", 3);
        StepVerifier.create(bookingRepository.save(rejectedBooking))
                .expectNextMatches(booking -> Objects.isNull(booking.createdBy()) &&
                        Objects.isNull(booking.lastModifiedBy()))
                .verifyComplete();
    }

    @Test
    @WithMockUser("marlena")
    void whenCreateOrderAuthenticatedThenAuditMetadata() {
        var rejectedOrder = BookingService.buildRejectedBooking( "1234567890", 3);
        StepVerifier.create(bookingRepository.save(rejectedOrder))
                .expectNextMatches(booking -> booking.createdBy().equals("marlena") &&
                        booking.lastModifiedBy().equals("marlena"))
                .verifyComplete();
    }
}
