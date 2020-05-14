package br.com.libraryapi.resource;

import br.com.libraryapi.api.dto.BookDTO;
import br.com.libraryapi.api.exception.ApiErrors;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.print.attribute.standard.Media;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {
	
	static String BOOK_API = "/api/books";
	
	@Autowired
	public MockMvc mvc;

	@MockBean
	BookService bookService;

	@Test
	@DisplayName("Deve criar um livro com sucesso")
	public void createBookTest() throws Exception {

		BookDTO dto = createNewBook();
		Book savedBook = Book.builder().id(10).author("Robert Ludlum").title("A Identidade Bourne").isbn("12345").build();
		BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.post(BOOK_API)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc
		.perform(request)
		.andExpect(status().isCreated())
		.andExpect(jsonPath("id").isNotEmpty())
		.andExpect(jsonPath("title").value(dto.getTitle()))
		.andExpect(jsonPath("author").value(dto.getAuthor()))
		.andExpect(jsonPath("isbn").value(dto.getIsbn()));
	}

	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro.")
	public void createInvalidBookTest() throws Exception{
		String json = new ObjectMapper().writeValueAsString(new BookDTO());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect( status().isBadRequest() )
				.andExpect( jsonPath("errors", hasSize(3)) );
	}
	
	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já cadastrado.")
	public void createBookWithReplicatedIsbn() throws Exception {

		BookDTO bookDTO = createNewBook();
		String json = new ObjectMapper().writeValueAsString(bookDTO);
		String mensagemErro = "ISBN já cadastrado.";
		BDDMockito.given(bookService.save(Mockito.any(Book.class))).willThrow(new BussinessException(mensagemErro));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", hasSize(1)))
				.andExpect(jsonPath("errors[0]").value(mensagemErro));
	}

	@Test
	@DisplayName("Deve obter informações de um livro.")
	public void getBookDetailsTest() throws Exception {
		// cenário
		Integer id = 11;

		Book book = Book.builder().id(id).author(createNewBook().getAuthor()).title(createNewBook().getTitle()).isbn(createNewBook().getIsbn()).build();

		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

		// execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id) )
				              .accept(MediaType.APPLICATION_JSON);
		mvc.perform(request)
			.andExpect(status().isOk() )
			.andExpect(jsonPath("id").value(id))
			.andExpect(jsonPath("title").value(createNewBook().getTitle()))
			.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
			.andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
	}

	@Test
	@DisplayName("Deve retornar resource not found caso não encontre um livro.")
	public void bookNotFoundTest() throws Exception {
		// cenário
		BDDMockito.given(bookService.getById(Mockito.anyInt())).willReturn(Optional.empty());

		// execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1) )
				.accept(MediaType.APPLICATION_JSON);

		// verificações
		mvc.perform(request)
				.andExpect(status().isNotFound() );
	}

	@Test
	@DisplayName("Deve deletar um livro.")
	public void deleteBookTest() throws Exception {
		// cenário
		BDDMockito.given(bookService.getById(Mockito.anyInt())).willReturn(Optional.of(Book.builder().id(1).build()));

		// execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1) )
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Deve retornar resource not found quando não encontrar um livro para deletar.")
	public void deleteBookNotFoundTest() throws Exception {
		// cenário
		BDDMockito.given(bookService.getById(Mockito.anyInt())).willReturn(Optional.empty());

		// execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1) )
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve atualizar um livro.")
	public void updateBookTest() throws Exception {
		// cenário
		Integer id = 11;
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		Book updatingBook = Book.builder().id(11).author("some author").title("some title").isbn("12345").build();
		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(updatingBook));

		BDDMockito.given(bookService.update(updatingBook)).willReturn(Book.builder().id(id).author("Robert Ludlum").title("A Identidade Bourne").isbn("12345").build());

		// execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 11) )
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);


		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBook().getTitle()))
				.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
				.andExpect(jsonPath("isbn").value("12345"));

	}

	@Test
	@DisplayName("Deve retornar 404 caso não encontre um livro.")
	public void updateNotFoundBook() throws Exception {
		// cenário
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		BDDMockito.given(bookService.getById(Mockito.anyInt())).willReturn(Optional.empty());

		// execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1) )
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);


		mvc.perform(request)
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve filtrar Livros.")
	public void findBooksTest() throws Exception {
		// Cenário
		Integer id = 11;

		Book book = Book.builder().id(id).title(createNewBook().getTitle()).author(createNewBook().getAuthor()).isbn(createNewBook().getIsbn()).build();

		BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Book>( Arrays.asList(book), PageRequest.of(0, 100), 1) );

		String queryString = String.format("?title=%s&author=s&page=0&size=100",
				book.getTitle(), book.getAuthor());

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
																	.get(BOOK_API.concat(queryString))
																	.accept(MediaType.APPLICATION_JSON);

		// Validações
		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("content" , hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1))
				.andExpect(jsonPath("pageable.pageSize").value(100))
				.andExpect(jsonPath("pageable.pageNumber").value(0));
	}

	private BookDTO createNewBook() {
		return BookDTO.builder().author("Robert Ludlum").title("A Identidade Bourne").isbn("12345").build();
	}

}
