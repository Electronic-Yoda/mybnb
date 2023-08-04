package domain;

import java.math.BigDecimal;

public record Amenity(Long amenity_id, String amenity_name, BigDecimal impact_on_revenue) {}
