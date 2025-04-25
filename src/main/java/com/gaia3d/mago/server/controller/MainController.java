package com.gaia3d.mago.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @Autowired
    private BuildProperties buildProperties;

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

    @RequestMapping(value = "/")
    public String mainPage(Model model) {
        String name = buildProperties.getName();
        String version = buildProperties.getVersion();
        String versionInfo = name + " v" + version;
        model.addAttribute("name", "Spring Boot");
        model.addAttribute("version", versionInfo);
        model.addAttribute("useConverting", true);
        return "index.html";
    }
}
