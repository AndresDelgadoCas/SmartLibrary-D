package com.smartlibrary.model;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false)
    private boolean disponible = true;

    // Permite detectar actualizaciones concurrentes sobre el mismo libro.
    @Version
    private Long version;

    protected Libro() {
    }

    public Libro(String titulo, String autor, boolean disponible) {
        this.titulo = titulo;
        this.autor = autor;
        this.disponible = disponible;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void marcarComoPrestado() {
        this.disponible = false;
    }

    public void marcarComoDisponible() {
        this.disponible = true;
    }
}
