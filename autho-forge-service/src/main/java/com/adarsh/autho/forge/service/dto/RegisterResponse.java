package com.adarsh.autho.forge.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private Long userId;
    private String username;
    private String message;
}
