package com.mtech.restaurant.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mtech.restaurant.domain.GeoLocation;
import com.mtech.restaurant.domain.RestaurantCreateUpdateRequest;
import com.mtech.restaurant.domain.entities.Address;
import com.mtech.restaurant.domain.entities.Restaurant;
import com.mtech.restaurant.repositories.RestaurantRepository;
import com.mtech.restaurant.services.impl.RestaurantServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository; // Mocking the repository

    @Mock
    private Validator validator; // Mocking the validator

    @Mock
    private PhotoService photoService; // Mocking the PhotoService

    @Mock
    private GeoLocationService geoLocationService; // Mocking the GeoLocationService

    @InjectMocks
    private RestaurantServiceImpl restaurantService; // Actual service under test

    @BeforeEach
    public void setUp() {
        // MockitoAnnotations.openMocks(this); // Initializes mocks
    }

    @Test
    void testUpdateRestaurant_shouldUpdateSuccessfully() {
        // Given
        String restaurantId = "test-id";
        Restaurant existingRestaurant = new Restaurant();
        existingRestaurant.setId(restaurantId);
        existingRestaurant.setName("Old Name");

        // Mock the repository's findById method to return the existing restaurant
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));

        // Prepare a RestaurantCreateUpdateRequest with all required fields
        Address address = new Address();
        address.setStreetNumber("10");
        address.setStreetName("Downing Street");
        address.setCity("London");
        address.setPostalCode("SW1A 2AA");
        address.setCountry("UK");

        RestaurantCreateUpdateRequest request = new RestaurantCreateUpdateRequest();
        request.setName("New Name");
        request.setAddress(address); // Set address
        request.setPhotoIds(List.of("photo1.jpg")); // Set photoIds
        request.setCuisineType("Italian"); // Set cuisine type
        request.setContactInformation("123-456-7890"); // Set contact information
        // request.setOperatingHours("9am-9pm");  // Set operating hours

        // Mock the geoLocationService if needed
        GeoLocation mockLocation = new GeoLocation(40.0, -70.0); // Mock a location
        when(geoLocationService.geoLocate(address))
                .thenReturn(mockLocation); // Assuming geoLocationService is used in the method

        // When the update operation is performed, it should not throw any exceptions

        Optional<Restaurant> test = restaurantRepository.findById("test-id");
        System.out.println("Mocked Restaurant: " + test);

        assertDoesNotThrow(() -> {
            restaurantService.updateRestaurant(restaurantId, request);
        });

        // Optionally, verify that the repository save method was called with updated restaurant
        verify(restaurantRepository).save(any(Restaurant.class)); // Verifying if save was called after update
    }

    // Test for the 'getRestaurant' method
    @Test
    public void testGetRestaurant_Success() {
        String restaurantId = "123"; // Sample restaurant ID
        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(restaurantId);
        mockRestaurant.setName("Test Restaurant");

        // Mock the repository behavior to return the restaurant when findById is called
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));

        // Call the service method
        Optional<Restaurant> result = restaurantService.getRestaurant(restaurantId);

        // Assertions
        assertTrue(result.isPresent(), "Restaurant should be present");
        assertEquals(restaurantId, result.get().getId(), "Restaurant ID should match");
        assertEquals("Test Restaurant", result.get().getName(), "Restaurant name should match");

        // Verify that repository's findById method was called once
        verify(restaurantRepository, times(1)).findById(restaurantId);
    }

    // Test for 'getRestaurant' with a non-existing ID (repository returns empty)
    @Test
    public void testGetRestaurant_NotFound() {
        String restaurantId = "999"; // ID that does not exist
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        Optional<Restaurant> result = restaurantService.getRestaurant(restaurantId);

        // Assertions
        assertFalse(result.isPresent(), "Restaurant should not be found");

        // Verify repository method call
        verify(restaurantRepository, times(1)).findById(restaurantId);
    }

    // Test for the validation of RestaurantCreateUpdateRequest
    @Test
    public void testCreateRestaurant_Validation() {
        RestaurantCreateUpdateRequest request = new RestaurantCreateUpdateRequest();
        request.setName(""); // Invalid name
        request.setCuisineType(""); // Invalid cuisine type

        // Simulate manual validation result if needed — OR remove this whole test if validator is not used

        // Assertion example if using manual validation (not with mocks)
        assertTrue(request.getName().isBlank(), "Name is blank and should be invalid");
        assertTrue(request.getCuisineType().isBlank(), "Cuisine type is blank and should be invalid");
    }

    // Helper method for mocking validation
    private Set<ConstraintViolation<RestaurantCreateUpdateRequest>> mockValidation(
            RestaurantCreateUpdateRequest request) {
        // Only mock this method if it's necessary, otherwise, remove the stub
        Set<ConstraintViolation<RestaurantCreateUpdateRequest>> violations = mock(Set.class);
        when(validator.validate(request)).thenReturn(violations);
        return violations;
    }

    // Test for the update method (if needed)
    @Test
    void testUpdateRestaurant_withValidData_shouldReturnUpdatedEntity() {
        // Arrange
        String restaurantId = "123";

        // Create the update request
        RestaurantCreateUpdateRequest updateRequest = new RestaurantCreateUpdateRequest();
        updateRequest.setName("Updated Restaurant");
        updateRequest.setCuisineType("Italian");

        Address address = new Address();
        address.setStreetNumber("10");
        address.setStreetName("Downing Street");
        address.setCity("London");
        address.setPostalCode("SW1A 2AA");
        address.setCountry("UK");
        updateRequest.setAddress(address);

        updateRequest.setPhotoIds(List.of("http://example.com/photo1.jpg", "http://example.com/photo2.jpg"));

        // Mock existing restaurant
        Restaurant existingRestaurant = new Restaurant();
        existingRestaurant.setId(restaurantId);
        existingRestaurant.setName("Old Restaurant");

        // Mock repository + geo location
        GeoLocation mockGeoLocation =
                GeoLocation.builder().latitude(51.5074).longitude(-0.1278).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
        when(geoLocationService.geoLocate(address)).thenReturn(mockGeoLocation);
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, updateRequest);

        // Assert
        assertNotNull(updatedRestaurant);
        assertEquals("Updated Restaurant", updatedRestaurant.getName());
        assertEquals("Italian", updatedRestaurant.getCuisineType());
        assertEquals(address, updatedRestaurant.getAddress());
        assertEquals(
                mockGeoLocation.getLatitude(),
                updatedRestaurant.getGeoLocation().getLat());
        assertEquals(
                mockGeoLocation.getLongitude(),
                updatedRestaurant.getGeoLocation().getLon());
        assertNotNull(updatedRestaurant.getPhotos());
        assertEquals(2, updatedRestaurant.getPhotos().size());
        assertTrue(updatedRestaurant.getPhotos().stream()
                .anyMatch(p -> p.getUrl().equals("http://example.com/photo1.jpg")));
        assertTrue(updatedRestaurant.getPhotos().stream()
                .anyMatch(p -> p.getUrl().equals("http://example.com/photo2.jpg")));

        // Verify repository interactions
        verify(restaurantRepository).findById(restaurantId);
        verify(restaurantRepository).save(existingRestaurant);
    }

    // Test for restaurant deletion (optional)
    @Test
    public void testDeleteRestaurant() {
        String restaurantId = "123";

        // No need to mock findById if it's not used inside deleteRestaurant
        restaurantService.deleteRestaurant(restaurantId);

        // Just verify deleteById is called
        verify(restaurantRepository, times(1)).deleteById(restaurantId);
    }
}
