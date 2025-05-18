package com.mtech.restaurant.services;

import com.mtech.restaurant.domain.RestaurantCreateUpdateRequest;
import com.mtech.restaurant.domain.entities.Restaurant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantService {

    Restaurant createRestaurant(RestaurantCreateUpdateRequest request);

    Page<Restaurant> searchRestaurants(
            String query, Float minRating, Float latitude, Float longitude, Float radius, Pageable pageable);

    Optional<Restaurant> getRestaurant(String id);
    // New update method
    Restaurant updateRestaurant(String id, RestaurantCreateUpdateRequest restaurant);

    void deleteRestaurant(String id);
}
