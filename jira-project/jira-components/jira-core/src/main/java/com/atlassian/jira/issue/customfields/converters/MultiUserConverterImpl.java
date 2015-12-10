package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

public class MultiUserConverterImpl extends UserConverterImpl implements MultiUserConverter
{
    private static final String COMMA_REPLACEMENT = "&#44;";

    public MultiUserConverterImpl(UserManager userManager, I18nHelper i18nHelper)
    {
        super(userManager, i18nHelper);
    }

    public Collection<String> extractUserStringsFromString(String value)
    {
        Collection<String> valuesToAdd = null;
        String[] a = StringUtils.split(StringUtils.replace(value, "\\,", COMMA_REPLACEMENT), ",");

        if (a != null)
        {
            valuesToAdd = new ArrayList<String>();
            for (String s : a)
            {
                valuesToAdd.add(StringUtils.replace(s, COMMA_REPLACEMENT, ",").trim());
            }
        }

        return valuesToAdd;
    }
}
