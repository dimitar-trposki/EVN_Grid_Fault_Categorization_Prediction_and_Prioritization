package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.NotificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.getUsername()));
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getMyUnread(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.getMyUnread(principal.getUsername()));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countMyUnread(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.countMyUnread(principal.getUsername()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails principal) {
        notificationService.markAsRead(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllMineAsRead(@AuthenticationPrincipal UserDetails principal) {
        notificationService.markAllMineAsRead(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
