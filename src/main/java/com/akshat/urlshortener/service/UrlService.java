package com.akshat.urlshortener.service;

import com.akshat.urlshortener.model.Url;
import com.akshat.urlshortener.repository.UrlRepository;
import com.akshat.urlshortener.util.ShortCodeGenerator;
import com.akshat.urlshortener.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator codeGenerator;
    private final UrlValidator urlValidator;

    public Url createShortUrl(String originalUrl, LocalDateTime expiresAt, Integer maxClicks) {
        String cleanUrl = originalUrl.trim();
        urlValidator.validate(cleanUrl);

        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Expiry time must be in the future");
        }

        String code;
        do {
            code = codeGenerator.generate();
        } while (urlRepository.existsByShortCode(code));

        Url url = new Url();
        url.setShortCode(code);
        url.setOriginalUrl(cleanUrl);
        url.setExpiresAt(expiresAt);
        url.setMaxClicks(maxClicks);
        return urlRepository.save(url);
    }

    public Url getByShortCode(String code) {
        return urlRepository.findByShortCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Short link not found"));
    }

    public Url resolveForRedirect(String code) {
        Url url = getByShortCode(code);

        if (url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has expired");
        }
        if (url.getMaxClicks() != null && url.getClickCount() >= url.getMaxClicks()) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link reached its click limit");
        }

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);
        return url;
    }
}