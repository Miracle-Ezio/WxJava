package com.starry.mb.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private Long employeeId;
    private String name;
    private String avatarUrl;
    private String jobTitle;
    private String roleCode;
    private Long storeId;
}
