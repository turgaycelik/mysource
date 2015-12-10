/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.Comparator;

public class NameComparator implements Comparator
{
    private final I18nHelper i18nHelper;

    public NameComparator(I18nBean i18nBean)
    {
        this.i18nHelper = i18nBean;
    }

    public NameComparator(I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
    }

    public int compare(Object o1, Object o2)
    {
        if (o1 == null)
            throw new IllegalArgumentException("The first parameter is null");
        if (!(o1 instanceof Field))
            throw new IllegalArgumentException("The first parameter " + o1 + " is not an instance of Field");
        if (o2 == null)
            throw new IllegalArgumentException("The second parameter is null");
        if (!(o2 instanceof Field))
            throw new IllegalArgumentException("The second parameter " + o2 + " is not an instance of Field");

        String name1 = i18nHelper.getText(((Field) o1).getNameKey());
        String name2 = i18nHelper.getText(((Field) o2).getNameKey());
        return name1.compareTo(name2);
    }
}
