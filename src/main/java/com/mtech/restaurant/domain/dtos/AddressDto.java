package com.mtech.restaurant.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {
    @NotBlank(message = "StreetNumber is Required") @Pattern(regexp = "[0-9]-{1,5}-[a-zA-Z]?$", message = "Invalid Street Number") private String streetNumber;

    @NotBlank(message = "StreetNumber is Required") private String streetName;

    private String unit;

    @NotBlank(message = "city is Required") private String city;

    @NotBlank(message = "state is Required") private String state;

    @NotBlank(message = "postalCode is Required") private String postalCode;

    @NotBlank(message = "country is Required") private String country;
}
