package springapp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import springapp.model.Book;
import springapp.services.BooksService;

@Component
public class BookValidator implements Validator {
    private final BooksService booksService;

    @Autowired
    public BookValidator(BooksService booksService) {
        this.booksService = booksService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Book.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Book book = (Book) target;

        Book existingBook = booksService.findByNameAndAuthor(book.getName(), book.getAuthor());
        Integer id = book.getId();

        if (existingBook != null && existingBook.getId() != book.getId()) {
            errors.rejectValue("name", "", "This book already exists");
        }
    }
}