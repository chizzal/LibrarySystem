package springapp.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springapp.model.Book;
import springapp.model.Person;
import springapp.repositories.BooksRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BooksService {

    private final BooksRepository booksRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public BooksService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public List<Book> findAll(int page, int booksPerPage, boolean isSortByYear) {
        String sortingBy;

        if (isSortByYear) {
            sortingBy = "year";
        } else {
            sortingBy = "id";
        }
        Pageable pageable = PageRequest.of(page, booksPerPage, Sort.by(sortingBy));

        return booksRepository.findAll(pageable).getContent();
    }

    public Book findOne(int id) {
        Optional<Book> foundBook = booksRepository.findById(id);

        return foundBook.orElse(null);
    }

    public List<Book> findByOwner(Person owner) {
        List<Book> books = booksRepository.findByOwner(owner);
        checkOverdue(books);
        return books;
    }

    public Integer findPersonIdById(int id) {
        return booksRepository.findPersonIdById(id);
    }

    public Boolean existsByName(String name) {
        return booksRepository.existsByName(name);
    }

    public Boolean existsByAuthor(String author) {
        return booksRepository.existsByAuthor(author);
    }

    public Optional<Book> findByNameStartingWith(String nameStartingWith) {
        return booksRepository.findByNameStartingWith(nameStartingWith);
    }

    public Book findByNameAndAuthor(String name, String author) {
        return booksRepository.findByNameAndAuthor(name, author);
    }

    @Transactional
    public void save(Book book) {
        booksRepository.save(book);
    }

    @Transactional
    public void update(int id, Book updatedBook) {
        updatedBook.setId(id);
        booksRepository.save(updatedBook);
    }

    public void checkOverdue(List<Book> books) {
        final long OVERDUE_LIMIT = 864000000;

        Date currentDate = new Date();

        for (Book book : books) {

            if (book.getBorrowDate() == null) {
                book.setOverdue(null);
            } else if (currentDate.getTime() - book.getBorrowDate().getTime() > OVERDUE_LIMIT) {
                book.setOverdue(true);
            } else {
                book.setOverdue(false);
            }
        }
    }

    @Transactional
    public void release(int id) {
        entityManager.createQuery("UPDATE Book b SET b.owner = null WHERE b.id = :id")
                .setParameter("id", id)
                .executeUpdate();

        Book releasedBook = booksRepository.findById(id).get();
        releasedBook.setBorrowDate(null);
        releasedBook.setOverdue(null);
    }

    @Transactional
    public void take(int id, Person newOwner) {
        entityManager.createQuery("UPDATE Book b SET b.owner = :owner WHERE b.id = :id")
                .setParameter("owner", newOwner)
                .setParameter("id", id)
                .executeUpdate();

        booksRepository.findById(id).get().setBorrowDate(new Date());
    }

    @Transactional
    public void delete(int id) {
        booksRepository.deleteById(id);
    }
}