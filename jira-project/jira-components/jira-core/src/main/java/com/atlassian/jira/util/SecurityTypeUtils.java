package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;

public class SecurityTypeUtils
{
    public static String formatSecurityTypeParameter(String type, String parameter, I18nHelper i18nHelper)
    {
        if (StringUtils.isNotBlank(parameter))
        {
            return "(" + parameter + ")";
        }
        else if ("group".equals(type))
        {
            return "(" + i18nHelper.getText("admin.common.words.anyone") + ")";
        }
        return "";
    }
}
