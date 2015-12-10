package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerCreationException;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.consumer.core.HostConsumerAndSecretProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.util.concurrent.LazyReference;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.UUID;

/**
 * Provides the default consumer and secret for this JIRA instance.
 *
 * @since v4.0
 */
public class DefaultHostConsumerAndSecretProvider implements HostConsumerAndSecretProvider
{
    public static final String HOST_SERVICENAME = "__HOST_SERVICE__";

    private final ApplicationProperties applicationProperties;
    private final ConsumerServiceStore consumerStore;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LazyReference<ConsumerServiceStore.ConsumerAndSecret> consumerAndSecretRef;

    public DefaultHostConsumerAndSecretProvider(ApplicationProperties applicationProperties,
            ConsumerServiceStore consumerStore)
    {
        this.applicationProperties = applicationProperties;
        this.consumerStore = consumerStore;
        this.jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();

        consumerAndSecretRef = new LazyReference<ConsumerServiceStore.ConsumerAndSecret>()
        {
            @Override
            protected ConsumerServiceStore.ConsumerAndSecret create() throws Exception
            {
                return createHostConsumerAndSecret();
            }
        };
    }

    public ConsumerServiceStore.ConsumerAndSecret get()
    {
        final ConsumerServiceStore.ConsumerAndSecret consumerAndSecret = consumerStore.get(HOST_SERVICENAME);
        if (consumerAndSecret == null)
        {
            final ConsumerServiceStore.ConsumerAndSecret hostConsumerAndSecret = consumerAndSecretRef.get();
            consumerStore.put(HOST_SERVICENAME, hostConsumerAndSecret);
            return consumerStore.get(HOST_SERVICENAME);
        }
        return consumerAndSecret;
    }

    public ConsumerServiceStore.ConsumerAndSecret put(final ConsumerServiceStore.ConsumerAndSecret consumerAndSecret)
    {
        consumerStore.put(consumerAndSecret.getServiceName(), consumerAndSecret);
        return consumerStore.get(consumerAndSecret.getServiceName());
    }

    private ConsumerServiceStore.ConsumerAndSecret createHostConsumerAndSecret()
    {
        KeyPair keyPair;
        try
        {
            keyPair = RSAKeys.generateKeyPair();
        }
        catch (GeneralSecurityException e)
        {
            throw new ConsumerCreationException("Could not create key pair for consumer", e);
        }

        final I18nHelper i18nBean = jiraAuthenticationContext.getI18nHelper();
        final String description = i18nBean.getText("oauth.host.consumer.default.description",
                applicationProperties.getString(APKeys.JIRA_BASEURL));

        Consumer consumer = Consumer.key(createConsumerKey())
                .name(applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE))
                .publicKey(keyPair.getPublic())
                .description(description)
                .build();
        return new ConsumerServiceStore.ConsumerAndSecret(HOST_SERVICENAME, consumer, keyPair.getPrivate());
    }

    /*
     * We want this to be unique amongst OnDemand instances so that it can be used to identify an instance
     * (for example, when talking to a Connect add-on).
     * <p>
     * What constitutes sufficiently "long" and "likely contain unique strings" is obviously both subject to scaling
     * factors (what happens if we multiply the number of instances by 1,000?) and not guaranteed to
     * avoid collisions (some sort of global check would be required for that). Global solutions have their own
     * problems and we don't currently have such a service, so let's make sure that we at least make collisions
     * vanishingly unlikely.
     * <p>
     * See https://ecosystem.atlassian.net/browse/AC-811 and https://ecosystem.atlassian.net/browse/APL-629 for
     * some of the consequences of using a readily duplicated id as a "unique" identifier.
     */
    private String createConsumerKey()
    {
        return "jira:" + UUID.randomUUID();
    }
}
