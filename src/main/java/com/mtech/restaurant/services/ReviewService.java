package com.mtech.restaurant.services;

import com.mtech.restaurant.domain.ReviewCreateUpdateRequest;
import com.mtech.restaurant.domain.entities.Review;
import com.mtech.restaurant.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest review);

    Page<Review> getRestaurantReviews(String restaurantId, Pageable pageable);

    Review updateReview(User user, String restaurantId, String reviewId, ReviewCreateUpdateRequest updatedReview);

    void deleteReview(String restaurantId, String reviewId);
}
