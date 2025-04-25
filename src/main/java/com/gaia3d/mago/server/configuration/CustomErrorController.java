package com.gaia3d.mago.server.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response) {
        if (response.getStatus() == 404) {
            return "404.html";
        }
        return "404.html";
    }

    public String getErrorPath() {
        return "/error";
    }
}
