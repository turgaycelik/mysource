package com.atlassian.jira.pageobjects.pages;

import junit.framework.Assert;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents Page Object of page represented by secure/admin/DeletePermission!default.jspa?id={id}&schemeId={schemeId}". It is confirmation
 * page, when permission is going to be deleted.
 * 
 * @since v6.2
 */
public class DeletePermissionPage extends AbstractJiraPage
{

    /**
     * @see #getId()
     */
    private final int id;

    /**
     * @see #getSchemeId()
     */
    private final int schemeId;

    /**
     * @see #delete()
     */
    @ElementBy(id = "delete_submit")
    private PageElement deleteButton;

    /**
     * Constructor.
     * 
     * @param id
     *            {@link #getId()}
     * @param schemeId
     *            {@link #getSchemeId()}
     */
    public DeletePermissionPage(int id, int schemeId)
    {
        this.id = id;
        this.schemeId = schemeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimedCondition isAt()
    {
        return deleteButton.timed().isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl()
    {
        return "secure/admin/DeletePermission!default.jspa?id=" + id + "&schemeId=" + schemeId;
    }

    /**
     * @return Identity of permission which is going to be deleted.
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return Identity of scheme, which is owner of permission, which is going to be deleted.
     */
    public int getSchemeId()
    {
        return schemeId;
    }

    public EditPermissionsPage delete()
    {
        deleteButton.click();

        EditPermissionsPage result = pageBinder.bind(EditPermissionsPage.class, schemeId);
        Assert.assertFalse("Permission was not successfully deleted, because it is still presented!", result.getPermissionById(id)
                .isPresent());
        return result;
    }

}
