package com.literalura;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    // Métodos de búsqueda específicos, si es necesario
}

