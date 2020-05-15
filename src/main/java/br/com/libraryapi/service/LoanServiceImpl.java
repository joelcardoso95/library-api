package br.com.libraryapi.service;

import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;

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
}
