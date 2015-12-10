package com.atlassian.jira.security.auth.rememberme;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.RealClock;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.seraph.service.rememberme.DefaultRememberMeToken;
import com.atlassian.seraph.service.rememberme.RememberMeToken;
import com.atlassian.seraph.spi.rememberme.RememberMeConfiguration;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This it the SPI implementation that we need for Seraph {@link com.atlassian.seraph.service.rememberme.RememberMeService}
 * integration
 *
 * @since v4.2
 */
public class JiraRememberMeTokenDao implements RememberMeTokenDao
{
    public static final String TABLE = "RememberMeToken";
    private final OfBizDelegator delegator;
    private final RememberMeConfiguration rememberMeConfiguration;
    private final Clock clock;
    private final UserKeyService userKeyService;

    public static final class Columns
    {
        public static final String ID = "id";
        public static final String CREATED = "created";
        public static final String TOKEN = "token";
        public static final String USERKEY = "username"; // JRADEV-16974: yup, we're reusing the column for a different purpose
    }

    public JiraRememberMeTokenDao(final OfBizDelegator delegator, final RememberMeConfiguration rememberMeConfiguration)
    {
        this(delegator, rememberMeConfiguration, RealClock.getInstance(), ComponentAccessor.getUserKeyService());
    }

    public JiraRememberMeTokenDao(final OfBizDelegator delegator, final RememberMeConfiguration rememberMeConfiguration, final Clock clock, final UserKeyService userKeyService)
    {
        this.delegator = delegator;
        this.rememberMeConfiguration = rememberMeConfiguration;
        this.clock = clock;
        this.userKeyService = userKeyService;
    }

    public long countAll()
    {
        return delegator.getCount(TABLE);
    }

    public RememberMeToken findById(final Long tokenId)
    {
        return releaseToken(delegator.findByPrimaryKey(TABLE, tokenId));
    }

    public List<RememberMeToken> findForUserName(String userName)
    {
        final String userKey = userKeyService.getKeyForUsername(userName);
        if (userKey == null) {
            return Collections.EMPTY_LIST;
        }
        final Map<String, Object> andMap = MapBuilder.<String, Object>newBuilder(Columns.USERKEY, userKey).toMap();
        final List<GenericValue> gvs = delegator.findByAnd(TABLE, andMap);
        final List<RememberMeToken> tokens = new ArrayList<RememberMeToken>(gvs.size());
        for (GenericValue gv : gvs)
        {
            final RememberMeToken token = releaseToken(gv);
            if (token != null)
            {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public RememberMeToken save(final RememberMeToken token)
    {
        final Timestamp now = new Timestamp(clock.getCurrentDate().getTime());

        final Map<String, Object> values = MapBuilder.<String, Object>newBuilder(Columns.TOKEN, token.getRandomString())
                .add(Columns.USERKEY, userKeyService.getKeyForUsername(token.getUserName()))
                .add(Columns.CREATED, now).toMap();

        final GenericValue gv = delegator.createValue(TABLE, values);

        return releaseToken(gv);
    }

    public void remove(final Long tokenId)
    {
        final GenericValue gv = delegator.findByPrimaryKey(TABLE, tokenId);
        if (gv != null)
        {
            delegator.removeValue(gv);
        }
    }

    public void removeAllForUser(final String username)
    {
        final Map<String, Object> andMap = MapBuilder.<String, Object>newBuilder(Columns.USERKEY, userKeyService.getKeyForUsername(username)).toMap();

        delegator.removeByAnd(TABLE, andMap);
    }

    public void removeAll()
    {
        delegator.removeByAnd(TABLE, Collections.<String, Object>emptyMap());
    }

    private RememberMeToken releaseToken(final GenericValue gv)
    {
        if (gv != null)
        {
            // has it expired
            if (!hasExpired(gv))
            {
                long createdTime = gv.getTimestamp(Columns.CREATED).getTime();
                return DefaultRememberMeToken
                        .builder(gv.getLong(Columns.ID), gv.getString(Columns.TOKEN))
                        .setUserName(userKeyService.getUsernameForKey(gv.getString(Columns.USERKEY)))
                        .setCreatedTime(createdTime)
                        .build();
            }
            else
            {
                delegator.removeValue(gv);
            }
        }
        return null;
    }

    private boolean hasExpired(final GenericValue gv)
    {
        final long maxAgeMS = rememberMeConfiguration.getCookieMaxAgeInSeconds() * 1000;
        final Timestamp createdTS = gv.getTimestamp(Columns.CREATED);
        //noinspection SimplifiableIfStatement
        if (createdTS == null)
        {
            // why would this ever be the case?  worse things have happened at sea!
            return true;
        }
        final long howOld = createdTS.getTime() + maxAgeMS;
        final long now = clock.getCurrentDate().getTime();
        return howOld < now;
    }

}
