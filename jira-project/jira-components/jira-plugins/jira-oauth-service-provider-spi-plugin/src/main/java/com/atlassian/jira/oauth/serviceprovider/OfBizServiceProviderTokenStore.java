package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.event.AccessTokenRemovedEvent;
import com.atlassian.oauth.event.RequestTokenRemovedEvent;
import com.atlassian.oauth.serviceprovider.Clock;
import com.atlassian.oauth.serviceprovider.InvalidTokenException;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.oauth.serviceprovider.StoreException;
import com.atlassian.oauth.serviceprovider.SystemClock;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.opensymphony.module.propertyset.PropertySet;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.oauth.serviceprovider.ServiceProviderToken.Session;
import static com.atlassian.oauth.serviceprovider.ServiceProviderToken.Version;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides an OfBiz implementation of the OAuth Service Provider token store.  That is OAuth tokens that are used to
 * process incoming requests.
 *
 * @since v4.0
 */
public class OfBizServiceProviderTokenStore implements ServiceProviderTokenStore
{
    private static final Logger log = Logger.getLogger(OfBizServiceProviderTokenStore.class);

    public static final String TABLE = "OAuthServiceProviderToken";
    public static final String PROPERTY_SET_KEY = TABLE;
    private final OfBizDelegator delegator;
    private final UserUtil userUtil;
    private final ServiceProviderConsumerStore consumerStore;
    private final JiraPropertySetFactory propertySetFactory;
    private final Clock clock;
    private final EventPublisher eventPublisher;
    private final UserKeyService userKeyService;

    static final class Columns
    {
        // token properties
        static final String ID = "id";
        static final String CREATED = "created";
        static final String TOKEN = "token";
        static final String TOKEN_SECRET = "tokenSecret";
        static final String TYPE = "tokenType";
        static final String CONSUMER_KEY = "consumerKey";
        static final String USERNAME = "username";
        static final String TTL = "ttl";
        static final String AUTHORIZATION = "auth";
        static final String CALLBACK = "callback";
        static final String VERIFIER = "verifier";
        static final String VERSION = "version";

        // sessions properties introduced in JIRA 5.1 (atlassian-oauth 1.4.x)
        static final String SESSION_HANDLE = "sessionHandle";
        static final String SESSION_CREATION_TIME = "sessionCreationTime";
        static final String SESSION_LAST_RENEWAL_TIME = "sessionLastRenewalTime";
        static final String SESSION_TIME_TO_LIVE = "sessionTimeToLive";
    }

    static enum TokenType
    {
        ACCESS, REQUEST
    }

    public OfBizServiceProviderTokenStore(final OfBizDelegator delegator, final UserUtil userUtil,
            final ServiceProviderConsumerStore consumerStore, EventPublisher eventPublisher,
            final JiraPropertySetFactory propertySetFactory, final UserKeyService userKeyService)
    {
        this(delegator, userUtil, consumerStore, propertySetFactory, eventPublisher, userKeyService, new SystemClock());
    }

    @VisibleForTesting
    public OfBizServiceProviderTokenStore(final OfBizDelegator delegator, final UserUtil userUtil,
            final ServiceProviderConsumerStore consumerStore, final JiraPropertySetFactory propertySetFactory,
            EventPublisher eventPublisher, final UserKeyService userKeyService, Clock clock)
    {
        this.userUtil = notNull("userUtil", userUtil);
        this.delegator = notNull("delegator", delegator);
        this.consumerStore = notNull("consumerStore", consumerStore);
        this.propertySetFactory = notNull("propertySetFactory", propertySetFactory);
        this.eventPublisher = notNull("eventPublisher", eventPublisher);
        this.clock = notNull("clock", clock);
        this.userKeyService = userKeyService;
    }


    @Override
    public ServiceProviderToken get(final String token) throws StoreException
    {
        notNull("token", token);

        try
        {
            final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(Columns.TOKEN, token).toMap());
            if (!consumerTokenGVs.isEmpty())
            {
                return createTokenFromGV(consumerTokenGVs.get(0));
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

    @Override
    public Iterable<ServiceProviderToken> getAccessTokensForUser(final String username)
    {
        final List<GenericValue> userTokenGVs;
        final String userKey = userKeyService.getKeyForUsername(username);
        if (userKey == null)
        {
            throw new IllegalArgumentException("There is no user with username '" + username + "'");
        }

        try
        {
            userTokenGVs = delegator.findByAnd(TABLE,
                    MapBuilder.<String, Object>newBuilder().
                            add(Columns.USERNAME, userKey).
                            add(Columns.TYPE, TokenType.ACCESS.toString()).toMap());
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
        final List<ServiceProviderToken> ret = new ArrayList<ServiceProviderToken>();
        for (GenericValue userTokenGV : userTokenGVs)
        {
            ret.add(createTokenFromGV(userTokenGV));
        }

        return ret;
    }

    @Override
    public ServiceProviderToken put(final ServiceProviderToken token) throws StoreException
    {
        notNull("token", token);

        final String userKey = token.getUser() == null ? null : userKeyService.getKeyForUsername(token.getUser().getName());
        final Map<String, Object> fieldValues = MapBuilder.<String, Object>newBuilder().
                add(Columns.CREATED, new Timestamp(token.getCreationTime())).
                add(Columns.TOKEN, token.getToken()).
                add(Columns.TOKEN_SECRET, token.getTokenSecret()).
                add(Columns.TYPE, token.isAccessToken() ? TokenType.ACCESS.toString() : TokenType.REQUEST.toString()).
                add(Columns.CONSUMER_KEY, token.getConsumer().getKey()).
                add(Columns.USERNAME, userKey).
                add(Columns.AUTHORIZATION, token.getAuthorization() == null ? null : token.getAuthorization().toString()).
                add(Columns.TTL, token.getTimeToLive()).
                add(Columns.VERIFIER, token.getVerifier()).
                add(Columns.CALLBACK, token.getCallback() == null ? null : token.getCallback().toASCIIString()).
                add(Columns.VERSION, token.getVersion() == null ? null : token.getVersion().toString()).
                toMutableMap();

        // save the session if one is available
        ServiceProviderToken.Session session = token.getSession();
        if (session != null)
        {
            fieldValues.put(Columns.SESSION_HANDLE, session.getHandle());
            fieldValues.put(Columns.SESSION_CREATION_TIME, new Timestamp(session.getCreationTime()));
            fieldValues.put(Columns.SESSION_LAST_RENEWAL_TIME, new Timestamp(session.getLastRenewalTime()));
            fieldValues.put(Columns.SESSION_TIME_TO_LIVE, new Timestamp(session.getTimeToLive()));
        }

        try
        {
            final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(Columns.TOKEN, token.getToken()).toMap());
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
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }

        return get(token.getToken());
    }

    @Override
    public void removeAndNotify(String token) throws StoreException
    {
        ServiceProviderToken removedToken = remove(token);
        if (removedToken != null)
        {
            publishRemovedTokenEvent(removedToken);
        }
    }

    @Nullable
    protected final ServiceProviderToken remove(final String token) throws StoreException
    {
        notNull("token", token);

        try
        {
            //need to lookup the tokenId so we can delete the token properties
            final List<GenericValue> consumerTokenGVs = delegator.findByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(Columns.TOKEN, token).toMap());
            if (!consumerTokenGVs.isEmpty())
            {
                final GenericValue tokenGv = consumerTokenGVs.get(0);
                final Long tokenId = tokenGv.getLong(Columns.ID);
                delegator.removeValue(tokenGv);
                setTokenProperties(tokenId, Collections.<String, String>emptyMap());

                return createTokenFromGV(tokenGv);
            }

            return null;
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    @Override
    public void removeExpiredTokensAndNotify() throws StoreException
    {
        List<ServiceProviderToken> removedTokens = removeExpiredTokens();
        for (ServiceProviderToken removedToken : removedTokens)
        {
            publishRemovedTokenEvent(removedToken);
        }
    }

    protected final List<ServiceProviderToken> removeExpiredTokens() throws StoreException
    {
        final OfBizListIterator allTokens = delegator.findListIteratorByCondition(TABLE, null);
        final List<ServiceProviderToken> removedTokens = Lists.newArrayList();
        final List<Long> idsToRemove = Lists.newArrayList();
        try
        {
            for (GenericValue tokenGV : allTokens)
            {
                ServiceProviderToken token = createTokenFromGV(tokenGV);

                // the contract for this method changed in atlassian-oauth 1.4.x. it now only removes tokens that do not
                // have any session information *and* whose TTL has expired. tokens with sessions are handled by
                // removeExpiredSessions().
                if (token.getSession() == null && token.hasExpired(clock))
                {
                    idsToRemove.add(tokenGV.getLong(Columns.ID));
                    removedTokens.add(token);
                }
            }
        }
        finally
        {
            allTokens.close();
        }

        removeByIds(idsToRemove);
        return removedTokens;
    }

    @Override
    public void removeExpiredSessionsAndNotify() throws StoreException
    {
        removeExpiredSessions();
    }

    protected final void removeExpiredSessions() throws StoreException
    {
        OfBizListIterator allTokens = delegator.findListIteratorByCondition(TABLE, null);
        List<Long> idsToRemove = Lists.newArrayList();
        try
        {
            for (GenericValue tokenGV : allTokens)
            {
                ServiceProviderToken token = createTokenFromGV(tokenGV);
                Session session = token.getSession();
                if (session != null && session.hasExpired(clock))
                {
                    idsToRemove.add(tokenGV.getLong(Columns.ID));
                }
            }
        }
        finally
        {
            allTokens.close();
        }

        removeByIds(idsToRemove);
    }

    @Override
    public void removeByConsumer(final String consumerKey)
    {
        notNull("consumerKey", consumerKey);

        try
        {
            delegator.removeByAnd(TABLE, MapBuilder.<String, Object>newBuilder().
                    add(Columns.CONSUMER_KEY, consumerKey).toMap());
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    protected void removeByIds(List<Long> idsToRemove)
    {
        try
        {
            final int rowsRemoved = delegator.removeByOr(TABLE, Columns.ID, idsToRemove);
            if (log.isDebugEnabled())
            {
                log.debug("Successfully removed " + rowsRemoved + " expired tokens.");
            }
        }
        catch (GenericModelException e)
        {
            throw new StoreException(e);
        }
        catch (DataAccessException e)
        {
            throw new StoreException(e);
        }
    }

    private ServiceProviderToken createTokenFromGV(final GenericValue gv)
    {
        boolean isAccessToken = isAccessToken(gv.getString(Columns.TYPE));

        final String token = gv.getString(Columns.TOKEN);
        final Principal user = getUser(gv.getString(Columns.USERNAME));
        if (user == null && isAccessToken)
        {
            throw new InvalidTokenException("Token '" + token + "' is an access token, but has no user associated with it");
        }

        if (isAccessToken)
        {
            ServiceProviderToken.ServiceProviderTokenBuilder builder = ServiceProviderToken.newAccessToken(token)
                    .tokenSecret(gv.getString(Columns.TOKEN_SECRET))
                    .consumer(consumerStore.get(gv.getString(Columns.CONSUMER_KEY)))
                    .authorizedBy(user)
                    .creationTime(gv.getTimestamp(Columns.CREATED).getTime())
                    .timeToLive(gv.getLong(Columns.TTL))
                    .properties(getTokenProperties(gv.getLong(Columns.ID)))
                    .version(getVersion(gv.getString(Columns.VERSION)));

            // session properties introduced in JIRA 5.1 (atlassian-oauth 1.4.x)
            if (gv.getString(Columns.SESSION_HANDLE) != null)
            {
                builder = builder.session(
                        Session.newSession(gv.getString(Columns.SESSION_HANDLE))
                                .creationTime(gv.getTimestamp(Columns.SESSION_CREATION_TIME).getTime())
                                .lastRenewalTime(gv.getTimestamp(Columns.SESSION_LAST_RENEWAL_TIME).getTime())
                                .timeToLive(gv.getTimestamp(Columns.SESSION_TIME_TO_LIVE).getTime())
                                .build()
                );
            }

            return builder.build();
        }
        else
        {
            final String callBackUriString = gv.getString(Columns.CALLBACK);
            URI callbackURI = null;
            if (StringUtils.isNotBlank(callBackUriString))
            {
                callbackURI = URI.create(callBackUriString);
            }
            ServiceProviderToken.ServiceProviderTokenBuilder builder = ServiceProviderToken.newRequestToken(token)
                    .tokenSecret(gv.getString(Columns.TOKEN_SECRET))
                    .consumer(consumerStore.get(gv.getString(Columns.CONSUMER_KEY)))
                    .callback(callbackURI)
                    .creationTime(gv.getTimestamp(Columns.CREATED).getTime())
                    .timeToLive(gv.getLong(Columns.TTL))
                    .version(getVersion(gv.getString(Columns.VERSION)))
                    .properties(getTokenProperties(gv.getLong(Columns.ID)));

            final ServiceProviderToken.Authorization authorization = getAuthorization(gv.getString(Columns.AUTHORIZATION), user);
            if (ServiceProviderToken.Authorization.AUTHORIZED.equals(authorization))
            {
                builder = builder.authorizedBy(user).verifier(gv.getString(Columns.VERIFIER));
            }
            else if (ServiceProviderToken.Authorization.DENIED.equals(authorization))
            {
                builder = builder.deniedBy(user);
            }
            return builder.build();
        }
    }

    private ServiceProviderToken.Version getVersion(final String versionString)
    {
        if (StringUtils.isBlank(versionString))
        {
            return null;
        }
        return Version.valueOf(versionString);
    }

    private ServiceProviderToken.Authorization getAuthorization(String authorization, Principal user)
    {
        if (authorization != null)
        {
            return ServiceProviderToken.Authorization.valueOf(authorization);
        }
        return user != null ? ServiceProviderToken.Authorization.AUTHORIZED : ServiceProviderToken.Authorization.NONE;
    }


    Principal getUser(final String userKey)
    {
        final ApplicationUser userByKey = userUtil.getUserByKey(userKey);
        return userByKey == null ? null: userByKey.getDirectoryUser();
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
     * We don't really worry too much about synchronisation here, since this should be called via the {@link
     * com.atlassian.jira.oauth.serviceprovider.CachingServiceProviderTokenStore} which ensures proper synchronisation.
     */
    @GuardedBy ("external-lock")
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

    /**
     * Publishes either a AccessTokenRemovedEvent or a RequestTokenRemovedEvent, depending on whether the passed-in
     * token is an ACCESS or REQUEST token.
     *
     * @param removedToken     a ServiceProviderToken
     */
    private void publishRemovedTokenEvent(@Nonnull ServiceProviderToken removedToken)
    {
        Principal user = removedToken.getUser();
        // Here we use the username to put in the events
        String username = user != null ? user.getName() : null;

        eventPublisher.publish(removedToken.isAccessToken() ? new AccessTokenRemovedEvent(username) : new RequestTokenRemovedEvent(username));
    }
}
