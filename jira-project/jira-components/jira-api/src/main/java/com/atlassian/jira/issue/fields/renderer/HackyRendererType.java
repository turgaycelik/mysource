package com.atlassian.jira.issue.fields.renderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration to define a set of custom renderer types to provide for a number of fields as defined in the {@link
 * com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry}.  A renderer type is simply a key and i18n key
 * that can be used to display an i18nized name in the UI.
 *
 * @since v4.2
 */
public enum HackyRendererType
{
    SELECT_LIST("select-list-renderer", "admin.renderer.select.list.renderer.name"),
    FROTHER_CONTROL("frother-control-renderer", "admin.renderer.frother.control.renderer.name");

    private static final Map<String, HackyRendererType> stringToEnum = new HashMap<String, HackyRendererType>();

    static
    {
        for (HackyRendererType rendererType : values())
        {
            stringToEnum.put(rendererType.getKey(), rendererType);
        }
    }

    private String key;
    private String displayNameI18nKey;

    HackyRendererType(final String key, final String displayNameI18nKey)
    {
        this.key = key;
        this.displayNameI18nKey = displayNameI18nKey;
    }

    /**
     * @return the i18n key to display a user friendly i18nized name for this renderer type
     */
    public String getDisplayNameI18nKey()
    {
        return displayNameI18nKey;
    }

    /**
     * @return unique key to store with the {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem}
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Resolves the RendererType object from the string key provided.  May return null if no matching RendererType can
     * be found.
     *
     * @param key The key
     * @return A HackyRendererType or null
     */
    public static HackyRendererType fromKey(String key)
    {
        return stringToEnum.get(key);
    }
}
