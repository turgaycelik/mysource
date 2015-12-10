package com.atlassian.jira.util.system.check;

import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A message that has to be i18n. Contains the i18n key and parameters.
 *
 * @since v4.0
 */
public class I18nMessage
{
    private final String i18nKey;
    private final List<Object> parameters = new ArrayList<Object>();
    private String link = null;

    public I18nMessage(String i18nKey)
    {
        this.i18nKey = i18nKey;
    }

    public void addParameter(Object parameter)
    {
        parameters.add(parameter);
    }

    public List<Object> getParameters()
    {
        return new ArrayList<Object>(parameters);
    }

    public String getKey()
    {
        return i18nKey;
    }

    public String getLink()
    {
        return link;
    }

    public boolean hasLink()
    {
        return TextUtils.stringSet(link);
    }

    public void setLink(String link)
    {
        this.link = link;
    }

}
