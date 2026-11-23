package com.albalab.usuario.controller;

import com.albalab.usuario.dto.EmailRequest;
import com.albalab.usuario.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios/notificaciones")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<?> enviarEmail(@RequestBody EmailRequest request) {
        emailService.enviar(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok(Map.of("mensaje", "Email enviado correctamente"));
    }
}
