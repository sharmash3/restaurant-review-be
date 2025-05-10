package com.mtech.restaurant.services.impl;

import com.mtech.restaurant.domain.GeoLocation;
import com.mtech.restaurant.domain.RestaurantCreateUpdateRequest;
import com.mtech.restaurant.domain.entities.Address;
import com.mtech.restaurant.domain.entities.Photo;
import com.mtech.restaurant.domain.entities.Restaurant;
import com.mtech.restaurant.exceptions.RestaurantNotFoundException;
import com.mtech.restaurant.repositories.RestaurantRepository;
import com.mtech.restaurant.services.GeoLocationService;
import com.mtech.restaurant.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final GeoLocationService geoLocationService;

    @Override
    public Restaurant createRestaurant(RestaurantCreateUpdateRequest request) {

        Address address = request.getAddress();
        GeoLocation geoLocation = geoLocationService.geoLocate(address);
        GeoPoint geoPoint = new GeoPoint(geoLocation.getLatitude(), geoLocation.getLongitude());

        List<String> photoIds = request.getPhotoIds();
        List<Photo> photos = photoIds.stream().map(photoUrl ->
                Photo.builder()
                        .url(photoUrl)
                        .uploadDate(LocalDateTime.now())
                        .build()).toList();
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .cuisineType(request.getCuisineType())
                .contactInformation(request.getContactInformation())
                .geoLocation(geoPoint)
                .photos(photos)
                .operatingHours(request.getOperatingHours())
                .averageRating(0f)
                .build();

        return restaurantRepository.save(restaurant);
    }

    @Override
    public Page<Restaurant> searchRestaurants(
            String query,
            Float minRating,
            Float latitude,
            Float longitude,
            Float radius,
            Pageable pageable) {
// If just filtering my min rating
        if (null != minRating && (null == query || query.isEmpty())) {
            return restaurantRepository.findByAverageRatingGreaterThanEqual(minRating, pageable);
        }
        // Normalize min rating to be used in other queries
        Float searchMinRating = minRating == null ? 0f : minRating;
// If there's a text, search query
        if (query != null && !query.trim().isEmpty()) {
            return restaurantRepository.findByQueryAndMinRating(query, searchMinRating, pageable);
        }
// If there's a location search
        if (latitude != null && longitude != null && radius != null) {
            return restaurantRepository.findByLocationNear(latitude, longitude, radius, pageable);
        }
// Otherwise we'll perform a non-location search
        return restaurantRepository.findAll(pageable);
    }

    @Override
    public Optional<Restaurant> getRestaurant(String id) {
// Delegate to the repository to fetch the restaurant by ID
        return restaurantRepository.findById(id);
    }

    @Override
    public Restaurant updateRestaurant(String id, RestaurantCreateUpdateRequest restaurantCreateUpdateRequest) {
        // First, verify the restaurant exists
        Restaurant existingRestaurant = getRestaurant(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant with ID does not exist: "));
        // Get new geo coordinates based on the updated address
        GeoLocation newGeoLocation = geoLocationService.geoLocate(restaurantCreateUpdateRequest.getAddress());
        GeoPoint newGeoPoint = new GeoPoint(newGeoLocation.getLatitude(), newGeoLocation.getLongitude());
        // Convert photo URLs to Photo entities
        List<Photo> photos = restaurantCreateUpdateRequest.getPhotoIds().stream().map(photoUrl ->
                Photo.builder()
                        .url(photoUrl)
                        .uploadDate(LocalDateTime.now())
                        .build()
        ).collect(Collectors.toList());
// Update all fields except averageRating
        existingRestaurant.setName(restaurantCreateUpdateRequest.getName());
        existingRestaurant.setCuisineType(restaurantCreateUpdateRequest.getCuisineType());
        existingRestaurant.setContactInformation(restaurantCreateUpdateRequest.getContactInformation());
        existingRestaurant.setAddress(restaurantCreateUpdateRequest.getAddress());
        existingRestaurant.setGeoLocation(newGeoPoint);
        existingRestaurant.setOperatingHours(restaurantCreateUpdateRequest.getOperatingHours());
        existingRestaurant.setPhotos(photos);
        return restaurantRepository.save(existingRestaurant);
    }

    @Override
    public void deleteRestaurant(String id) {
        restaurantRepository.deleteById(id);
    }

}
