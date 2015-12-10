package com.atlassian.jira.service;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the in-built service types that ship with JIRA.
 *
 * @see JiraService
 * @since v4.3
 */
public interface InBuiltServiceTypes
{
    /**
     * Gets a list of all the in-built service types.
     *
     * @return A list of all the in-built service types.
     */
    Set<InBuiltServiceType> all();

    /**
     * Gets a list of all the in-built services types that can be managed by the specified user.
     *
     * @param user the user in play.
     * @return A list of all the in-built services types that can be managed by the specified user.
     */
    Set<InBuiltServiceType> manageableBy(User user);

    /**
     * Describes an in-built service in JIRA.
     *
     * @see InBuiltServiceTypes
     */
    @Immutable
    class InBuiltServiceType
    {
        private final Class<? extends JiraService> type;

        private final String i18nKey;

        InBuiltServiceType(final Class<? extends JiraService> type, final String i18nKey)
        {
            this.type = notNull("type", type);
            this.i18nKey = notNull("i18nKey", i18nKey);
        }

        /**
         * Gets the {@link Class} that implements this service.
         *
         * @return the Class that implements this service.
         */
        @Nonnull
        public Class<? extends JiraService> getType()
        {
            return type;
        }

        /**
         * Gets an i18nk key which describes the capabilities of this service.
         *
         * @return An i18nk key which describes the capabilities of this service.
         */
        @Nonnull
        public String getI18nKey()
        {
            return i18nKey;
        }
    }
}
