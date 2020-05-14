package br.com.libraryapi.repository;

import br.com.libraryapi.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenISBNExists() {
        // cenário
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        boolean exists = bookRepository.existsByIsbn(isbn);

       // verificação
        assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findByIdTest() {
        // cenário
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        Optional<Book> foundBook =  bookRepository.findById(book.getId());

        // verificações
        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        // cenário
        String isbn = "123";
        Book book = createNewBook(isbn);

        // execução
        Book savedBook = bookRepository.save(book);

        // verificações
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() {
        // cenário
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        Book foundBook = entityManager.find(Book.class, book.getId());
        bookRepository.delete(foundBook);

        // verificações
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }

    private Book createNewBook(String isbn) {
        return Book.builder().author("Robert Ludlum").title("A Identidade Bourne").isbn(isbn).build();
    }

}
