package br.com.libraryapi.api.resource;

import br.com.libraryapi.api.dto.LoanDTO;
import br.com.libraryapi.api.exception.ApiErrors;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.model.Loan;
import br.com.libraryapi.service.BookService;
import br.com.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import br.com.libraryapi.api.dto.BookDTO;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
@Slf4j
public class BookController {

	@Autowired
	private BookService bookService;

	@Autowired
	private LoanService loanService;

	@Autowired
	private ModelMapper modelMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create a book")
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		log.info("Creating a book for isbn: {} ", dto.getIsbn());
		Book book = modelMapper.map(dto, Book.class);
		book = bookService.save(book);
		return modelMapper.map(book, BookDTO.class);
	}

	@GetMapping("{id}")
	@ApiOperation("Find a book details by id")
	public BookDTO get(@PathVariable Integer id) {
		log.info("Get book details for book id: {} ", id);
	    return bookService.getById(id)
                          .map(book -> modelMapper.map(book, BookDTO.class))
                          .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Delete a book by id")
    public void delete(@PathVariable Integer id) {
		log.info("Delete book of id: {} ", id);
		Book book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		bookService.delete(book);
	}

	@PutMapping("{id}")
	@ApiOperation("Update a book by id")
	public BookDTO update(@PathVariable Integer id, @RequestBody @Valid BookDTO dto) {
		log.info("Update book of id: {} ", id);
        return bookService.getById(id).map(book -> {

			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = bookService.update(book);
			return modelMapper.map(book, BookDTO.class);

		}).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
	@ApiOperation("Get all books")
    public Page<BookDTO> find(BookDTO dto, Pageable pageable) {
		log.info("Get All books");
		Book filter = modelMapper.map(dto, Book.class);
		Page<Book> result = bookService.find(filter, pageable);
		List<BookDTO> list = result.getContent()
				.stream()
				.map(entity -> modelMapper.map(entity, BookDTO.class))
				.collect(Collectors.toList());

		return new PageImpl<BookDTO>(list, pageable, result.getTotalElements());

	}

	@GetMapping("{id}/loans")
	@ApiOperation("Get all loans from a book by id")
	public Page<LoanDTO> loansByBook(@PathVariable Integer id, Pageable pageable) {
		log.info("Get All loans from id: {} ", id);
		Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Page<Loan> result = loanService.getLoansByBook(book, pageable);
		List<LoanDTO> list = result.getContent()
				.stream()
				.map(loan -> {
					Book loanBook = loan.getBook();
					BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
					LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
					loanDTO.setBook(bookDTO);
					return loanDTO;
				}).collect(Collectors.toList());

		return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
	}
}
