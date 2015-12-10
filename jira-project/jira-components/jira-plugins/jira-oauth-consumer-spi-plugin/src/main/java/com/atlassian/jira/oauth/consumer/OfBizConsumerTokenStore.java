package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import com.opensymphony.module.propertyset.PropertySet;
import net.jcip.annotations.GuardedBy;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OfBiz implementation of the ConsumerTokenStore.  These are the tokens required when making an Oauth request to an
 * Oauth service provider. So for example, an iGoogle Gadget served by JIRA's Dashboard calling home trying to authorize
 * access.
 *
 * @since v4.0
 */
public class OfBizConsumerTokenStore implements ConsumerTokenStore
{
    public static final String TABLE = "OAuthConsumerToken";
    public static final String PROPERTY_SET_KEY = TABLE;
    private final OfBizDelegator delegator;
    private final JiraPropertySetFactory propertySetFactory;

    public static final class Columns
    {
        public static final String ID = "id";
        public static final String KEY = "tokenKey";
        public static final String CREATED = "created";
        public static final String TOKEN = "token";
        public static final String TOKEN_SECRET = "tokenSecret";
        public static final String TYPE = "tokenType";
        public static final String CONSUMER_KEY = "consumerKey";
    }

    static enum TokenType
    {
        ACCESS, REQUEST
    }

    public OfBizConsumerTokenStore(final OfBizDelegator delegator, final JiraPropertySetFactory propertySetFactory)
    {
        notNull("delegator", delegator);
        notNull("propertySetFactory", propertySetFactory);

        this.propertySetFactory = propertySetFactory;
        this.delegator = delegator;
    }

    public ConsumerToken get(final Key key)
    {
        notNull("key", key);

        final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.KEY, key.toString()).toMap());
        if (!consumerTokenGVs.isEmpty())
        {
            return createTokenFromGV(consumerTokenGVs.get(0));
        }
        else
        {
            return null;
        }
    }

    public Map<Key, ConsumerToken> getConsumerTokens(String consumerKey)
    {
        notNull("consumerKey", consumerKey);
        final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.CONSUMER_KEY, consumerKey).toMap());
        Map<Key, ConsumerToken> consumerTokenMap = new HashMap<Key, ConsumerToken>();
        for (GenericValue consumerTokenGV : consumerTokenGVs)
        {
            final Key tokenKey = new Key(consumerTokenGV.getString(Columns.KEY));
            final ConsumerToken token = createTokenFromGV(consumerTokenGV);
            consumerTokenMap.put(tokenKey, token);
        }
        return consumerTokenMap;
    }

    public ConsumerToken put(final Key key, final ConsumerToken token)
    {
        notNull("key", key);
        notNull("token", token);

        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final Map<String, Object> fieldValues = MapBuilder.<String, Object>newBuilder().
                add(Columns.KEY, key.toString()).
                add(Columns.CREATED, now).
                add(Columns.TOKEN, token.getToken()).
                add(Columns.TOKEN_SECRET, token.getTokenSecret()).
                add(Columns.TYPE, token.isAccessToken() ? TokenType.ACCESS.toString() : TokenType.REQUEST.toString()).
                add(Columns.CONSUMER_KEY, token.getConsumer().getKey()).toMap();

        final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.KEY, key.toString()).toMap());
        if (!consumerTokenGVs.isEmpty())
        {
            final GenericValue gv = consumerTokenGVs.get(0);
            gv.setNonPKFields(fieldValues);
            try
            {
                gv.store();
                setTokenProperties(gv.getLong(Columns.ID), token.getProperties());
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
        else
        {
            final GenericValue gv = delegator.createValue(TABLE, fieldValues);
            setTokenProperties(gv.getLong(Columns.ID), token.getProperties());
        }

        return get(key);
    }

    public void remove(final Key key)
    {
        notNull("key", key);

        final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.KEY, key.toString()).toMap());
        if (!consumerTokenGVs.isEmpty())
        {
            final GenericValue tokenGv = consumerTokenGVs.get(0);
            final Long tokenId = tokenGv.getLong(Columns.ID);
            delegator.removeValue(tokenGv);
            setTokenProperties(tokenId, Collections.<String, String>emptyMap());
        }
    }

    public void removeTokensForConsumer(final String consumerKey)
    {
        notNull("consumerKey", consumerKey);

        final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                add(Columns.CONSUMER_KEY, consumerKey).toMap());
        for (GenericValue tokenGv : consumerTokenGVs)
        {
            final Long tokenId = tokenGv.getLong(Columns.ID);
            delegator.removeValue(tokenGv);
            setTokenProperties(tokenId, Collections.<String, String>emptyMap());
        }
    }

    private ConsumerToken createTokenFromGV(final GenericValue gv)
    {
        final Consumer consumer = getConsumerService().getConsumerByKey(gv.getString(Columns.CONSUMER_KEY));
        if (isAccessToken(gv.getString(Columns.TYPE)))
        {
            return ConsumerToken.newAccessToken(gv.getString(Columns.TOKEN))
                    .tokenSecret(gv.getString(Columns.TOKEN_SECRET))
                    .consumer(consumer)
                    .properties(getTokenProperties(gv.getLong(Columns.ID)))
                    .build();
        }
        else
        {
            return ConsumerToken.newRequestToken(gv.getString(Columns.TOKEN))
                    .tokenSecret(gv.getString(Columns.TOKEN_SECRET))
                    .consumer(consumer)
                    .properties(getTokenProperties(gv.getLong(Columns.ID)))
                    .build();
        }
    }

    private Map<String, String> getTokenProperties(final Long tokenId)
    {
        final PropertySet propertySet = propertySetFactory.buildCachingPropertySet(PROPERTY_SET_KEY, tokenId, true);

        final MapBuilder<String, String> ret = MapBuilder.newBuilder();
        @SuppressWarnings ("unchecked")
        final Collection<String> keys = propertySet.getKeys();
        for (String key : keys)
        {
            ret.add(key, propertySet.getText(key));
        }
        return ret.toMap();
    }

    /**
     * We don't really worry too much about synchronisation here, since this should be called via the
     * {@link com.atlassian.jira.oauth.consumer.CachingConsumerTokenStore} which ensures proper synchronisation.
     */
    @GuardedBy("external-lock")
    private void setTokenProperties(final Long tokenId, Map<String, String> props)
    {
        final PropertySet propertySet = propertySetFactory.buildCachingPropertySet(PROPERTY_SET_KEY, tokenId, true);
        //first clear the existing properties
        @SuppressWarnings ("unchecked")
        final Collection<String> keys = propertySet.getKeys();
        for (String key : keys)
        {
            propertySet.remove(key);
        }

        //then add them in again!
        final Set<Map.Entry<String, String>> entries = props.entrySet();
        for (Map.Entry<String, String> prop : entries)
        {
            propertySet.setText(prop.getKey(), prop.getValue());
        }
    }

    private boolean isAccessToken(final String tokenType)
    {
        return TokenType.ACCESS.equals(TokenType.valueOf(tokenType));
    }

    //Protected for unit testing
    protected ConsumerService getConsumerService()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(ConsumerService.class);
    }
}
