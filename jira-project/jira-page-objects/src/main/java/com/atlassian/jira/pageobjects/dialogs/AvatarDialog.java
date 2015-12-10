package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

/**
 * @since v4.4
 */
public class AvatarDialog extends FormDialog
{
    public AvatarDialog(String id)
    {
        super(id);
    }

    public void setAvatar (final String id)
    {
        PageElement option = getDialogElement().find(By.cssSelector(".jira-avatar[data-id='" + id + "']"));
        PageElement img = option.find(By.tagName("img"));
        img.click(); // The image is the one with the click handler on it.
        waitUntilHidden();
    }

    public void selectAvatarOption(final int number)
    {
        String id = getDialogElement().find(By.cssSelector(".jira-avatar:nth-child("+number+")")).getAttribute("data-id");
        setAvatar(id);
    }
}
