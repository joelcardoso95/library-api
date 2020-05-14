package br.com.libraryapi.api.resource;

import br.com.libraryapi.api.exception.ApiErrors;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.service.BookService;
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
public class BookController {

	@Autowired
	private BookService bookService;

	@Autowired
	private ModelMapper modelMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		Book book = modelMapper.map(dto, Book.class);
		book = bookService.save(book);
		return modelMapper.map(book, BookDTO.class);
	}

	@GetMapping("{id}")
	public BookDTO get(@PathVariable Integer id) {
	    return bookService.getById(id)
                          .map(book -> modelMapper.map(book, BookDTO.class))
                          .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
		Book book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		bookService.delete(book);
	}

	@PutMapping("{id}")
	public BookDTO put(@PathVariable Integer id, BookDTO dto) {
        return bookService.getById(id).map(book -> {

			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = bookService.update(book);
			return modelMapper.map(book, BookDTO.class);

		}).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageable) {
		Book filter = modelMapper.map(dto, Book.class);
		Page<Book> result = bookService.find(filter, pageable);
		List<BookDTO> list = result.getContent()
				.stream()
				.map(entity -> modelMapper.map(entity, BookDTO.class))
				.collect(Collectors.toList());

		return new PageImpl<BookDTO>(list, pageable, result.getTotalElements());

	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleValidationExceptions (MethodArgumentNotValidException exception) {
		BindingResult bindingResult = exception.getBindingResult();
		return new ApiErrors(bindingResult);
	}

	@ExceptionHandler(BussinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleBusinessException (BussinessException exception) {
		return new ApiErrors(exception);
	}
}
