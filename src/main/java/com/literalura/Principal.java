package com.literalura;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Controller
public class Principal implements CommandLineRunner {

    @Autowired
    private ConsumoAPI consumoAPI;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) {
        int opcion;

        do {
            mostrarMenu();
            opcion = obtenerOpcionValida();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosEnAnio();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    System.out.println("Saliendo del programa.");
                    break;
                default:
                    System.out.println("Opción inválida. Por favor, ingrese un número del 1 al 6.");
                    break;
            }
        } while (opcion != 6);

        scanner.close();
    }

    private void mostrarMenu() {
        String menuFormat =
                "************************%n" +
                        "1 - Buscar libro por título%n" +
                        "2 - Listar libros registrados%n" +
                        "3 - Listar autores registrados%n" +
                        "4 - Listar autores vivos en un año específico%n" +
                        "5 - Listar libros por idioma%n" +
                        "6 - Salir%n" +
                        "************************%n" +
                        "Ingrese una opción:";

        System.out.printf(menuFormat);
    }

    private int obtenerOpcionValida() {
        while (!scanner.hasNextInt()) {
            System.out.println("Entrada no válida. Por favor, ingrese un número.");
            scanner.nextLine(); // Limpiar el buffer
        }
        return scanner.nextInt();
    }

    private void buscarLibroPorTitulo() {
        scanner.nextLine(); // Consumir el newline pendiente en el buffer

        System.out.println("Ingrese el título del libro:");
        String titulo = scanner.nextLine().trim();
        System.out.println("Buscando libro por título: " + titulo);

        Libro libro = consumoAPI.buscarLibroPorTitulo(titulo);
        if (libro != null) {
            mostrarLibro(libro);
        } else {
            System.out.println("Libro no encontrado.");
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = consumoAPI.listarLibrosRegistrados();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            libros.forEach(this::mostrarLibro);
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = consumoAPI.listarAutoresRegistrados();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            autores.forEach(this::mostrarAutor);
        }
    }

    private void listarAutoresVivosEnAnio() {
        System.out.println("Ingrese el año:");
        int anio = obtenerOpcionValida();
        List<Autor> autoresVivos = consumoAPI.listarAutoresVivosEnAnio(anio);
        if (autoresVivos.isEmpty()) {
            System.out.println("No hay autores vivos en el año " + anio + ".");
        } else {
            autoresVivos.forEach(this::mostrarAutor);
        }
    }

    private void listarLibrosPorIdioma() {
        mostrarMenuIdiomas();
        int opcionIdioma = obtenerOpcionValida();
        String codigoIdioma = obtenerCodigoIdioma(opcionIdioma);

        List<Libro> librosPorIdioma = consumoAPI.listarLibrosPorIdioma(codigoIdioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No hay libros registrados en el idioma seleccionado.");
        } else {
            librosPorIdioma.forEach(this::mostrarLibro);
        }
    }

    private void mostrarMenuIdiomas() {
        String menuFormat =
                "************************%n" +
                        "1 - Español%n" +
                        "2 - Inglés%n" +
                        "3 - Francés%n" +
                        "4 - Portugués%n" +
                        "************************%n" +
                        "Ingrese el número del idioma:";

        System.out.printf(menuFormat);
    }

    private String obtenerCodigoIdioma(int opcion) {
        switch (opcion) {
            case 1:
                return "es";
            case 2:
                return "en";
            case 3:
                return "fr";
            case 4:
                return "pt";
            default:
                throw new IllegalArgumentException("Opción de idioma inválida.");
        }
    }

    private void mostrarLibro(Libro libro) {
        System.out.println("******** LIBRO ********");
        System.out.println("Título: " + libro.getTitulo());
        System.out.println("Autor: " + libro.getAutor().getNombre());
        System.out.println("Idioma: " + String.join(", ", libro.getIdiomas()));
        System.out.println("Número de descargas: " + libro.getNumeroDescargas());
        System.out.println("************************");
    }

    private void mostrarAutor(Autor autor) {
        System.out.println("************************");
        System.out.println("Autor: " + autor.getNombre());
        System.out.println("Fecha de Nacimiento: " + autor.getFechaNacimiento());
        System.out.println("Fecha de Fallecimiento: " + autor.getFechaFallecimiento());

        List<Libro> librosAutor = consumoAPI.buscarLibrosPorAutor(autor.getNombre());
        System.out.print("Libros: [");
        librosAutor.stream()
                .limit(5)
                .map(Libro::getTitulo)
                .forEach(titulo -> System.out.print(titulo + ", "));
        System.out.println("]");
        System.out.println("************************");
    }

    @PreDestroy
    public void closeScanner() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

