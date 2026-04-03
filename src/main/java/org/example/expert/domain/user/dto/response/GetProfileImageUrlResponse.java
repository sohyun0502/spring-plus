package org.example.expert.domain.user.dto.response;

import lombok.Getter;

import java.time.Instant;

@Getter
public class GetProfileImageUrlResponse {

    private final String url;
    private final Instant expirationTime;

    public GetProfileImageUrlResponse(String url, Instant expirationTime) {
        this.url = url;
        this.expirationTime = expirationTime;
    }
}
