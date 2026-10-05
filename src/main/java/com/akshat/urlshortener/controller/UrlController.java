package com.akshat.urlshortener.controller;

import com.akshat.urlshortener.model.Url;
import com.akshat.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    public record CreateUrlRequest(
            @NotBlank String originalUrl,
            LocalDateTime expiresAt,
            @Positive Integer maxClicks) {}

    public record CreateUrlResponse(String shortCode, String shortUrl, String originalUrl) {}

    @PostMapping("/api/urls")
    public CreateUrlResponse create(@Valid @RequestBody CreateUrlRequest request) {
        Url url = urlService.createShortUrl(request.originalUrl(), request.expiresAt(), request.maxClicks());
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/" + url.getShortCode())
                .toUriString();
        return new CreateUrlResponse(url.getShortCode(), shortUrl, url.getOriginalUrl());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        Url url = urlService.resolveForRedirect(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(url.getOriginalUrl()).toString())
                .build();
    }
}