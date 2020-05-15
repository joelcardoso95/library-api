package br.com.libraryapi.service;

import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;


    public BookServiceImpl (BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book save(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BussinessException("ISBN já cadastrado.");
        }
        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> getById(Integer id) {
        return bookRepository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("book id cant be null.");
        }
        bookRepository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("book id cant be null.");
        }
        return bookRepository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> bookExample = Example.of(filter,
                                    ExampleMatcher
                                            .matching()
                                            .withIgnoreCase()
                                            .withIgnoreNullValues()
                                            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        return bookRepository.findAll(bookExample, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
}
