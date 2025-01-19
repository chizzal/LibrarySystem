package springapp.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springapp.model.Book;
import springapp.model.Person;
import springapp.services.BooksService;
import springapp.services.PeopleService;
import springapp.util.BookValidator;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
public class BooksController {

    private final BookValidator bookValidator;
    private final BooksService booksService;
    private final PeopleService peopleService;


    @Autowired
    public BooksController(BookValidator bookValidator, BooksService booksService, PeopleService peopleService) {
        this.booksService = booksService;
        this.peopleService = peopleService;
        this.bookValidator = bookValidator;
    }

    @GetMapping
    public String index(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "booksPerPage", defaultValue = "10") int booksPerPage,
            @RequestParam(name = "isSortingByYear", defaultValue = "false") boolean isSortingByYear,
            Model model) {


        List<Book> books = booksService.findAll(page, booksPerPage, isSortingByYear);

        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("booksPerPage", booksPerPage);
        model.addAttribute("isSortingByYear", isSortingByYear);

        return "books/index";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id") int id, Model model) {
        model.addAttribute("book", booksService.findOne(id));

        Integer personId = booksService.findPersonIdById(id);
        if (personId != null) {

            model.addAttribute("person", peopleService.findOne(personId));
        } else {

            model.addAttribute("person", null);
        }

        model.addAttribute("people", peopleService.findAll());

        return "books/show";
    }


    @GetMapping("/new")
    public String newBook(@ModelAttribute("book") Book book) {

        return "books/new";
    }

    @PostMapping
    public String create(@ModelAttribute("book") @Valid Book book,
                         BindingResult bindingResult) {
        bookValidator.validate(book, bindingResult);

        if (bindingResult.hasErrors()) {
            return "books/new";
        }

        booksService.save(book);

        return "redirect:books";
    }

    @PutMapping("/{id}/edit")
    public String edit(Model model, @PathVariable("id") int id) {
        model.addAttribute("book", booksService.findOne(id));

        return "books/edit";
    }

    @PatchMapping("/{id}")
    public String update(@ModelAttribute("person") @Valid Book book,
                         BindingResult bindingResult, @PathVariable("id") int id) {
        if (bindingResult.hasErrors()) {
            return "books/edit";
        }

        booksService.update(id, book);

        return "redirect:/books";
    }

    @PatchMapping("/{id}/release")
    public String release(@PathVariable("id") int id) {
        booksService.release(id);

        return "redirect:/books/{id}";
    }

    @PatchMapping("/{id}/take")
    public String take(@PathVariable("id") int id, @RequestParam("personId") int personId) {
        Person person = peopleService.findOne(personId);
        Book book = booksService.findOne(id);

        booksService.take(book.getId(), person);

        return "redirect:/books/{id}";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") int id) {
        booksService.delete(id);

        return "redirect:/books";
    }

    @GetMapping("/search")
    public String searchPage() {
        return "books/search";
    }

    @PostMapping("/search")
    public String search(@RequestParam(value = "nameStartingWith", required = false) String nameStartingWith,
                         Model model) {

        Optional<Book> foundBook = booksService.findByNameStartingWith(nameStartingWith);
        model.addAttribute("isSearchCompleted", true);

        if (foundBook.isEmpty()) {
            model.addAttribute("book", null);
        } else {
            model.addAttribute("book", foundBook.get());

            Integer foundId = booksService.findPersonIdById(foundBook.get().getId());

            if (foundId != null) {
                model.addAttribute("person", peopleService.findOne(foundId) );
            } else {
                model.addAttribute("person", null);
            }
        }

        return "books/search";
    }
}