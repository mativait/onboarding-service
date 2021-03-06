package ee.tuleva.onboarding.audit;

import ee.tuleva.onboarding.auth.principal.Person;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditServiceMonitor {

    private final AuditEventPublisher auditEventPublisher;

    @Before("execution(* ee.tuleva.onboarding.account.AccountStatementService.getAccountStatement(..)) && args(person)")
    public void logServiceAccess(Person person) {
        auditEventPublisher.publish(person.getPersonalCode(), AuditEventType.GET_ACCOUNT_STATEMENT);
    }

    @Before("execution(* ee.tuleva.onboarding.comparisons.overview.EpisAccountOverviewProvider.getAccountOverview(..)) && args(person, ..)")
    public void logCashFlowAccess(Person person) {
        auditEventPublisher.publish(person.getPersonalCode(), AuditEventType.GET_CASH_FLOWS);
    }
}
