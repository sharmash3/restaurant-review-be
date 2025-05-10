package com.mtech.restaurant.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantCreateUpdateRequestDto {
    @NotBlank(message = "Restaurant Name is required")
    private String name;
    @NotBlank(message = "Restaurant Name is required")
    private String cuisineType;
    @NotBlank(message = "Restaurant Name is required")
    private String contactInformation;
    private AddressDto address;

    private OperatingHoursDto operatingHours;
    private List<String> photoIds;
}
