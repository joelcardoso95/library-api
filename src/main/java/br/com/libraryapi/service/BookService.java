package br.com.libraryapi.service;

import br.com.libraryapi.model.Book;

import java.util.Optional;

public interface BookService {
    Book save(Book any);

    Optional<Book> getById(Integer id);

    void delete(Book book);
}
