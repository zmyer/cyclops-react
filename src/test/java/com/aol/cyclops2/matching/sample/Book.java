package com.aol.cyclops2.matching.sample;

import com.aol.cyclops2.matching.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class Book {

  private final String name;

  private final String author;


  public interface BookPatterns {

    static Pattern<Book> Name(String name) {
      return book -> Objects.equals(name, book.getName());
    }

    static Pattern<Book> Author(String author) {
      return book -> Objects.equals(author, book.getAuthor());
    }

  }

}
