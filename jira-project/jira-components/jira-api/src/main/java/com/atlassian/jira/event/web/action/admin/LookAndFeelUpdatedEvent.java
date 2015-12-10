package com.atlassian.jira.event.web.action.admin;

import com.atlassian.crowd.embedded.api.User;

/**
 * Denotes that the look and feel has been changed in some way.
 *
 * @since v5.1
 */
public class LookAndFeelUpdatedEvent
{
    public enum Type
    {
        UPLOAD_LOGO,
        RESET_LOGO,
        UPLOAD_FAVICON,
        RESET_FAVICON,
        AUTO_COLOR_SCHEME,
        UNDO_AUTO_COLOR_SCHEME,
        SITE_TITLE,
        REFRESH_RESOURCES;

        @Override
        public String toString()
        {
            return name().toLowerCase().replace("_", "");
        }
    }

    private final User user;
    private final Type type;

    /**
     * @deprecated since 6.0, replaced by {@link #LookAndFeelUpdatedEvent(User, Type)}
     */
    public LookAndFeelUpdatedEvent()
    {
        this(null, null);
    }

    /**
     * @deprecated since 6.0, replaced by {@link #LookAndFeelUpdatedEvent(User, Type)}
     */
    public LookAndFeelUpdatedEvent(final User user)
    {
        this(user, null);
    }

    public LookAndFeelUpdatedEvent(final User user, final Type type)
    {
        this.user = user;
        this.type = type;
    }

    public User getUser()
    {
        return user;
    }

    public Type getType()
    {
        return type;
    }
}
