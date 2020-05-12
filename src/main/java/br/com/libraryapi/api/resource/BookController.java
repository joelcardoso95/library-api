package br.com.libraryapi.api.resource;

import br.com.libraryapi.api.exception.ApiErrors;
import br.com.libraryapi.exception.BussinessException;
import br.com.libraryapi.model.Book;
import br.com.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import br.com.libraryapi.api.dto.BookDTO;

import javax.validation.Valid;

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
