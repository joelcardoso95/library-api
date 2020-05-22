package br.com.libraryapi.service;

import br.com.libraryapi.api.dto.LoanFilterDTO;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {
        if ( loanRepository.existsByBookAndNotReturned(loan.getBook()) ) {
            throw new BussinessException("Book already loaned.");
        }
        return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Integer id) {
        return loanRepository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return loanRepository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable pageable) {
        return loanRepository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageable);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return loanRepository.findByBook(book, pageable);
    }
}
