package br.com.libraryapi.service;

import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("Test")
public class LoanServiceTest {

    @MockBean
    private LoanRepository loanRepository;

    private LoanService loanService;

    @BeforeEach
    public void setUp() {
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo.")
    public void saveLoanTest() {
        // cenário
        Book book = Book.builder().id(11).build();
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer("Alex Spezani")
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                                .id(11)
                                .customer("Alex Spezani")
                                .book(book)
                                .loanDate(LocalDate.now())
                                .build();

        when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(loanRepository.save(savingLoan)).thenReturn(savedLoan);

        // execução
        Loan loan = loanService.save(savingLoan);

        // verificações
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar empréstimo de um livro já emprestado.")
    public void loanedBookSaveTest() {
        // cenário
        Book book = Book.builder().id(11).build();
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer("Alex Spezani")
                .loanDate(LocalDate.now())
                .build();

        when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(true);

        // execução
        Throwable exception = catchThrowable(() -> loanService.save(savingLoan));

        // verificações
        assertThat(exception).isInstanceOf(BussinessException.class).hasMessage("Book already loaned.");

        Mockito.verify(loanRepository, never()).save(savingLoan);
    }
}
