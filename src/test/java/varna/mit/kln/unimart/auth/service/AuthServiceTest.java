package varna.mit.kln.unimart.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import varna.mit.kln.unimart.auth.dto.*;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.entity.UserRole;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ConflictException;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtEncoder, 1440);
    }

    @Test
    void registerUser_Success() {
        UserRequestDto request = new UserRequestDto();
        request.setUniversityEmail("newstudent@kln.ac.lk");
        request.setPassword("Password123!");
        request.setFullName("New Student");
        request.setRole(UserRole.buyer);

        when(userRepository.findByUniversityEmail(request.getUniversityEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setUniversityEmail(request.getUniversityEmail());
        savedUser.setPasswordHash("encodedPassword");
        savedUser.setFullName(request.getFullName());
        savedUser.setRole(request.getRole());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("newstudent@kln.ac.lk", response.getUniversityEmail());
        assertEquals("New Student", response.getFullName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsConflictException() {
        UserRequestDto request = new UserRequestDto();
        request.setUniversityEmail("existing@kln.ac.lk");
        request.setPassword("Password123!");
        request.setFullName("Existing User");

        User existingUser = new User();
        existingUser.setUniversityEmail("existing@kln.ac.lk");

        when(userRepository.findByUniversityEmail("existing@kln.ac.lk"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(ConflictException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUniversityEmail("student@kln.ac.lk");
        request.setPassword("Password123!");

        User user = new User();
        user.setId(2);
        user.setUniversityEmail("student@kln.ac.lk");
        user.setPasswordHash("hashedPassword");
        user.setFullName("Student Seller");
        user.setRole(UserRole.seller);

        when(userRepository.findByUniversityEmail("student@kln.ac.lk"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashedPassword"))
                .thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked.jwt.token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        LoginResponseDto response = authService.loginUser(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals(1440, response.getExpiresInMinutes());
        assertEquals("student@kln.ac.lk", response.getUser().getUniversityEmail());
    }

    @Test
    void loginUser_InvalidPassword_ThrowsBadCredentialsException() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUniversityEmail("student@kln.ac.lk");
        request.setPassword("WrongPassword");

        User user = new User();
        user.setUniversityEmail("student@kln.ac.lk");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByUniversityEmail("student@kln.ac.lk"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword"))
                .thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.loginUser(request));
    }

    @Test
    void getUserProfile_Success() {
        User user = new User();
        user.setId(3);
        user.setUniversityEmail("profile@kln.ac.lk");
        user.setFullName("Profile User");
        user.setRole(UserRole.buyer);

        when(userRepository.findByUniversityEmail("profile@kln.ac.lk"))
                .thenReturn(Optional.of(user));

        UserProfileResponseDto profile = authService.getUserProfile("profile@kln.ac.lk");

        assertNotNull(profile);
        assertEquals(3, profile.getId());
        assertEquals("profile@kln.ac.lk", profile.getUniversityEmail());
        assertEquals("Profile User", profile.getFullName());
    }

    @Test
    void getUserProfile_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUniversityEmail("unknown@kln.ac.lk"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getUserProfile("unknown@kln.ac.lk"));
    }
}
