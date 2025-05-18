package com.mtech.restaurant.controllers;

import com.mtech.restaurant.domain.dtos.PhotoDto;
import com.mtech.restaurant.domain.entities.Photo;
import com.mtech.restaurant.mappers.PhotoMapper;
import com.mtech.restaurant.services.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/photos")
@Slf4j
public class PhotoController {
    private final PhotoService photoService;
    private final PhotoMapper photoMapper;

    @PostMapping
    public PhotoDto uploadPhoto(@RequestParam("file") MultipartFile file) {
        Photo savedPhoto = photoService.uploadPhoto(file);
        return photoMapper.toDto(savedPhoto);
    }

    @GetMapping("/{id:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String id) {
        return photoService
                .getPhotoAsResource(id)
                .map(photo -> ResponseEntity.ok()
                        .contentType(MediaTypeFactory.getMediaType(photo).orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(photo))
                .orElse(ResponseEntity.notFound().build());
    }
}
