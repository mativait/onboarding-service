package ee.tuleva.onboarding.auth.idcard

import ee.tuleva.onboarding.auth.authority.GrantedAuthorityFactory
import ee.tuleva.onboarding.auth.principal.PrincipalService
import ee.tuleva.onboarding.auth.session.GenericSessionStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2RequestFactory
import org.springframework.security.oauth2.provider.TokenRequest
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import spock.lang.Specification

class IdCardTokenGranterSpec extends Specification {

    IdCardTokenGranter tokenGranter
    AuthorizationServerTokenServices authorizationServerTokenServices = Mock(AuthorizationServerTokenServices)
    ClientDetailsService clientDetailsService = Mock(ClientDetailsService)
    OAuth2RequestFactory oAuth2RequestFactory = Mock(OAuth2RequestFactory)
    GenericSessionStore genericSessionStore = Mock(GenericSessionStore)
    PrincipalService principalService = Mock(PrincipalService)
    GrantedAuthorityFactory grantedAuthorityFactory = Mock(GrantedAuthorityFactory)
    ApplicationEventPublisher applicationEventPublisher = Mock(ApplicationEventPublisher)

    def setup() {
        tokenGranter = new IdCardTokenGranter(
                authorizationServerTokenServices,
                clientDetailsService,
                oAuth2RequestFactory,
                genericSessionStore,
                principalService,
                grantedAuthorityFactory,
                applicationEventPublisher)
    }

    def "GetAccessToken: Logging in with no client id fails"() {
        when:
        tokenGranter.getAccessToken(Mock(ClientDetails), tokenRequest())
        then:
        thrown InvalidRequestException
    }

    def "GetAccessToken: Logging in with no id card session returns null"() {
        given:
        genericSessionStore.get(IdCardSession) >> Optional.empty()

        when:
        def token = tokenGranter.getAccessToken(clientDetails(), tokenRequest())
        then:
        token == null
    }

    def clientDetails() {
        return Mock(ClientDetails) {
            1 * getClientId() >> "test"
        }
    }

    def tokenRequest() {
        Mock(TokenRequest)
    }
}
