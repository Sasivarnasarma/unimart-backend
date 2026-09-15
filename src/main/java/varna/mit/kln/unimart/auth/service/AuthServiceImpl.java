package varna.mit.kln.unimart.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import varna.mit.kln.unimart.auth.dto.LoginRequestDto;
import varna.mit.kln.unimart.auth.dto.LoginResponseDto;
import varna.mit.kln.unimart.auth.dto.UserProfileResponseDto;
import varna.mit.kln.unimart.auth.dto.UserProfileUpdateRequestDto;
import varna.mit.kln.unimart.auth.dto.UserRequestDto;
import varna.mit.kln.unimart.auth.dto.UserResponseDto;
import varna.mit.kln.unimart.auth.entity.User;
import varna.mit.kln.unimart.auth.repository.UserRepository;
import varna.mit.kln.unimart.common.exception.ConflictException;
import varna.mit.kln.unimart.common.exception.ResourceNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final int accessMinutes;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.security.access-minutes:1440}") int accessMinutes) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessMinutes = accessMinutes;
    }

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRequestDto requestDto) {
        if (userRepository.findByUniversityEmail(requestDto.getUniversityEmail()).isPresent()) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setUniversityEmail(requestDto.getUniversityEmail());
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setFullName(requestDto.getFullName());
        user.setRole(requestDto.getRole());
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto loginUser(LoginRequestDto requestDto) {
        User user = userRepository.findByUniversityEmail(requestDto.getUniversityEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("unimart-backend")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUniversityEmail())
                .claim("scope", user.getRole().name())
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                .build();

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponseDto(tokenValue, accessMinutes, new UserResponseDto(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(String email) {
        User user = userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return new UserProfileResponseDto(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateUserProfile(String email, UserProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setFullName(requestDto.getFullName().trim());
        User updatedUser = userRepository.save(user);
        return new UserProfileResponseDto(updatedUser);
    }
}
