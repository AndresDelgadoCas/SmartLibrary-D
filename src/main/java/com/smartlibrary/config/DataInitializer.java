package com.smartlibrary.config;

import com.smartlibrary.model.Estudiante;
import com.smartlibrary.model.Libro;
import com.smartlibrary.repository.EstudianteRepository;
import com.smartlibrary.repository.LibroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadInitialData(
            EstudianteRepository estudianteRepository,
            LibroRepository libroRepository) {
        return args -> {
            if (estudianteRepository.count() == 0) {
                estudianteRepository.save(new Estudiante("Ana Gómez", "1001001001"));
                estudianteRepository.save(new Estudiante("Carlos Pérez", "1001001002"));
            }

            if (libroRepository.count() == 0) {
                libroRepository.save(new Libro("Cien años de soledad", "Gabriel García Márquez", true));
                libroRepository.save(new Libro("Clean Code", "Robert C. Martin", true));
                libroRepository.save(new Libro("El principito", "Antoine de Saint-Exupéry", true));
            }
        };
    }
}
