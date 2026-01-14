package com.adarsh.autho.forge.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RefreshTokenDTO {

    private String rawToken;        // the one sent back to client
    private LocalDateTime expiresAt;
}