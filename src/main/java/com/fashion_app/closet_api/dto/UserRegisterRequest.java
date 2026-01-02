package com.fashion_app.closet_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
