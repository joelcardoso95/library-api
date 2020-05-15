package br.com.libraryapi.resource;

import br.com.libraryapi.api.dto.LoanDTO;
import br.com.libraryapi.api.resource.LoanController;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.service.BookService;
import br.com.libraryapi.service.LoanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

    private static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar o empréstimo de um livro.")
    public void createLoanTest() throws Exception {
        // cenário
        LoanDTO dto = LoanDTO.builder().isbn("12345").customer("Alexandre Spezani").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().isbn("12345").build();
        BDDMockito.given(bookService.getBookByIsbn("12345") )
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1).customer("Alexandre Spezani")
                                        .book(book)
                                        .loanDate(LocalDate.now()).build();

        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // validações
        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar realizar empréstimo de um livro inexistente.")
    public void invalidIsbnCreateLoanTest() throws Exception {
        // cenário
        LoanDTO dto = LoanDTO.builder().isbn("12345").customer("Alexandre Spezani").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("12345") )
                .willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // validações
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));


    }

    @Test
    @DisplayName("Deve retornar erro ao tentar realizar empréstimo de um livro emprestado.")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        // cenário
        LoanDTO dto = LoanDTO.builder().isbn("12345").customer("Alexandre Spezani").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().isbn("12345").build();
        BDDMockito.given(bookService.getBookByIsbn("12345") )
                .willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BussinessException("Book already loaned."));

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // validações
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned."));


    }
}
