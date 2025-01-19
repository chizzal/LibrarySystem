package springapp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import springapp.model.Person;
import springapp.services.PeopleService;

@Component
public class PersonValidator implements Validator {

    private final PeopleService peopleService;

    @Autowired
    public PersonValidator(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;

        Person existingPerson = peopleService.findOne(person.getName());

        if (existingPerson != null && existingPerson.getId() != person.getId()) {
            errors.rejectValue("name", "", "Человек с таким ФИО уже существует");
        }
    }
}
