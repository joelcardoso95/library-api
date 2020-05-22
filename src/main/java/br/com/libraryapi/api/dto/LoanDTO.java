package br.com.libraryapi.api.dto;

import br.com.libraryapi.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Integer id;
    private String isbn;
    private String customer;
    private BookDTO book;
}
