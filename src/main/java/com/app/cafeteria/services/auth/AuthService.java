package com.app.cafeteria.services.auth;

import com.app.cafeteria.dtos.auth.LoginRequest;
import com.app.cafeteria.dtos.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
