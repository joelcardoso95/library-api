package br.com.libraryapi.repository;

import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

    boolean existsByBookAndNotReturned(Book book);
}
