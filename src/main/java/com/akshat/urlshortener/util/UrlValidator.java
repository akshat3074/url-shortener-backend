package com.akshat.urlshortener.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

@Component
public class UrlValidator {

    public void validate(String rawUrl) {
        if (rawUrl.length() > 2048) {
            throw bad("URL is too long");
        }

        URI uri;
        try {
            uri = new URI(rawUrl);
        } catch (URISyntaxException e) {
            throw bad("Invalid URL format");
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw bad("Only http and https links are allowed");
        }

        String host = uri.getHost();
        if (host == null) {
            throw bad("URL must contain a valid host");
        }

        if (uri.getUserInfo() != null) {
            throw bad("URLs with username or password are not allowed");
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()) {
                throw bad("Private or local addresses are not allowed");
            }
        } catch (UnknownHostException e) {
            throw bad("Could not find that website");
        }
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}