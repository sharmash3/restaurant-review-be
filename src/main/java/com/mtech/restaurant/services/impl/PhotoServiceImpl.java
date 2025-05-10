package com.mtech.restaurant.services.impl;

import com.mtech.restaurant.domain.entities.Photo;
import com.mtech.restaurant.services.PhotoService;
import com.mtech.restaurant.services.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {
    private final StorageService storageService;

    @Override
    public Photo uploadPhoto(MultipartFile file) {
// Generate a unique ID for the photo
        String photoId = UUID.randomUUID().toString();
// Store the file and get its URL
        String url = storageService.store(file, photoId);
// Create and populate the photo entity
        Photo photo = new Photo();
        photo.setUrl(url);
        photo.setUploadDate(LocalDateTime.now());
        return photo;
    }

    @Override
    public Optional<Resource> getPhotoAsResource(String id) {
        return storageService.loadAsResource(id);
    }
}
