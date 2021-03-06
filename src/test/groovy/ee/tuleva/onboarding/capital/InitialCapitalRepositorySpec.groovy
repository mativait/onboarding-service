package ee.tuleva.onboarding.capital

import ee.tuleva.onboarding.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

import static ee.tuleva.onboarding.auth.UserFixture.sampleUserNonMember
import static ee.tuleva.onboarding.capital.InitialCapitalFixture.initialCapitalFixture

@DataJpaTest
class InitialCapitalRepositorySpec extends Specification {

	@Autowired
	private TestEntityManager entityManager

	@Autowired
	private InitialCapitalRepository repository

	def "persisting and findByUserId() works"() {
		given:
		User sampleUser = entityManager.persist(sampleUserNonMember().id(null).build())

		InitialCapital sampleInitialCapital = initialCapitalFixture(sampleUser).build()

		entityManager.persist(sampleInitialCapital)

		entityManager.flush()

		when:
		InitialCapital initialCapital = repository.findByUserId(sampleUser.id)

		then:
		initialCapital.id != null
		initialCapital.amount == sampleInitialCapital.amount
		initialCapital.currency == sampleInitialCapital.currency
        initialCapital.ownershipFraction == sampleInitialCapital.ownershipFraction
		initialCapital.user == sampleUser
	}

}