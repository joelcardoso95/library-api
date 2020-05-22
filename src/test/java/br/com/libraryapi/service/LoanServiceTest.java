package br.com.libraryapi.service;

import br.com.libraryapi.api.dto.LoanFilterDTO;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo id.")
    public void getLoanDetailsTest() {
        // cenário
        Integer id = 11;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        // execução
        Optional<Loan> result = loanService.getById(id);

        // verificações
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(loanRepository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo.")
    public void updateLoanTest() {
        // cenário
        Integer id = 11;
        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        Mockito.when(loanRepository.save(loan)).thenReturn(loan);

        // execução
        Loan updatedLoan = loanService.update(loan);

        // verificações
        assertThat(updatedLoan.getReturned()).isTrue();
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pela propriedades passadas.")
    public void findLoansTest() {
        // cenário
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Alex").isbn("321").build();

        Loan loan = createLoan();
        loan.setId(12);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(list, pageRequest , list.size());

        Mockito.when(loanRepository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // execução
        Page<Loan> result = loanService.find(loanFilterDTO, pageRequest);

        // validações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public Loan createLoan() {
        Book book = Book.builder().id(11).build();
        return Loan.builder()
                .book(book)
                .customer("Alex Spezani")
                .loanDate(LocalDate.now())
                .build();
    }
}
