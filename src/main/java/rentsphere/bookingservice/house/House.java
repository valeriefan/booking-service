package rentsphere.bookingservice.house;

public record House(
        String code,
        String name,
        String city,
        String state,
        String photo,
        int availableUnits,
        boolean wifi,
        boolean laundry
) {
    public static House of(
            String code, String name, String city, String state, String photo,
            int availableUnits, boolean wifi, boolean laundry
    ) {
        return new House(code, name, city, state, photo, availableUnits, wifi, laundry);
    }
}