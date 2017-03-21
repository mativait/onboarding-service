package ee.tuleva.onboarding.mandate

import com.codeborne.security.mobileid.IdCardSignatureSession
import com.codeborne.security.mobileid.MobileIDSession
import com.codeborne.security.mobileid.MobileIdSignatureSession
import ee.tuleva.onboarding.BaseControllerSpec
import ee.tuleva.onboarding.auth.session.GenericSessionStore
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import static ee.tuleva.onboarding.mandate.MandateFixture.*
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class MandateControllerSpec extends BaseControllerSpec {

    MandateRepository mandateRepository = Mock(MandateRepository)
    MandateService mandateService = Mock(MandateService)
    GenericSessionStore sessionStore = Mock(GenericSessionStore)

    MandateController controller = new MandateController(mandateRepository, mandateService, sessionStore)

    MockMvc mvc = mockMvc(controller)

    def "save a mandate"() {
        expect:
        mvc
                .perform(post("/v1/mandates/").content(
                mapper.writeValueAsString(
                        MandateFixture.sampleCreateMandateCommand()
                ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//TODO                .andExpect(jsonPath('$.futureContributionFundIsin', is(MandateFixture.sampleMandate().futureContributionFundIsin)))

    }

    def "mobile id signature start returns the mobile id challenge code"() {
        when:
        sessionStore.get(MobileIDSession) >> dummyMobileIdSessionWithPhone("555")
        mandateService.mobileIdSign(1L, _, "555") >> new MobileIdSignatureSession(1, "1234")

        then:
        mvc
                .perform(put("/v1/mandates/1/signature/mobileId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.mobileIdChallengeCode', is("1234")))
    }

    def "mobile id signature start fails when there's no mobile id session"() {
        given:
        sessionStore.get(MobileIDSession) >> Optional.empty()

        when:
        mvc
                .perform(put("/v1/mandates/1/signature/mobileId"))
                .andReturn()

        then:
        thrown Exception
    }

    def "get mobile ID signature status returns the status code"() {
        when:
        def session = new MobileIdSignatureSession(1, "1234")
        sessionStore.get(MobileIdSignatureSession) >> Optional.of(session)
        mandateService.finalizeMobileIdSignature(1L, session) >> "SIGNATURE"

        then:
        mvc
                .perform(get("/v1/mandates/1/signature/mobileId/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.statusCode', is("SIGNATURE")))
    }

    def "id card signature start returns the hash to be signed by the client"() {
        when:
        mandateService.idCardSign(1L, _, "clientCertificate") >> new IdCardSignatureSession(1, "sigId", "asdfg")

        then:
        mvc
                .perform(put("/v1/mandates/1/signature/idCard")
                .content(mapper.writeValueAsString(sampleStartIdCardSignCommand("clientCertificate")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.hash', is("asdfg")))
    }

    def "put ID card signature status returns the status code"() {
        when:
        def session = new IdCardSignatureSession(1, "sigId", "hash")
        sessionStore.get(IdCardSignatureSession) >> Optional.of(session)
        mandateService.finalizeIdCardSignature(1L, session, "signedHash") >> "SIGNATURE"

        then:
        mvc
                .perform(put("/v1/mandates/1/signature/idCard/status")
                .content(mapper.writeValueAsString(sampleFinishIdCardSignCommand("signedHash")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.statusCode', is("SIGNATURE")))
    }

    def "getMandateFile returns mandate file"() {
        when:
        1 * mandateRepository
                .findByIdAndUser(sampleMandate().id, _) >> sampleMandate()

        then:
        mvc
                .perform(get("/v1/mandates/" + sampleMandate().id + "/file"))
                .andExpect(status().isOk())
    }

    def "getMandateFile returns not found on non existing mandate file"() {
        when:
        1 * mandateRepository
                .findByIdAndUser(sampleMandate().id, _) >> null

        then:
        mvc
                .perform(get("/v1/mandates/" + sampleMandate().id + "/file"))
                .andExpect(status().isNotFound())
    }

    private Optional<MobileIDSession> dummyMobileIdSessionWithPhone(String phone) {
        Optional.of(new MobileIDSession(0, "", "", "", "", phone))
    }

}
