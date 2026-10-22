package com.itu.gestionRH.controller.notifications;

import com.itu.gestionRH.common.PageResponse;
import com.itu.gestionRH.security.AccessScope;
import com.itu.gestionRH.service.notifications.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;
    private final AccessScope accessScope;

    public NotificationController(NotificationService service, AccessScope accessScope) {
        this.service=service; this.accessScope=accessScope;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public PageResponse<NotificationService.NotificationDto> mesNotifications(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.mesNotifications(accessScope.currentUserId(), page, size);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/non-lues/count")
    public CountDto nonLues() { return new CountDto(service.nombreNonLues(accessScope.currentUserId())); }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/lue")
    public void marquerLue(@PathVariable Long id) { service.marquerLue(id, accessScope.currentUserId()); }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/lire-tout")
    public CountDto lireTout() { return new CountDto(service.marquerToutLu(accessScope.currentUserId())); }

    public record CountDto(long count) {}
}
