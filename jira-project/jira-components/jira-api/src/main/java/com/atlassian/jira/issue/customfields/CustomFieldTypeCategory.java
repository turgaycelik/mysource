package com.atlassian.jira.issue.customfields;

import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.List;

/**
 * A category for a custom field type.
 *
 * @since v6.1
 */
public enum CustomFieldTypeCategory
{
    //NOTE: The order of these enums are important (i.e. this is their order was we want to see them).
    ALL("customfields.category.all"),
    STANDARD("customfields.category.standard"),
    ADVANCED("customfields.category.advanced");

    private final String nameI18nKey;

    private CustomFieldTypeCategory(String nameI18nKey)
    {
        this.nameI18nKey = nameI18nKey;
    }

    public static Optional<CustomFieldTypeCategory> fromString(String name)
    {
        for (CustomFieldTypeCategory category : CustomFieldTypeCategory.values())
        {
            if (category.name().equalsIgnoreCase(name))
            {
                return Optional.of(category);
            }
        }
        return Optional.absent();
    }

    public static List<CustomFieldTypeCategory> orderedValues()
    {
        //We rely on the order of the enum. We have this method to make it clear that the order is consistent and
        //useful.
        return Arrays.asList(CustomFieldTypeCategory.values());
    }

    public String getNameI18nKey()
    {
        return nameI18nKey;
    }
}