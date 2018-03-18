package com.konradbochnia;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class NotificationsController {

    private final NotificationsService service;

    public NotificationsController(NotificationsService service) {
        this.service = service;
    }
    
    @PostMapping("/register")
    public void register(@RequestBody String token) {
        service.subscribe("updates", token);
    }
}
