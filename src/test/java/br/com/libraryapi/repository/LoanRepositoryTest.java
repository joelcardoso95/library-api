package br.com.libraryapi.repository;

import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("teste")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
    public void existsByBookAndNotReturnedTest() {
        // cenário
        Book book = Book.builder().author("Robert Ludlum").title("A Identidade Bourne").isbn("12345").build();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Alexandre Spezani").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        // execução
        boolean exists = loanRepository.existsByBookAndNotReturned(book);

        // validações
        assertThat(exists).isTrue();
    }
}
