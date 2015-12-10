/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Map;

public class FilterValuesGenerator implements ValuesGenerator
{
    public static final String FILTER_ALL_ISSUES = "all";
    public static final String FILTER_INCOMPLETE_ISSUES = "incomplete";

    public Map getValues(Map userParams)
    {
        User u = (User) userParams.get("User");
        I18nHelper i18nHelper = new I18nBean(u);

        return EasyMap.build(FILTER_ALL_ISSUES, i18nHelper.getText("timetracking.limit.all"), FILTER_INCOMPLETE_ISSUES, i18nHelper.getText("timetracking.limit.incomplete.only"));
    }
}
