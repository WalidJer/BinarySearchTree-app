package com.keyin.BinarySearchTree_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/enter-numbers")
    public String enterNumbers() {
        // forwards to src/main/resources/static/index.html
        return "forward:/index.html";
    }

    @GetMapping("/previous-trees")
    public String previousTrees() {
        // forwards to src/main/resources/static/previous.html
        return "forward:/previous.html";
    }
}