package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.exception.DataAccessException;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.CALLBACK;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.CREATED;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.DESCRIPTION;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.KEY;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.NAME;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.PRIVATE_KEY;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.PUBLIC_KEY;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.SERVICE;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.SHARED_SECRET;
import static com.atlassian.jira.oauth.consumer.OfBizConsumerStore.Columns.SIGNATURE_METHOD;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicate;
import static com.atlassian.jira.util.collect.CollectionUtil.transform;
import static com.atlassian.jira.util.collect.CollectionUtil.filter;
import com.atlassian.jira.util.collect.MapBuilder;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.oauth.Consumer;
import static com.atlassian.oauth.Consumer.SignatureMethod;
import com.atlassian.oauth.consumer.ConsumerCreationException;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.util.RSAKeys;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Persists client consumer information via OfBiz.
 *
 * @since v4.0
 */
public class OfBizConsumerStore implements ConsumerServiceStore
{
    private final OfBizDelegator ofBizDelegator;

    public static final String TABLE = "OAuthConsumer";

    public static class Columns
    {
        public static final String ID = "id";
        public static final String CREATED = "created";
        public static final String NAME = "name";
        public static final String KEY = "consumerKey";
        public static final String SERVICE = "service";
        public static final String PUBLIC_KEY = "publicKey";
        public static final String PRIVATE_KEY = "privateKey";
        public static final String DESCRIPTION = "description";
        public static final String CALLBACK = "callback";
        public static final String SIGNATURE_METHOD = "signatureMethod";
        public static final String SHARED_SECRET = "sharedSecret";
    }

    public OfBizConsumerStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public ConsumerServiceStore.ConsumerAndSecret get(final String service)
    {
        notNull("service", service);

        final List<GenericValue> cosumerGVs = ofBizDelegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().add(SERVICE, service).toMap());
        if (!cosumerGVs.isEmpty())
        {
            return createConsumerFromGV(cosumerGVs.get(0));
        }
        else
        {
            return null;
        }
    }

    public ConsumerServiceStore.ConsumerAndSecret getByKey(final String key)
    {
        notNull("key", key);

        final List<GenericValue> cosumerGVs = ofBizDelegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().add(KEY, key).toMap());
        if (!cosumerGVs.isEmpty())
        {
            return createConsumerFromGV(cosumerGVs.get(0));
        }
        else
        {
            return null;
        }
    }

    public void put(final String service, final ConsumerServiceStore.ConsumerAndSecret cas)
    {
        notNull("service", service);
        notNull("cas", cas);
        notNull("cas.consumer", cas.getConsumer());

        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final URI callback = cas.getConsumer().getCallback();
        final MapBuilder<String, Object> fieldValuesBuilder = MapBuilder.<String, Object>newBuilder().
                add(SERVICE, service).
                add(KEY, cas.getConsumer().getKey()).
                add(CREATED, now).
                add(NAME, cas.getConsumer().getName()).
                add(SIGNATURE_METHOD, cas.getConsumer().getSignatureMethod().name()).
                add(DESCRIPTION, cas.getConsumer().getDescription() == null ? "" : cas.getConsumer().getDescription()).
                add(CALLBACK, callback != null ? callback.toASCIIString() : null);

        if (cas.getConsumer().getSignatureMethod() == SignatureMethod.HMAC_SHA1)
        {
            fieldValuesBuilder.add(SHARED_SECRET, cas.getSharedSecret());
        }
        else
        {
            final String publicKey = RSAKeys.toPemEncoding(cas.getConsumer().getPublicKey());
            final String privateKey = RSAKeys.toPemEncoding(cas.getPrivateKey());
            fieldValuesBuilder.add(PUBLIC_KEY, publicKey);
            fieldValuesBuilder.add(PRIVATE_KEY, privateKey);
        }

        final Map<String, Object> fieldValues = fieldValuesBuilder.toMap();
        final List<GenericValue> consumerGVs = ofBizDelegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(SERVICE, service).toMap());
        if (!consumerGVs.isEmpty())
        {
            final GenericValue gv = consumerGVs.get(0);
            gv.setNonPKFields(fieldValues);
            try
            {
                gv.store();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
        else
        {
            ofBizDelegator.createValue(TABLE, fieldValues);
        }
    }

    public void removeByKey(final String key)
    {
        notNull("key", key);

        ofBizDelegator.removeByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.KEY, key).toMap());
    }

    public Iterable<Consumer> getAllServiceProviders()
    {
        final List<GenericValue> consumerGVs = ofBizDelegator.findAll(TABLE);
        final Iterable<GenericValue> serviceProvidersWithoutHost = filter(consumerGVs, new Predicate<GenericValue>()
        {
            public boolean evaluate(GenericValue input)
            {
                //don't return the host-application's consumer itself in this list!
                return !DefaultHostConsumerAndSecretProvider.HOST_SERVICENAME.equals(input.getString(SERVICE));
            }
        });
        return transform(serviceProvidersWithoutHost, new Function<GenericValue, Consumer>()
        {
            public Consumer get(final GenericValue input)
            {
                return createConsumerFromGV(input).getConsumer();
            }
        });
    }

    private ConsumerAndSecret createConsumerFromGV(final GenericValue consumerGV)
    {
        try
        {
            final Consumer consumer = newConsumer(consumerGV);
            if (consumer.getSignatureMethod() == SignatureMethod.HMAC_SHA1)
            {
                return new ConsumerAndSecret(consumerGV.getString(SERVICE), consumer, consumerGV.getString(SHARED_SECRET));
            }
            else
            {
                final PrivateKey privateKey = RSAKeys.fromPemEncodingToPrivateKey(consumerGV.getString(PRIVATE_KEY));
                return new ConsumerAndSecret(consumerGV.getString(SERVICE), consumer, privateKey);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ConsumerCreationException("No encryption provider with the RSA algorithm installed", e);
        }
        catch (InvalidKeySpecException e)
        {
            throw new ConsumerCreationException("Invalid public key found in store", e);
        }
        catch (URISyntaxException e)
        {
            throw new ConsumerCreationException("Callback URI in store is not a valid URI", e);
        }
    }

    private Consumer newConsumer(GenericValue consumerGV)
            throws URISyntaxException, NoSuchAlgorithmException, InvalidKeySpecException
    {

        final SignatureMethod signatureMethod = SignatureMethod.valueOf(consumerGV.getString(SIGNATURE_METHOD));
        final String uriString = consumerGV.getString(CALLBACK);
        URI callBack = null;
        if (StringUtils.isNotEmpty(uriString))
        {
            callBack = new URI(uriString);
        }
        final String description = consumerGV.getString(Columns.DESCRIPTION) == null ? "" : consumerGV.getString(Columns.DESCRIPTION);
        Consumer.InstanceBuilder builder = Consumer.key(consumerGV.getString(KEY))
                .name(consumerGV.getString(NAME))
                .description(description)
                .callback(callBack)
                .signatureMethod(signatureMethod);

        if (signatureMethod == SignatureMethod.RSA_SHA1)
        {
            final PublicKey publicKey = RSAKeys.fromPemEncodingToPublicKey(consumerGV.getString(PUBLIC_KEY));
            builder.publicKey(publicKey);
        }
        return builder.build();
    }
}
