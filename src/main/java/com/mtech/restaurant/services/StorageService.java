package com.mtech.restaurant.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface StorageService {
    // Store a file and return its unique identifier
    String store(MultipartFile file, String filename);

    // Retrieve a file by its identifier
    Optional<Resource> loadAsResource(String filename);
}