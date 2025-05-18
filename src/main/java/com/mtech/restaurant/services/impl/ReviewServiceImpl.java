package com.mtech.restaurant.services.impl;

import com.mtech.restaurant.domain.ReviewCreateUpdateRequest;
import com.mtech.restaurant.domain.entities.Photo;
import com.mtech.restaurant.domain.entities.Restaurant;
import com.mtech.restaurant.domain.entities.Review;
import com.mtech.restaurant.domain.entities.User;
import com.mtech.restaurant.exceptions.RestaurantNotFoundException;
import com.mtech.restaurant.exceptions.ReviewNotAllowedException;
import com.mtech.restaurant.repositories.RestaurantRepository;
import com.mtech.restaurant.services.ReviewService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final RestaurantRepository restaurantRepository;

    @Override
    public Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest createReview) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        // Check if user has already reviewed this restaurant
        boolean hasExistingReview = restaurant.getReviews().stream()
                .anyMatch(r -> r.getWrittenBy().getId().equals(author.getId()));
        if (hasExistingReview) {
            throw new ReviewNotAllowedException("User has already reviewed this restaurant");
        }
        LocalDateTime now = LocalDateTime.now();
        // Create photos
        List<Photo> photos = createReview.getPhotoIds().stream()
                .map(url -> {
                    Photo photo = new Photo();
                    photo.setUrl(url);
                    photo.setUploadDate(now);
                    return photo;
                })
                .collect(Collectors.toList());
        // Create review
        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .content(createReview.getContent())
                .rating(createReview.getRating())
                .photos(photos)
                .datePosted(now)
                .lastEdited(now)
                .writtenBy(author)
                .build();
        // Add review to restaurant
        restaurant.getReviews().add(review);
        // Update restaurant's average rating
        updateRestaurantAverageRating(restaurant);
        // Save restaurant with new review
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        // Return the newly created review
        return updatedRestaurant.getReviews().stream()
                .filter(r -> r.getDatePosted().equals(review.getDatePosted()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error retrieving created review"));
    }

    private Restaurant getRestaurantOrThrow(String restaurantId) {
        return restaurantRepository
                .findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: "));
    }

    private void updateRestaurantAverageRating(Restaurant restaurant) {
        List<Review> reviews = restaurant.getReviews();
        if (reviews.isEmpty()) {
            restaurant.setAverageRating(0.0f);
        } else {
            float averageRating = (float)
                    reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
            restaurant.setAverageRating(averageRating);
        }
    }

    @Override
    public Page<Review> getRestaurantReviews(String restaurantId, Pageable pageable) {
        // Get the restaurant or throw an exception if not found
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        // Create a list of reviews
        List<Review> reviews = new ArrayList<>(restaurant.getReviews());
        // Apply sorting based on the Pageable's Sort
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean isAscending = order.getDirection().isAscending();
            Comparator<Review> comparator =
                    switch (property) {
                        case "datePosted" -> Comparator.comparing(Review::getDatePosted);
                        case "rating" -> Comparator.comparing(Review::getRating);
                        default -> Comparator.comparing(Review::getDatePosted);
                    };
            reviews.sort(isAscending ? comparator : comparator.reversed());
        } else {
            // Default sort by date descending
            reviews.sort(Comparator.comparing(Review::getDatePosted).reversed());
        }
        // Calculate pagination boundaries
        int start = (int) pageable.getOffset();
        // Handle empty pages
        if (start >= reviews.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, reviews.size());
        }
        int end = Math.min((start + pageable.getPageSize()), reviews.size());
        // Create the page of reviews
        return new PageImpl<>(reviews.subList(start, end), pageable, reviews.size());
    }

    @Override
    public Review updateReview(
            User user, String restaurantId, String reviewId, ReviewCreateUpdateRequest updatedReview) {
        // Get the restaurant or throw an exception if not found
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        String currentUserId = user.getId();
        // Find the review and verify ownership
        List<Review> reviews = restaurant.getReviews();
        Review existingReview = reviews.stream()
                .filter(r ->
                        r.getId().equals(reviewId) && r.getWrittenBy().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        // Verify the 48-hour edit window
        if (LocalDateTime.now().isAfter(existingReview.getDatePosted().plusHours(48))) {
            throw new ReviewNotAllowedException("Review can no longer be edited (48-hour limit exceeded)");
        }
        // Update the review content
        existingReview.setContent(updatedReview.getContent());
        existingReview.setRating(updatedReview.getRating());
        existingReview.setLastEdited(LocalDateTime.now());
        // Update photos
        existingReview.setPhotos(updatedReview.getPhotoIds().stream()
                .map(url -> {
                    Photo photo = new Photo();
                    photo.setUrl(url);
                    photo.setUploadDate(LocalDateTime.now());
                    return photo;
                })
                .collect(Collectors.toList()));
        // Recalculate restaurant's average rating
        updateRestaurantAverageRating(restaurant);
        // Save and return the updated review
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return savedRestaurant.getReviews().stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error retrieving updated review"));
    }

    @Override
    public void deleteReview(String restaurantId, String reviewId) {
        // Get the restaurant or throw an exception if not found
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        // Filter out the review with the matching ID
        List<Review> filteredReviews = restaurant.getReviews().stream()
                .filter(review -> !reviewId.equals(review.getId()))
                .toList();
        // Update the restaurant's reviews
        restaurant.setReviews(filteredReviews);
        // Update the restaurant's average rating
        updateRestaurantAverageRating(restaurant);
        // Save the updated restaurant
        restaurantRepository.save(restaurant);
    }
}
