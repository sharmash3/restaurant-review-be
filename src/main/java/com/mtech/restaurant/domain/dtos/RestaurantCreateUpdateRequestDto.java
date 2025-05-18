package com.mtech.restaurant.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantCreateUpdateRequestDto {
    @NotBlank(message = "Restaurant Name is required") private String name;

    @NotBlank(message = "Restaurant Name is required") private String cuisineType;

    @NotBlank(message = "Restaurant Name is required") private String contactInformation;

    private AddressDto address;

    private OperatingHoursDto operatingHours;
    private List<String> photoIds;
}
