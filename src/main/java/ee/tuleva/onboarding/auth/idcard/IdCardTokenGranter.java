package ee.tuleva.onboarding.auth.idcard;

import ee.tuleva.onboarding.auth.BeforeTokenGrantedEventPublisher;
import ee.tuleva.onboarding.auth.PersonalCodeAuthentication;
import ee.tuleva.onboarding.auth.authority.GrantedAuthorityFactory;
import ee.tuleva.onboarding.auth.principal.AuthenticatedPerson;
import ee.tuleva.onboarding.auth.principal.Person;
import ee.tuleva.onboarding.auth.principal.PrincipalService;
import ee.tuleva.onboarding.auth.session.GenericSessionStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Optional;

public class IdCardTokenGranter extends AbstractTokenGranter implements TokenGranter {

    private final GenericSessionStore sessionStore;
    private final PrincipalService principalService;
    private final GrantedAuthorityFactory grantedAuthorityFactory;
    private BeforeTokenGrantedEventPublisher beforeTokenGrantedEventPublisher;

    private static final String GRANT_TYPE = "id_card";

    public IdCardTokenGranter(AuthorizationServerTokenServices tokenServices,
                              ClientDetailsService clientDetailsService,
                              OAuth2RequestFactory requestFactory,
                              GenericSessionStore genericSessionStore,
                              PrincipalService principalService,
                              GrantedAuthorityFactory grantedAuthorityFactory,
                              ApplicationEventPublisher applicationEventPublisher) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.sessionStore = genericSessionStore;
        this.principalService = principalService;
        this.grantedAuthorityFactory = grantedAuthorityFactory;
        this.beforeTokenGrantedEventPublisher = new BeforeTokenGrantedEventPublisher(applicationEventPublisher);
    }

    @Override
    protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
        final String clientId = client.getClientId();
        if (clientId == null) {
            throw new InvalidRequestException("Unknown Client ID.");
        }

        Optional<IdCardSession> session = sessionStore.get(IdCardSession.class);
        if (!session.isPresent()) {
            return null;
        }
        IdCardSession idCardSession = session.get();

        AuthenticatedPerson authenticatedPerson = principalService.getFrom(new Person() {
            @Override
            public String getPersonalCode() {
                return idCardSession.getPersonalCode();
            }

            @Override
            public String getFirstName() {
                return idCardSession.getFirstName();
            }

            @Override
            public String getLastName() {
                return idCardSession.getLastName();
            }
        });

        Authentication userAuthentication = new PersonalCodeAuthentication<>(
                authenticatedPerson,
                idCardSession,
                grantedAuthorityFactory.from(authenticatedPerson));
        userAuthentication.setAuthenticated(true);

        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(client);
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, userAuthentication);

        beforeTokenGrantedEventPublisher.publish(oAuth2Authentication);

        return getTokenServices().createAccessToken(oAuth2Authentication);
    }
}
