package br.com.libraryapi.api.resource;

import br.com.libraryapi.api.dto.LoanDTO;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.service.BookService;
import br.com.libraryapi.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("api/loans")
public class LoanController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Integer create(@RequestBody LoanDTO loanDTO) {
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan entity = Loan.builder().book(book)
                                    .customer(loanDTO.getCustomer())
                                    .loanDate(LocalDate.now()).build();

        entity = loanService.save(entity);

        return entity.getId();
    }
}
