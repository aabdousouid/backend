package com.bezkoder.spring.security.postgresql.controllers;


import com.bezkoder.spring.security.postgresql.models.Notifications;
import com.bezkoder.spring.security.postgresql.models.User;
import com.bezkoder.spring.security.postgresql.repository.UserRepository;
import com.bezkoder.spring.security.postgresql.services.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
@Slf4j
public class NotificationController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @GetMapping("/me")
    public List<Notifications> getMyNotifications(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return notificationService.getUserNotifications(userId);
    }


    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        //Long userId = getUserIdFromPrincipal(principal);
        // Optionally: verify notification belongs to userId
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/geUserNotifications")
    public List<Notifications> getNotifications(Principal principal) {
        // Assuming you can get userId from Principal
        Long userId = getUserIdFromPrincipal(principal); // implement this!
        return notificationService.getUserNotifications(userId);

    }

    @PutMapping("/markAllAsRead")
    public void markAllAsRead(){
        this.notificationService.markAllAsRead();
    }

    /*@PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }*/

    @PostMapping("/create")
    public Notifications create(@RequestBody Notifications notification) {
        User recipient = userRepository.findById(notification.getRecipient().getId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Notifications saved = notificationService.save(notification);
        simpMessagingTemplate.convertAndSendToUser(
                recipient.getUsername(), // session id or username, see note below
                "/queue/notifications",
                saved
        );

        return saved;
    }

    // Utility method: Implement according to your user details
    private Long getUserIdFromPrincipal(Principal principal) {

        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User userDetails = (User) authentication.getPrincipal();
            log.info("this is the principal : :  : "+userDetails);
        }

        log.info("No authenticated principal found.");*/



        String username = principal.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

}
