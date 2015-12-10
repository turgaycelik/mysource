/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Map;

public class SortingValuesGenerator implements ValuesGenerator
{
    public static final String SORT_BY_MOST_COMPLETED = "most";
    public static final String SORT_BY_LEAST_COMPLETED = "least";

    public Map getValues(Map userParams)
    {
        User u = (User) userParams.get("User");

        I18nHelper i18nHelper = new I18nBean(u);

        return EasyMap.build(SORT_BY_MOST_COMPLETED, i18nHelper.getText("timetracking.sorting.mostcompletedissuesfirst"), SORT_BY_LEAST_COMPLETED, i18nHelper.getText("timetracking.sorting.leastcompletedissuesfirst"));
    }
}
