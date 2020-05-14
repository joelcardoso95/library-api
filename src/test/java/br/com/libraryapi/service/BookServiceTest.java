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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);

    }

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

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void getByIdTest() {
        // cenário
        Integer id = 11;

        Book book = createNewBook();
        book.setId(id);
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // execução
        Optional<Book> foundBook = bookService.getById(id);

        // verificações
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio caso não encontre um livro por id.")
    public void bookNotFoundGetByIdTest() {
        // cenário
        Integer id = 11;
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // execução
        Optional<Book> book = bookService.getById(id);

        // verificações
        assertThat(book.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() {
        // cenário
        Book book = Book.builder().id(11).build();

        // execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        // verificação
        Mockito.verify(bookRepository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve lançar retornar erro ao tentar deletar um livro inexistente.")
    public void deleteInvalidBook() {
        // cenário
        Book book = new Book();

        // execução
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> bookService.delete(book));

        // verificações
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest() {
        // cenário
        Integer id = 11;
        Book updatingBook = Book.builder().id(id).build();

        Book updatedBook = createNewBook();
        updatedBook.setId(id);

        Mockito.when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        // execução
        Book book = bookService.update(updatingBook);

        // verificações
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Deve lançar retornar erro ao tentar atualizar um livro inexistente.")
    public void updateInvalidBook() {
        // cenário
        Book book = new Book();

        // execução
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> bookService.update(book));

        // verificações
        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pela propriedades passadas.")
    public void findBookTest() {
        // cenário
        Book book = createNewBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(list, pageRequest , 1);
        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // execução
        Page<Book> result = bookService.find(book, pageRequest);

        // validações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    private Book createNewBook() {
        return Book.builder().author("Robert Ludlum").title("A Identidade Bourne").isbn("12345").build();
    }


}
