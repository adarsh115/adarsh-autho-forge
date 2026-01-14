package com.adarsh.autho.forge.service.service;

import com.adarsh.autho.forge.service.dto.LoginRequest;
import com.adarsh.autho.forge.service.dto.RegisterRequest;
import com.adarsh.autho.forge.service.dto.RegisterResponse;
import com.adarsh.autho.forge.service.dto.TokenResponse;
import com.adarsh.autho.forge.service.entity.AuthUser;
import com.adarsh.autho.forge.service.exception.InvalidCredentialsException;
import com.adarsh.autho.forge.service.exception.UserCreationException;
import com.adarsh.autho.forge.service.exception.UserNameAlreadyExistsException;
import com.adarsh.autho.forge.service.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import javax.swing.text.html.parser.Entity;
import java.sql.Ref;
import java.util.Optional;


//register(RegisterRequest req)
//login(LoginRequest req)
//refresh(String refreshToken)
//
//generateAccessToken(User user)
//generateRefreshToken(User user)

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public RegisterResponse register(RegisterRequest requestDto){
        //Username check
        boolean userExists = userRepository.existsByUsername(requestDto.getUsername());
        if(userExists){
            throw new UserNameAlreadyExistsException("The username '" + requestDto.getUsername() + "' is already taken.");
        }

        //Hassing password using Bcrypt
        String hashPassword = passwordEncoder.encode(requestDto.getPassword());

        AuthUser newUser = AuthUser.builder()
                .username(requestDto.getUsername())
                .passwordHash(hashPassword)
                .role(requestDto.getRoles())
                .build();
        //throwing a dataaccess exception in case new user cannot be saved
        try {
            userRepository.save(newUser);
        } catch (DataAccessException e) {
            throw new UserCreationException("Unable to register user at this time. Please try again later.");
        }

        //returning the response builded from new user
        RegisterResponse response = RegisterResponse.builder()
                .userId(newUser.getId())
                .username(newUser.getUsername())
                .message("Registration successful")
                .build();

        return response;
    }

    @Autowired
    private JwtTokenService jwtTokenService;

    public TokenResponse login(LoginRequest loginRequestDto){
        //Optional allows us to handle NPE
        Optional<AuthUser> currentUser = userRepository.findByUsername(loginRequestDto.getUsername());
        if (currentUser.isEmpty()) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), currentUser.get().getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Generate access token
        AuthUser user = currentUser.get();
        String accessToken = jwtTokenService.generateAccessToken(user);
        
        // Build token response
        TokenResponse response = TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(15 * 60L) // 15 minutes in seconds
                .build();
        
        return response;
    }

    public TokenResponse refresh(String refreshToken){
        // TODO: Implement refresh token logic
        throw new UnsupportedOperationException("Refresh not yet implemented");
    }

    public TokenResponse generateAccessToken(AuthUser user){
        //  Deprecated - use jwtTokenService directly
        throw new UnsupportedOperationException("Use jwtTokenService.generateAccessToken instead");
    }

    public TokenResponse generateRefreshToken(AuthUser user){
        // TODO: Implement refresh token generation
        throw new UnsupportedOperationException("Refresh token generation not yet implemented");
    }

}
