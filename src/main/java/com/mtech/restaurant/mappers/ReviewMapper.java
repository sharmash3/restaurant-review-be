package com.mtech.restaurant.mappers;

import com.mtech.restaurant.domain.ReviewCreateUpdateRequest;
import com.mtech.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.mtech.restaurant.domain.dtos.ReviewDto;
import com.mtech.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    ReviewCreateUpdateRequest toReviewCreateUpdateRequest(ReviewCreateUpdateRequestDto dto);

    ReviewDto toDto(Review review);

}