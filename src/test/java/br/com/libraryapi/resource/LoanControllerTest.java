package br.com.libraryapi.resource;

import br.com.libraryapi.api.dto.LoanDTO;
import br.com.libraryapi.api.dto.LoanFilterDTO;
import br.com.libraryapi.api.dto.ReturnedLoanDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Test
    @DisplayName("Deve retornar um livro.")
    public void returnBookTest() throws Exception {
        // cenário (returned = true)
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        Loan loan = Loan.builder().id(11).build();
        BDDMockito.given(loanService.getById(Mockito.anyInt()))
                .willReturn(Optional.of(loan));

        String json = new ObjectMapper().writeValueAsString(dto);

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        Mockito.verify(loanService, Mockito.times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente.")
    public void returnInexistBookTest() throws Exception {
        // cenário (returned = true)
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyInt()))
                .willReturn(Optional.empty());

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar empréstimos.")
    public void findLoansTest() throws Exception {
        // Cenário
        Integer id = 11;
        Book book = Book.builder().id(12).isbn("1234").build();
        Loan loan = createLoan();
        loan.setId(id);
        loan.setBook(book);

        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>( Arrays.asList(loan), PageRequest.of(0, 10), 1) );

        String queryString = String.format("?isbn=%s&customer=s&page=0&size=10",
                book.getIsbn(), loan.getCustomer());

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // Validações
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content" , hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    public Loan createLoan() {
        Book book = Book.builder().id(11).build();
        return Loan.builder()
                .book(book)
                .customer("Alex")
                .loanDate(LocalDate.now())
                .build();
    }
}
