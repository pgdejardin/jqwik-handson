package fr.xebia.jqwik.exercise4;

import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

class AlertRoutingServiceTest {

    private static final Country ITALY = new Country("IT");
    private static final Country USA = new Country("US");
    private static final Collection<Country> COUNTRIES_WITH_SPECIFIC_SERVICES = asList(ITALY, USA);

    private final NotificationServiceForItaly notificationServiceForItaly = mock(NotificationServiceForItaly.class);
    private final NotificationServiceForUsa notificationServiceForUsa = mock(NotificationServiceForUsa.class);
    private final DefaultNotificationService defaultNotificationService = mock(DefaultNotificationService.class);
    private final AlertRoutingService routingService = new AlertRoutingService(notificationServiceForItaly, notificationServiceForUsa, defaultNotificationService);

    @Property
    void should_send_alert_to_specific_service_for_Italian_alert(@ForAll Alert.Type type) {
        final Alert alert = new Alert(type, ITALY);

        routingService.send(alert);

        then(notificationServiceForItaly).should().notify(type.getCodeForItaly());
        then(notificationServiceForUsa).shouldHaveZeroInteractions();
        then(defaultNotificationService).shouldHaveZeroInteractions();
    }

    @Property
    void should_send_alert_code_to_specific_service_for_US_alert(@ForAll Alert.Type type) {
        final Alert alert = new Alert(type, USA);

        routingService.send(alert);

        then(notificationServiceForItaly).shouldHaveZeroInteractions();
        then(notificationServiceForUsa).should().notify(type.getCodeForUsa());
        then(defaultNotificationService).shouldHaveZeroInteractions();
    }

    /**
     * TODO: Add country as an argument of the test method, so that the test does not rely on an example.
     * <br/>
     * <p>Hint #1: <a href="https://jqwik.net/docs/current/user-guide.html#simple-arbitrary-providers">Simple arbitrary providers</a></p>
     * <p>Hint #2: <a href="https://jqwik.net/docs/current/user-guide.html#assumptions">Assumptions</a></p>
     * <p>Hint #3: Calling Mockito.reset(defaultNotificationService) is necessary, and it does not work from JUnit-lifecycle methods :(</p>
     */
    @Property
    void should_send_alert_message_to_default_service_for_alert_of_standard_country(@ForAll Alert.Type type, @ForAll Country country) {
        Assume.that(!COUNTRIES_WITH_SPECIFIC_SERVICES.contains(country));

        final Alert alert = new Alert(type, country);

        reset(defaultNotificationService);
        routingService.send(alert);

        then(notificationServiceForItaly).shouldHaveZeroInteractions();
        then(notificationServiceForUsa).shouldHaveZeroInteractions();
        then(defaultNotificationService).should().notify(type.getDefaultMessage());
    }

}
