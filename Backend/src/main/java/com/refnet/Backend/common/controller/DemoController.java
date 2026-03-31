package com.refnet.Backend.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from protected endpoint!");
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> candidateOnly() {
        return ResponseEntity.ok("Hello Candidate!");
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> employeeOnly() {
        return ResponseEntity.ok("Hello Employee!");
    }

    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<String> hrOnly() {
        return ResponseEntity.ok("Hello HR Admin!");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Hello Admin!");
    }
}
