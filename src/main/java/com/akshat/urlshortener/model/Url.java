package com.akshat.urlshortener.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
@Getter
@Setter
@NoArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Extra 1: expiry and click limit
    private LocalDateTime expiresAt;
    private Integer maxClicks;
    private int clickCount = 0;

    // Extra 2: password protection (we'll store a hash, never plain text)
    private String passwordHash;
}