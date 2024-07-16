package com.literalura;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsumoAPI {
    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public Libro buscarLibroPorTitulo(String titulo) {
        if (libroRepository.findByTitulo(titulo) != null) {
            System.out.println("No se puede registrar el mismo libro más de una vez.");
            return null;
        }

        String url = "https://gutendex.com/books/?search=" + titulo;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JSONObject json = new JSONObject(response.getBody());
        JSONArray results = json.getJSONArray("results");

        if (results.length() > 0) {
            JSONObject libroJson = results.getJSONObject(0); // Obtener el primer resultado
            String title = libroJson.getString("title");
            JSONArray authorsArray = libroJson.getJSONArray("authors");
            JSONObject authorJson = authorsArray.getJSONObject(0); // Obtener el primer autor

            Autor autor = new Autor();
            autor.setNombre(authorJson.getString("name"));
            autor.setFechaNacimiento(authorJson.optInt("birth_year"));
            autor.setFechaFallecimiento(authorJson.optInt("death_year"));

            List<String> idiomas = new ArrayList<>();
            JSONArray languagesArray = libroJson.getJSONArray("languages");
            for (int i = 0; i < languagesArray.length(); i++) {
                idiomas.add(languagesArray.getString(i));
            }

            Libro libro = new Libro();
            libro.setTitulo(title);
            libro.setAutor(autor);
            Hibernate.initialize(libro.getIdiomas());
            libro.setNumeroDescargas(libroJson.getInt("download_count"));

            autorRepository.save(autor);
            libroRepository.save(libro);

            return libro;
        } else {
            System.out.println("Libro no encontrado.");
            return null;
        }
    }

    public List<Libro> listarLibrosRegistrados() {
        return libroRepository.findAll();
    }

    public List<Autor> listarAutoresRegistrados() {
        return autorRepository.findAll();
    }

    public List<Autor> listarAutoresVivosEnAnio(int anio) {
        return autorRepository.findAll().stream()
                .filter(autor -> autor.getFechaNacimiento() <= anio && autor.getFechaFallecimiento() >= anio)
                .collect(Collectors.toList());
    }

    public List<Libro> listarLibrosPorIdioma(String idioma) {
        return libroRepository.findByIdiomasContains(idioma);
    }

    @Transactional
    public List<Libro> buscarLibrosPorAutor(String autorNombre) {
        String url = "https://gutendex.com/books/?search=" + autorNombre;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JSONObject json = new JSONObject(response.getBody());
        JSONArray results = json.getJSONArray("results");

        List<Libro> libros = new ArrayList<>();

        for (int i = 0; i < Math.min(results.length(), 5); i++) {
            JSONObject libroJson = results.getJSONObject(i);
            String title = libroJson.getString("title");

            JSONArray authorsArray = libroJson.getJSONArray("authors");
            JSONObject authorJson = authorsArray.getJSONObject(0);

            Autor autor = new Autor();
            autor.setNombre(authorJson.getString("name"));
            autor.setFechaNacimiento(authorJson.optInt("birth_year"));
            autor.setFechaFallecimiento(authorJson.optInt("death_year"));

            List<String> idiomas = new ArrayList<>();
            JSONArray languagesArray = libroJson.getJSONArray("languages");
            for (int j = 0; j < languagesArray.length(); j++) {
                idiomas.add(languagesArray.getString(j));
            }

            Libro libro = new Libro();
            libro.setTitulo(title);
            libro.setAutor(autor);
            libro.setIdiomas(idiomas);
            libro.setNumeroDescargas(libroJson.getInt("download_count"));

            // Guardar el autor y los libros asociados
            autorRepository.save(autor);
            libroRepository.save(libro);

            // Inicializar la colección de libros del autor
            Hibernate.initialize(autor.getLibros());

            libros.add(libro);
        }

        return libros;
    }

}
