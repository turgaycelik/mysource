package com.atlassian.jira.user;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.security.random.DefaultSecureTokenGenerator;

/**
 * @see {@link SecureUserTokenManager} for details
 */
@EventComponent
public class DefaultSecureUserTokenManager implements SecureUserTokenManager
{
    //tokens are valid for 30 mins
    final Cache<TokenKey, User> tokenCache;

    public DefaultSecureUserTokenManager(final CacheManager cacheManager)
    {
        tokenCache = cacheManager.getCache(DefaultSecureUserTokenManager.class.getName() + ".tokenCache",
                null,
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @Override
    public String generateToken(final User user, final TokenType tokenType)
    {
        if (user == null)
        {
            return null;
        }
        final String token = DefaultSecureTokenGenerator.getInstance().generateToken();
        tokenCache.put(new TokenKey(token, tokenType), user);
        return token;
    }

    @Override
    public User useToken(final String token, final TokenType tokenType)
    {
        final TokenKey tokenKey = new TokenKey(token, tokenType);
        try
        {
            return tokenCache.get(tokenKey);
        }
        finally
        {
            tokenCache.remove(tokenKey);
        }
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        tokenCache.removeAll();
    }

    private static class TokenKey implements Serializable
    {
        private final String token;
        private final TokenType tokenType;

        private TokenKey(final String token, final TokenType tokenType)
        {
            this.token = token;
            this.tokenType = tokenType;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TokenKey tokenKey = (TokenKey) o;

            if (token != null ? !token.equals(tokenKey.token) : tokenKey.token != null) { return false; }
            if (tokenType != tokenKey.tokenType) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = token != null ? token.hashCode() : 0;
            result = 31 * result + tokenType.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return token + "," + tokenType;
        }
    }
}
