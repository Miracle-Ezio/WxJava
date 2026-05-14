package com.starry.mb.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long customerId;
    private String nickname;
    private String avatarUrl;
    private String levelCode;
    private boolean newCustomer;
}
