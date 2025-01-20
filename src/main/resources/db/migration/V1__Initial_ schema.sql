CREATE TABLE bookings (
                       id                  BIGSERIAL PRIMARY KEY NOT NULL,
                       house_code          varchar(255) NOT NULL,
                       house_name          varchar(255),
                       house_city          varchar(255),
                       house_state         varchar(255),
                       house_photo         varchar(255),
                       quantity            integer,
                       wifi                BOOL,
                       laundry             BOOL,
                       status              varchar(255) NOT NULL,
                       created_date        timestamp NOT NULL,
                       last_modified_date  timestamp NOT NULL,
                       version             integer NOT NULL
);