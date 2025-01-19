package springapp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapp.model.Book;
import springapp.model.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface BooksRepository extends JpaRepository<Book, Integer> {
    List<Book> findByOwner(Person owner);

    Page<Book> findAll(Pageable pageable);

    Optional<Book> findByNameStartingWith(String nameStartingWith);

    @Query("SELECT b.owner.id FROM Book b WHERE b.id = :id")
    Integer findPersonIdById(@Param("id") Integer id);

    Boolean existsByName(String name);

    Boolean existsByAuthor(String author);

    @Query("SELECT b FROM Book b WHERE b.name = :name AND b.author = :author")
    Book findByNameAndAuthor(@Param("name") String name, @Param("author") String author);
}