package ee.tuleva.onboarding.conversion

import ee.tuleva.onboarding.BaseControllerSpec
import ee.tuleva.onboarding.auth.principal.Person
import org.springframework.http.MediaType

import static org.hamcrest.Matchers.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class ConversionControllerSpec extends BaseControllerSpec {

    UserConversionService conversionService = Mock(UserConversionService)

    ConversionController controller = new ConversionController(conversionService)

    def "Conversion: Get current user conversion"() {
        given:

        ConversionResponse conversionResponse = ConversionResponse.builder()
                .transfersComplete(true)
                .selectionComplete(true)
                .build()

        def mvc = mockMvc(controller)
        1 * conversionService.getConversion(_ as Person) >> conversionResponse

        expect:
        mvc.perform(get("/v1/me/conversion"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.transfersComplete', is(conversionResponse.transfersComplete)))
                .andExpect(jsonPath('$.selectionComplete', is(conversionResponse.selectionComplete)))
    }
}
