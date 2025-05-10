package com.mtech.restaurant.services;

import com.mtech.restaurant.domain.GeoLocation;
import com.mtech.restaurant.domain.entities.Address;

public interface GeoLocationService {
    GeoLocation geoLocate(Address address);
}
