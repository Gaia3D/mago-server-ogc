package com.gaia3d.mago.server.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Slf4j
@RestController()
@RequestMapping("/api/session")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class SessionController {

    @PutMapping("/language")
    public ResponseEntity<String> setLocale(HttpSession session, @RequestParam String locale) {
        Locale currentLocale = (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        Locale changedLocale = Locale.ENGLISH;
        log.info("Changed Locale: {} -> {}", currentLocale, changedLocale);

        session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, changedLocale);
        return ResponseEntity.ok("Success");
    }
}
