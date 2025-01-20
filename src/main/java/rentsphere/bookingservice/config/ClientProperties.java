package rentsphere.bookingservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "rentsphere")
public record ClientProperties(

        @NotNull
        URI catalogServiceUri
){}
