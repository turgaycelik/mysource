package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.StoreException;
import com.atlassian.oauth.util.RSAKeys;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.collect.CollectionUtil.transform;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * OfBiz Consumer Store implementation.  This store is responsible for persisting a white list of OAuth consumers that
 * can be edited by admins in JIRA's admin section.  This list will most likely not be very large.
 *
 * @since v4.0
 */
public class OfBizServiceProviderConsumerStore implements ServiceProviderConsumerStore
{
    public static final String TABLE = "OAuthServiceProviderConsumer";
    public static final boolean IS_2LO_ALLOWED_DEFAULT = false;
    private static final boolean IS_3LO_ALLOWED_DEFAULT = true;
    private final OfBizDelegator delegator;

    public static final class Columns
    {
        public static final String ID = "id";
        public static final String CREATED = "created";
        public static final String KEY = "consumerKey";
        public static final String NAME = "name";
        public static final String PUBLIC_KEY = "publicKey";
        public static final String DESCRIPTION = "description";
        public static final String CALLBACK = "callback";

        // 2 Legged OAuth parameters used by the service provider side.
        public static final String IS_2LO_ALLOWED = "twoLOAllowed";
        public static final String EXECUTING_2LO_USER = "executingTwoLOUser";
        public static final String IS_2LO_IMPERSONATION_ALLOWED = "twoLOImpersonationAllowed";
        public static final String IS_3LO_ALLOWED = "threeLOAllowed";
    }

    public OfBizServiceProviderConsumerStore(final OfBizDelegator delegator)
    {
        notNull("delegator", delegator);

        this.delegator = delegator;
    }

    public void put(final Consumer consumer) throws StoreException
    {
        notNull("consumer", consumer);
        notNull("consumer.key", consumer.getKey());
        notNull("consumer.name", consumer.getName());
        notNull("consumer.publicKey", consumer.getPublicKey());

        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final Map<String, Object> fieldValues = MapBuilder.<String, Object>newBuilder().
                add(Columns.KEY, consumer.getKey()).
                add(Columns.CREATED, now).
                add(Columns.NAME, consumer.getName()).
                add(Columns.PUBLIC_KEY, RSAKeys.toPemEncoding(consumer.getPublicKey())).
                add(Columns.DESCRIPTION, consumer.getDescription() == null ? "" : consumer.getDescription()).
                add(Columns.CALLBACK, consumer.getCallback() != null ? consumer.getCallback().toASCIIString() : null).
                add(Columns.IS_2LO_ALLOWED, consumer.getTwoLOAllowed()).
                add(Columns.EXECUTING_2LO_USER, consumer.getExecutingTwoLOUser()).
                add(Columns.IS_2LO_IMPERSONATION_ALLOWED, consumer.getTwoLOImpersonationAllowed()).
                add(Columns.IS_3LO_ALLOWED, consumer.getThreeLOAllowed()).
                toMap();

        try
        {
            final List<GenericValue> consumerGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(Columns.KEY, consumer.getKey()).toMap());
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
                delegator.createValue(TABLE, fieldValues);
            }
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    public Consumer get(final String key) throws StoreException
    {
        notNull("key", key);

        try
        {
            final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(Columns.KEY, key).toMap());
            if (!consumerTokenGVs.isEmpty())
            {
                return createConsumerFromGV(consumerTokenGVs.get(0));
            }
            else
            {
                return null;
            }
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    public void remove(final String key) throws StoreException
    {
        notNull("key", key);

        try
        {
            delegator.removeByAnd(TABLE, MapBuilder.<String, String>newBuilder().add(Columns.KEY, key).toMap());
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    public Iterable<Consumer> getAll() throws StoreException
    {
        try
        {
            final List<GenericValue> consumerGVs = delegator.findAll(TABLE);

            return transform(consumerGVs, new Function<GenericValue, Consumer>()
            {
                public Consumer get(final GenericValue input)
                {
                    return createConsumerFromGV(input);
                }
            });
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    private Consumer createConsumerFromGV(final GenericValue gv)
    {
        try
        {
            try
            {
                final PublicKey publicKey = RSAKeys.fromPemEncodingToPublicKey(gv.getString(Columns.PUBLIC_KEY));
                final String uriString = gv.getString(Columns.CALLBACK);
                final String description = gv.getString(Columns.DESCRIPTION) == null ? "" : gv.getString(Columns.DESCRIPTION);
                URI callBack = null;
                if (StringUtils.isNotEmpty(uriString))
                {
                    callBack = new URI(uriString);
                }

                // this may be null, since this field was only introduced in Atlassian OAuth 1.6.1. if it's null
                // we should use IS_2LO_ALLOWED_DEFAULT.
                Boolean is2LoAllowed = gv.getBoolean(Columns.IS_2LO_ALLOWED);

                // this may be null, since this field was only introduced in Atlassian OAuth 1.8.0. if it's null
                // we assume that 3LO is enabled.
                Boolean is3LoAllowed = gv.getBoolean(Columns.IS_3LO_ALLOWED);

                return Consumer.key(gv.getString(Columns.KEY))
                        .name(gv.getString(Columns.NAME))
                        .publicKey(publicKey)
                        .description(description)
                        .callback(callBack)
                        .twoLOAllowed(is2LoAllowed != null ? is2LoAllowed : IS_2LO_ALLOWED_DEFAULT)
                        .executingTwoLOUser(gv.getString(Columns.EXECUTING_2LO_USER))
                        .twoLOImpersonationAllowed(is2LoAllowed != null ? gv.getBoolean(Columns.IS_2LO_IMPERSONATION_ALLOWED) : IS_2LO_ALLOWED_DEFAULT)
                        .threeLOAllowed(is3LoAllowed != null ? gv.getBoolean(Columns.IS_3LO_ALLOWED) : IS_3LO_ALLOWED_DEFAULT) // for backwards compatibility, not having this defined means 3LO is on (true).
                        .build();
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new StoreException(e);
            }
            catch (InvalidKeySpecException e)
            {
                throw new StoreException(e);
            }
        }
        catch (URISyntaxException e)
        {
            throw new StoreException("callback URI is not valid", e);
        }
    }
}
