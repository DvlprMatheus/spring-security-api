package com.dvlprmatheus.security.api.controller;

import com.dvlprmatheus.security.api.response.TestResponse;
import com.dvlprmatheus.security.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {
    
    @GetMapping("/test")
    public ResponseEntity<TestResponse> test() {
        log.debug("Received test request for user authentication status");
        Optional<User> currentUser = User.currentUser();
        
        if (currentUser.isPresent()) {
            log.debug("User is authenticated: {}", currentUser.get().getUsername());
            TestResponse response = TestResponse.builder()
                    .message("Authentication working correctly!")
                    .username(currentUser.get().getUsername())
                    .build();
            return ResponseEntity.ok(response);
        }
        
        log.debug("User is not authenticated");
        TestResponse response = TestResponse.builder()
                .message("User not authenticated")
                .username(null)
                .build();
        return ResponseEntity.ok(response);
    }
}

