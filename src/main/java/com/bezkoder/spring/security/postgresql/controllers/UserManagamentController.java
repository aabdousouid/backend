package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserManagamentController {
    private final UserService userService;

    @GetMapping("/getUsers")
    public List<User> getUsers(){
        return this.userService.findAll();
    }

    @PatchMapping("/{id}/active")


    public ResponseEntity<User> setActive(
             @PathVariable Long id,
            @RequestParam boolean value
    ) {
        User updated = userService.setActive(id, value);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")

    public ResponseEntity<User> activate(@PathVariable Long id) {
        User updated = userService.activate(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")

    public ResponseEntity<User> deactivate(@PathVariable Long id) {
        User updated = userService.deactivate(id);
        return ResponseEntity.ok(updated);
    }
}
