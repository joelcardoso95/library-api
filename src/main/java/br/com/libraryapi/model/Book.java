package br.com.libraryapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private String isbn;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Loan> loans;
}
