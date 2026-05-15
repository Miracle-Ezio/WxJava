package com.starry.mb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}
