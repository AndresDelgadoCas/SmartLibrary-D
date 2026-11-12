package com.smartlibrary.service;

import com.smartlibrary.dto.PrestamoRequest;
import com.smartlibrary.dto.PrestamoResponse;
import com.smartlibrary.exception.BusinessConflictException;
import com.smartlibrary.exception.ResourceNotFoundException;
import com.smartlibrary.model.Estudiante;
import com.smartlibrary.model.Libro;
import com.smartlibrary.model.Prestamo;
import com.smartlibrary.repository.EstudianteRepository;
import com.smartlibrary.repository.LibroRepository;
import com.smartlibrary.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final EstudianteRepository estudianteRepository;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            LibroRepository libroRepository,
            EstudianteRepository estudianteRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional
    public PrestamoResponse registrarPrestamo(PrestamoRequest request) {
        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el estudiante con id " + request.estudianteId()));

        Libro libro = libroRepository.findById(request.libroId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el libro con id " + request.libroId()));

        if (!libro.isDisponible()
                || prestamoRepository.existsByLibroIdAndFechaDevolucionIsNull(libro.getId())) {
            throw new BusinessConflictException(
                    "El libro '" + libro.getTitulo() + "' no está disponible para préstamo.");
        }

        Prestamo prestamo = new Prestamo(estudiante, libro, LocalDate.now());
        libro.marcarComoPrestado();

        // La transacción guarda el préstamo y el cambio del libro como una sola unidad.
        Libro libroActualizado = libroRepository.save(libro);
        Prestamo prestamoGuardado = prestamoRepository.save(prestamo);

        return toResponse(prestamoGuardado);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> consultarPrestamos() {
        return prestamoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse consultarPrestamoPorId(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el préstamo con id " + id));
        return toResponse(prestamo);
    }

    private PrestamoResponse toResponse(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getEstudiante().getId(),
                prestamo.getEstudiante().getNombre(),
                prestamo.getLibro().getId(),
                prestamo.getLibro().getTitulo(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucion()
        );
    }
}
