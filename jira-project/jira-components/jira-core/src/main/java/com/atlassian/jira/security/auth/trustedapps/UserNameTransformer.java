package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

/**
 * Responsible for getting a UserResolver we can pass into the TrustedApplicationsFilter.
 * Normally configured from application properties. Implements JRA-14414
 * <p/>
 * Note: implementations Must have a no-arg ctor as they are constructed reflectively.
 *
 * @since v3.13
 */
public interface UserNameTransformer
{
    /**
     * Transform a user name
     *
     * @param userName to transform into a canonical version for the user name store
     * @return the transformed userName
     */
    String transform(String userName);

    /**
     * Default implementation, will fall-back to this.
     */
    static class LowerCase implements UserNameTransformer
    {
        public String transform(final String userName)
        {
            return (userName == null) ? null : userName.toLowerCase();
        }
    }

    /**
     * Implementation for use if the OSUser ProfileProvider uses mixed case as does TrustedApplication requests to JIRA.
     */
    static class NoOp implements UserNameTransformer
    {
        public String transform(final String userName)
        {
            return userName;
        }
    }

    /**
     * Get the name of the UserResolver
     */
    public interface ClassNameRetriever
    {
        Class<UserNameTransformer> get();
    }

    public static class ApplicationPropertiesClassNameRetriever implements ClassNameRetriever
    {
        private final ApplicationProperties properties;

        public ApplicationPropertiesClassNameRetriever(final ApplicationProperties properties)
        {
            this.properties = properties;
        }

        @SuppressWarnings("unchecked")
        public Class<UserNameTransformer> get()
        {
            final String className = properties.getDefaultBackedString(APKeys.TrustedApplications.USER_NAME_TRANSFORMER_CLASS);
            if (StringUtils.isBlank(className))
            {
                return null;
            }
            try
            {
                return (Class<UserNameTransformer>) Class.forName(className);
            }
            catch (final ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * A Factory that gets the configured UserNameTransformer. Public so we can register with pico
     */
    public static class Factory
    {
        private final ClassNameRetriever retriever;

        public Factory(final ClassNameRetriever retriever)
        {
            Null.not("retriever", retriever);
            this.retriever = retriever;
        }

        UserNameTransformer get()
        {
            Class<? extends UserNameTransformer> retrieverClass = retriever.get();
            if (retrieverClass == null)
            {
                // default to LowerCase
                retrieverClass = LowerCase.class;
            }
            try
            {
                return retrieverClass.newInstance();
            }
            catch (final InstantiationException e)
            {
                throw new RuntimeException(e);
            }
            catch (final IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}