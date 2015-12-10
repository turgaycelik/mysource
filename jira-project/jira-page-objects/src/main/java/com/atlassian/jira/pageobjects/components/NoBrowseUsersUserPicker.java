package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

/**
 * Simple extension of MultiSelect to implement the suggestion-free
 * user name picker used when the logged-in user does not have the
 * Browse User permission.
 *
 * @since v5.1
 */
public class NoBrowseUsersUserPicker extends MultiSelect
{
    public NoBrowseUsersUserPicker(final String id)
    {
        super(id);
    }

    public NoBrowseUsersUserPicker(final String id, Function<String, By> itemLocator)
    {
        super(id, itemLocator);
    }

    @Override
    public void addNotWait(final String item)
    {
        textArea.type(item);
        textArea.type(Keys.SPACE);
    }
}
