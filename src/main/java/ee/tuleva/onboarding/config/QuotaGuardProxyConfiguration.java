package ee.tuleva.onboarding.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import static java.net.Authenticator.RequestorType.PROXY;

/**
 * http://support.quotaguard.com/support/solutions/articles/5000013914-java-quick-start-guide-quotaguard-static
 */
@Slf4j
@Configuration
public class QuotaGuardProxyConfiguration extends Authenticator {
    private String user;
    private String password;
    private String host;
    private int port;
    private ProxyAuthenticator auth;

    @Value("${proxy.url}")
    private String proxyUrlEnv;

    @Value("${proxy.nonProxyHosts}")
    private String nonProxyHosts;

    @PostConstruct
    private void initializeProxy() {
        if (this.proxyUrlEnv != null && this.proxyUrlEnv.trim().length() > 0) {
            try {
                URL proxyUrl = new URL(proxyUrlEnv);
                String authString = proxyUrl.getUserInfo();
                user = authString.split(":")[0];
                password = authString.split(":")[1];
                host = proxyUrl.getHost();
                port = proxyUrl.getPort();
                auth = new ProxyAuthenticator(user, password);
                setProxy();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Environemnt variable QUOTAGUARDSTATIC_URL is not set, not configuring proxy!");
        }
    }

    private void setProxy() {
        // https://bugs.openjdk.java.net/browse/JDK-8168839
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes","");

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", String.valueOf(port));
        System.setProperty("https.proxyHost",host);
        System.setProperty("https.proxyPort", String.valueOf(port));

        if (this.nonProxyHosts != null) {
            System.setProperty("http.nonProxyHosts", this.nonProxyHosts);
            System.setProperty("https.nonProxyHosts", this.nonProxyHosts);
        }

        Authenticator.setDefault(this.auth);
    }

    public ProxyAuthenticator getAuth(){
        return auth;
    }

    class ProxyAuthenticator extends Authenticator {

        private String user, password;

        public ProxyAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            if (PROXY.equals(getRequestorType())) {
                return new PasswordAuthentication(user, password.toCharArray());
            }

            return null;
        }
    }

}