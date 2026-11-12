package com.smartlibrary.controller;

import com.smartlibrary.dto.PrestamoRequest;
import com.smartlibrary.dto.PrestamoResponse;
import com.smartlibrary.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> registrarPrestamo(
            @Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse response = prestamoService.registrarPrestamo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponse>> consultarPrestamos() {
        return ResponseEntity.ok(prestamoService.consultarPrestamos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponse> consultarPrestamoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.consultarPrestamoPorId(id));
    }
}
