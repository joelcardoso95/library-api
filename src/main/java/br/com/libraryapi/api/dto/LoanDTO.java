package br.com.libraryapi.api.dto;

import br.com.libraryapi.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Integer id;

    @NotEmpty
    private String isbn;

    @NotEmpty
    private String customer;

    @NotEmpty
    private String customerEmail;

    private BookDTO book;
}
