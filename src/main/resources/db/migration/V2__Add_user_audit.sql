ALTER TABLE bookings
    ADD COLUMN created_by varchar(255);
ALTER TABLE bookings
    ADD COLUMN last_modified_by varchar(255);