package filter;

import java.time.LocalDate;

public record UserFilter(Long sin, String name, String address, LocalDate birthdate, String occupation) {}

