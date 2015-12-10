package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.hasDataAttribute;

/**
 * Group picker popup.
 *
 * @since v5.0
 */
public class GroupPickerPopup extends PickerPopup<GroupPickerPopup.GroupPickerRow>
{
    public GroupPickerPopup(LegacyTriggerPicker parent)
    {
        super(parent, PickerType.GROUP_PICKER, GroupPickerRow.class);
    }

    public static class GroupPickerRow extends PickerPopup.PickerRow<GroupPickerRow>
    {
        @Inject protected ExtendedElementFinder extendedFinder;

        private PageElement groupNameCell;

        public GroupPickerRow(GroupPickerPopup owner, PageElement rowElement)
        {
            super(owner, rowElement);
        }

        @Init
        private void initCells()
        {
            groupNameCell = extendedFinder.within(rowElement).find(By.tagName("td"), hasDataAttribute("name"));
        }

        public String getGroupName()
        {
            return groupNameCell.getText();
        }
    }
}
