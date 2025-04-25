package com.gaia3d.mago.server.controller;

import com.gaia3d.mago.server.configuration.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController()
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AuthController {

    private final AuthProperties authProperties;

    @GetMapping("/check")
    public ResponseEntity<?> login(@RequestParam String password) {
        if (authProperties.getPassword().equals(password)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
