package br.com.libraryapi.service;

import br.com.libraryapi.api.dto.BookDTO;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.repository.BookRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        // cenário
        Book book = createNewBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when( bookRepository.save(book) ).thenReturn(Book.builder()
                                                            .id(11)
                                                            .isbn("12345")
                                                            .title("A Identidade Bourne")
                                                            .author("Robert Ludlum")
                                                            .build());

        // execução
        Book savedBook = bookService.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("12345");
        assertThat(savedBook.getTitle()).isEqualTo("A Identidade Bourne");
        assertThat(savedBook.getAuthor()).isEqualTo("Robert Ludlum");
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar cadastrar livro com ISBN duplicado")
    public void notCreateDuplicatedBookISBN() {
        // cenário
        Book book = createNewBook();
        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // verificações
       Throwable exception = Assertions.catchThrowable( () ->  bookService.save(book) );
       assertThat(exception)
               .isInstanceOf(BussinessException.class)
               .hasMessage("ISBN já cadastrado.");

       Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    private Book createNewBook() {
        return Book.builder().author("Robert Ludlum").title("A Identidade Bourne").isbn("12345").build();
    }


}
