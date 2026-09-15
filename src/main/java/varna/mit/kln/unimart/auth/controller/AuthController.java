package varna.mit.kln.unimart.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import varna.mit.kln.unimart.auth.dto.LoginRequestDto;
import varna.mit.kln.unimart.auth.dto.LoginResponseDto;
import varna.mit.kln.unimart.auth.dto.UserRequestDto;
import varna.mit.kln.unimart.auth.dto.UserResponseDto;
import varna.mit.kln.unimart.auth.service.AuthService;

@RestController
@RequestMapping("/api/v1/public/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto requestDto) {
        UserResponseDto responseDto = authService.registerUser(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = authService.loginUser(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
