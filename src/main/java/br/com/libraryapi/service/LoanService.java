package br.com.libraryapi.service;

import br.com.libraryapi.api.dto.LoanFilterDTO;
import br.com.libraryapi.api.resource.BookController;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Integer id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filter, Pageable pageable);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);
}
