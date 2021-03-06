package ee.tuleva.onboarding.error

import ee.tuleva.onboarding.error.converter.ErrorAttributesConverter
import ee.tuleva.onboarding.error.response.ErrorResponseEntityFactory
import ee.tuleva.onboarding.error.converter.InputErrorsConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import javax.servlet.RequestDispatcher

import static org.hamcrest.Matchers.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest(ErrorHandlingController)
@WithMockUser
@Import([DefaultErrorAttributes, ErrorResponseEntityFactory, InputErrorsConverter, ErrorAttributesConverter])
class ErrorHandlingControllerSpec extends Specification {

	@Autowired
	MockMvc mvc

	def "error handling works"() {
		expect:
		mvc.perform(get("/error")
				.requestAttr(RequestDispatcher.ERROR_EXCEPTION, new RuntimeException())
				.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
				.requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/asdf")
				.requestAttr(RequestDispatcher.ERROR_MESSAGE, "oops!"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath('$.errors[0].code', is("RuntimeException")))
				.andExpect(jsonPath('$.errors[0].message', is("oops!")))
				.andExpect(jsonPath('$.errors[0].path').doesNotExist())
				.andExpect(jsonPath('$.errors[0].arguments').doesNotExist())
	}

}
