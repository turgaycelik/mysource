package com.atlassian.jira.oauth.consumer;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.trustedapps.CurrentApplicationFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore.ConsumerAndSecret;
import com.atlassian.security.auth.trustedapps.CurrentApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultHostConsumerAndSecretProvider
{
    private DefaultHostConsumerAndSecretProvider provider;

    @Mock private ApplicationProperties applicationProperties;
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private I18nHelper i18nHelper;
    @Mock private CurrentApplication currentApplication;
    @Mock private CurrentApplicationFactory currentApplicationFactory;

    @Test
    public void newConsumerAndSecretIsCreatedWhenNoneExist()
    {
        assertThat(provider.get(), is(not(nullValue())));
    }

    @Test
    public void newConsumerAndSecretHasConsumer()
    {
        assertThat(provider.get().getConsumer(), is(not(nullValue())));
    }

    @Test
    public void newConsumerAndSecretWithRsaSignatureHasPrivateKey()
    {
        provider.put(createRsaConsumerAndSecret());
        assertThat(provider.get().getPrivateKey(), is(not(nullValue())));
    }

    @Test
    public void newConsumerAndSecretWithHmacSignatureDoesNotHavePrivateKey()
    {
        provider.put(createHmacConsumerAndSecret());
        assertThat(provider.get().getPrivateKey(), is(nullValue()));
    }

    @Test
    public void newConsumerAndSecretWithRsaSignatureDoesNotHaveSharedSecret()
    {
        provider.put(createRsaConsumerAndSecret());
        assertThat(provider.get().getSharedSecret(), is(nullValue()));
    }

    @Test
    public void newConsumerAndSecretWithHmacSignatureHasSharedSecret()
    {
        provider.put(createHmacConsumerAndSecret());
        assertThat(provider.get().getSharedSecret(), is(not(nullValue())));
    }

    @Test
    public void newConsumerAndSecretHasServiceName()
    {
        assertThat(provider.get().getServiceName(), is(not(nullValue())));
    }

    @Test
    public void newConsumerAndSecretIsSavedOnCreation()
    {
        ConsumerAndSecret expectedInstance = provider.get();
        assertThat(provider.get(), is(sameInstance(expectedInstance)));
    }

    @Test
    public void existingConsumerAndSecretIsRemembered()
    {
        ConsumerAndSecret consumerAndSecret = createHmacConsumerAndSecret();
        provider.put(consumerAndSecret);
        assertThat(provider.get(), is(sameInstance(consumerAndSecret)));
    }

    @Test
    public void consumerKeyIsNotNull()
    {
        assertThat(provider.get().getConsumer().getKey(), is(not(nullValue())));
    }

    @Test
    public void consumerKeyIsLongEnough()
    {
        assertThat(provider.get().getConsumer().getKey().length(), is(greaterThan(25))); // longer than "jira:" plus 20 random digits
    }

    @Test
    public void consumerKeyIsShortEnough()
    {
        assertThat(provider.get().getConsumer().getKey().length(), is(lessThan(255))); // a peek at a JIRA OD DB shows 255 as the length limit
    }

    @Test
    public void consumerKeyIsNotTriviallyRepeated()
    {
        assertThat(createProvider().get().getConsumer().getKey(), is(not(createProvider().get().getConsumer().getKey())));
    }

    @Before
    public void beforeEachTest()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE)).thenReturn("JIRA title");
        when(currentApplication.getID()).thenReturn("id");
        when(currentApplicationFactory.getCurrentApplication()).thenReturn(currentApplication);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        new MockComponentWorker().addMock(JiraAuthenticationContext.class, jiraAuthenticationContext).init();
        provider = createProvider();
    }

    private DefaultHostConsumerAndSecretProvider createProvider()
    {
        return new DefaultHostConsumerAndSecretProvider(applicationProperties, new InMemoryConsumerServiceStore());
    }

    // a real implementation probably stores it in the DB
    private static class InMemoryConsumerServiceStore implements ConsumerServiceStore
    {
        private final Map<String, ConsumerAndSecret> cache = new ConcurrentHashMap<String, ConsumerAndSecret>();

        @Override
        public ConsumerAndSecret get(String key)
        {
            return cache.get(key);
        }

        @Override
        public ConsumerAndSecret getByKey(final String key)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void put(String key, ConsumerAndSecret consumerAndSecret)
        {
            cache.put(key, consumerAndSecret);
        }

        @Override
        public void removeByKey(final String consumerKey)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<Consumer> getAllServiceProviders()
        {
            throw new UnsupportedOperationException();
        }
    }

    private ConsumerAndSecret createRsaConsumerAndSecret()
    {
        Consumer consumer = Consumer.key("key").name("name").publicKey(mock(PublicKey.class)).signatureMethod(Consumer.SignatureMethod.RSA_SHA1).build();
        return new ConsumerAndSecret(DefaultHostConsumerAndSecretProvider.HOST_SERVICENAME, consumer, mock(PrivateKey.class));
    }

    private ConsumerAndSecret createHmacConsumerAndSecret()
    {
        Consumer consumer = Consumer.key("key").name("name").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        return new ConsumerAndSecret(DefaultHostConsumerAndSecretProvider.HOST_SERVICENAME, consumer, "shared secret");
    }
}
