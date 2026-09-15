package varna.mit.kln.unimart.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
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
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final int accessMinutes;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.security.access-minutes:15}") int accessMinutes
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessMinutes = accessMinutes;
    }

    @Override
    public UserResponseDto registerUser(UserRequestDto request) {
        if (userRepository.findByUniversityEmail(request.getUniversityEmail()).isPresent()) {
            throw new ConflictException(
                    "User already exists with email: " + request.getUniversityEmail());
        }

        User user = new User();
        user.setUniversityEmail(request.getUniversityEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());

        User saved = userRepository.save(user);
        return new UserResponseDto(saved);
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto request) {
        User user = userRepository.findByUniversityEmail(request.getUniversityEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
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

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

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
    public UserProfileResponseDto updateUserProfile(String email, UserProfileUpdateRequestDto request) {
        User user = userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        User saved = userRepository.save(user);
        return new UserProfileResponseDto(saved);
    }
}
