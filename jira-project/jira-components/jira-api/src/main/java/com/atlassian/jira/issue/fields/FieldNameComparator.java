package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.I18nHelper;

import java.util.Comparator;

/**
 * Compares {@link Field}s based on their translated (i18n'd) name.
 *
 * @since v5.0
 */
@PublicApi
public class FieldNameComparator implements Comparator<Field>
{
    private final I18nHelper i18nHelper;

    public FieldNameComparator(I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
    }

    @Override
    public int compare(Field field1, Field field2)
    {
        if (field1 == null)
            throw new IllegalArgumentException("The first parameter is null");
        if (field2 == null)
            throw new IllegalArgumentException("The second parameter is null");

        String name1 = i18nHelper.getText(field1.getNameKey());
        String name2 = i18nHelper.getText(field2.getNameKey());
        return name1.compareTo(name2);
    }
}
