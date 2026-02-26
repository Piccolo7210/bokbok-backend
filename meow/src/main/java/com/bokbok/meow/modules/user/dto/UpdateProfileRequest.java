package com.bokbok.meow.modules.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String about;
    private String email;
}