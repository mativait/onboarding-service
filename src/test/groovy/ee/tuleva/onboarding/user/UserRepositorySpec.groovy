package ee.tuleva.onboarding.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

@DataJpaTest
class UserRepositorySpec extends Specification {

	@Autowired
	private TestEntityManager entityManager

	@Autowired
	private UserRepository repository

	def "persisting and findByPersonalCode() works"() {
		given:
		entityManager.persist(User.builder()
				.firstName("Erko")
				.lastName("Risthein")
				.personalCode("38501010002")
				.email("erko@risthein.ee")
				.phoneNumber("5555555")
				.active(true)
				.build())

		entityManager.flush()

		when:
		User user = repository.findByPersonalCode("38501010002").get()

		then:
		user.id != null
		user.firstName == "Erko"
		user.lastName == "Risthein"
		user.personalCode == "38501010002"
		user.email == "erko@risthein.ee"
		user.phoneNumber == "5555555"
		user.createdDate != null
		user.updatedDate != null
	}

	def "persisting a user with just personal code and email works"() {
		given:
		entityManager.persist(User.builder()
				.personalCode("38501010002")
				.email("erko@risthein.ee")
				.active(true)
				.build())

		entityManager.flush()

		when:
		User user = repository.findByPersonalCode("38501010002").get()

		then:
		user.id != null
		user.personalCode == "38501010002"
		user.email == "erko@risthein.ee"
		user.createdDate != null
		user.updatedDate != null
	}

	def "can not save null"() {
		when:
		repository.save(null)
		then:
		thrown RuntimeException
	}
}
