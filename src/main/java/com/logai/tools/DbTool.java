package com.logai.tools;

import org.springframework.stereotype.Component;

@Component
public class DbTool {
    public String run(String input) {
        return "DB: " + input;
    }
}