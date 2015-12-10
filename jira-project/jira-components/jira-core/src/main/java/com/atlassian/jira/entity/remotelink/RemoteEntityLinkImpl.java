package com.atlassian.jira.entity.remotelink;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.1
 */
public abstract class RemoteEntityLinkImpl<E> implements RemoteEntityLink<E>
{
    protected static final String APPLICATION = "application";
    protected static final String APPLICATION_NAME = "name";
    protected static final String APPLICATION_TYPE = "type";
    protected static final String OBJECT = "object";
    protected static final String OBJECT_ICON = "icon";
    protected static final String OBJECT_ICON_TITLE = "title";
    protected static final String OBJECT_ICON_URL = "url16x16";
    protected static final String OBJECT_SUMMARY = "summary";
    protected static final String OBJECT_TITLE = "title";
    protected static final String OBJECT_URL = "url";

    protected final String globalId;
    protected final LazyJsonParser jsonRef;

    protected RemoteEntityLinkImpl(@Nonnull String globalId, @Nonnull String json)
    {
        this.globalId = notBlank("globalId", globalId);
        this.jsonRef = new LazyJsonParser(notNull("json", json));
    }

    @Nonnull
    @Override
    public String getGlobalId()
    {
        return globalId;
    }

    @Nonnull
    @Override
    public String getJsonString()
    {
        return jsonRef.getJson();
    }

    @Override
    public String getTitle()
    {
        return jsonRef.getTextAtPath(OBJECT, OBJECT_TITLE);
    }

    @Override
    public String getSummary()
    {
        return jsonRef.getTextAtPath(OBJECT, OBJECT_SUMMARY);
    }

    @Override
    public String getUrl()
    {
        return jsonRef.getTextAtPath(OBJECT, OBJECT_URL);
    }

    @Override
    public String getIconUrl()
    {
        return jsonRef.getTextAtPath(OBJECT, OBJECT_ICON, OBJECT_ICON_URL);
    }

    @Override
    public String getIconTitle()
    {
        return jsonRef.getTextAtPath(OBJECT, OBJECT_ICON, OBJECT_ICON_TITLE);
    }

    @Override
    public String getApplicationName()
    {
        return jsonRef.getTextAtPath(APPLICATION, APPLICATION_NAME);
    }

    @Override
    public String getApplicationType()
    {
        return jsonRef.getTextAtPath(APPLICATION, APPLICATION_TYPE);
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);
}
