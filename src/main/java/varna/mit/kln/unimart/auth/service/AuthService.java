package varna.mit.kln.unimart.auth.service;

import varna.mit.kln.unimart.auth.dto.LoginRequestDto;
import varna.mit.kln.unimart.auth.dto.LoginResponseDto;
import varna.mit.kln.unimart.auth.dto.UserProfileResponseDto;
import varna.mit.kln.unimart.auth.dto.UserProfileUpdateRequestDto;
import varna.mit.kln.unimart.auth.dto.UserRequestDto;
import varna.mit.kln.unimart.auth.dto.UserResponseDto;

public interface AuthService {
    UserResponseDto registerUser(UserRequestDto requestDto);
    LoginResponseDto loginUser(LoginRequestDto requestDto);
    UserProfileResponseDto getUserProfile(String email);
    UserProfileResponseDto updateUserProfile(String email, UserProfileUpdateRequestDto requestDto);
}
