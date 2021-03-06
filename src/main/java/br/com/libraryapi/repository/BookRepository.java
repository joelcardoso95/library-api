package br.com.libraryapi.repository;

import br.com.libraryapi.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);
}
