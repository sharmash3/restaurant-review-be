package com.mtech.restaurant.controllers;

import com.mtech.restaurant.domain.ReviewCreateUpdateRequest;
import com.mtech.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.mtech.restaurant.domain.dtos.ReviewDto;
import com.mtech.restaurant.domain.entities.Review;
import com.mtech.restaurant.domain.entities.User;
import com.mtech.restaurant.mappers.ReviewMapper;
import com.mtech.restaurant.services.ReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "Keycloak")
@RequestMapping("/api/restaurants/{restaurantId}/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable String restaurantId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review,
            @AuthenticationPrincipal Jwt jwt) {
        // Convert the review DTO to a domain object
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);
        // Extract user details from JWT
        User user = jwtToUser(jwt);
        // Create the review
        Review createdReview = reviewService.createReview(user, restaurantId, reviewCreateUpdateRequest);
        // Return the created review as DTO
        return ResponseEntity.ok(reviewMapper.toDto(createdReview));
    }

    @GetMapping
    public Page<ReviewDto> listReviews(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, page = 0, sort = "datePosted", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return reviewService.getRestaurantReviews(restaurantId, pageable).map(reviewMapper::toDto);
    }

    private User jwtToUser(Jwt jwt) {
        return new User(
                jwt.getSubject(), // User's unique ID
                jwt.getClaimAsString("preferred_username"), // Username
                jwt.getClaimAsString("given_name"), // First name
                jwt.getClaimAsString("family_name") // Last name
                );
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review,
            @AuthenticationPrincipal Jwt jwt) {
        // Convert the DTO to domain object
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);
        // Extract user information from JWT
        User user = jwtToUser(jwt);
        // Call service to perform update
        Review updatedReview = reviewService.updateReview(user, restaurantId, reviewId, reviewCreateUpdateRequest);
        // Return updated review
        return ResponseEntity.ok(reviewMapper.toDto(updatedReview));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String restaurantId, @PathVariable String reviewId) {
        reviewService.deleteReview(restaurantId, reviewId);
        return ResponseEntity.noContent().build();
    }
}
