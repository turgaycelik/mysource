package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.junit.Assert;
import org.openqa.selenium.By;

import static com.atlassian.jira.webtests.LegacyProjectPermissionKeyMapping.getId;
import static com.atlassian.jira.webtests.LegacyProjectPermissionKeyMapping.getKey;

/**
 * @since v6.2
 */
public class AddPermissionPage extends AbstractJiraPage
{

    /**
     * @see #getSchemeId()
     */
    private final int schemeId;

    /**
     * @see #getPermissionKey()
     */
    private final String permissionKey;

    /**
     * Select for permission type selection.
     */
    @ElementBy(name = "permissions")
    private PageElement permissionsSelect;

    /**
     * 'Group' type of permission.
     */
    @ElementBy(id = "type_group")
    private PageElement groupType;

    /**
     * Select for permission group selection.
     */
    @ElementBy(name = "group")
    private PageElement groupSelect;

    /**
     * Add button used for form submitting.
     */
    @ElementBy(id = "add_submit")
    private PageElement addButton;

    /**
     * Constructor.
     * 
     * @param schemeId
     *            {@link #getSchemeId()}
     * @param permission
     *            {@link #getPermission()}
     * @deprecated Use {@link #AddPermissionPage(int, String)}
     */
    @Deprecated
    public AddPermissionPage(int schemeId, int permission)
    {
        this(schemeId, getKey(permission));
    }

    /**
     * Constructor.
     *
     * @param schemeId
     *            {@link #getSchemeId()}
     * @param permissionKey
     *            {@link #getPermission()}
     */
    public AddPermissionPage(int schemeId, String permissionKey)
    {
        this.schemeId = schemeId;
        this.permissionKey = permissionKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimedCondition isAt()
    {
        return permissionsSelect.timed().isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl()
    {
        return "/secure/admin/AddPermission!default.jspa?&schemeId=" + schemeId + "&permissions=" + permissionKey;
    }

    /**
     * @return scheme id - permission owner
     */
    public int getSchemeId()
    {
        return schemeId;
    }

    /**
     * @return for which permission type
     * @deprecated use {@link #getPermissionKey()}
     */
    @Deprecated
    public int getPermission()
    {
        return getId(permissionKey);
    }

    /**
     * @return for which permission type
     */
    public String getPermissionKey()
    {
        return permissionKey;
    }

    /**
     * @return Name of permission, which will be added.
     */
    public String getPermissionName()
    {
        return permissionsSelect.find(By.xpath("option[@value = '" + permissionsSelect.getValue() + "']")).getText();
    }

    /**
     * @return Returns group, which is selected.
     */
    public String getGroup()
    {
        return groupSelect.find(By.xpath("option[@value = '" + groupSelect.getValue() + "']")).getText();
    }

    /**
     * Selects group by provided name.
     * 
     * @param group
     *            select name of group
     */
    public void setGroup(String group)
    {
        groupType.select();
        groupSelect.find(By.xpath("option[text() = '" + group + "']")).click();
    }

    /**
     * @return Adds permission, for form, which is filled on this page.
     */
    public EditPermissionsPage add()
    {
        class Context
        {
        }
        class GroupContext extends Context
        {
            private final String permissionName = getPermissionName();
            private final String group = getGroup();
        }

        Context context;

        if (groupType.isSelected())
        {
            context = new GroupContext();

        } else
        {
            throw new IllegalStateException("Selected type is not supported by add permission!");
        }

        addButton.click();
        EditPermissionsPage result = pageBinder.bind(EditPermissionsPage.class, schemeId);

        if (context instanceof GroupContext)
        {
            GroupContext groupContext = (GroupContext) context;
            Assert.assertTrue("Permission for selected type should be presented on edit permissions page!", result
                    .getPermissionsRowByPermission(groupContext.permissionName).getPermissionByGroup(groupContext.group).isPresent());
        }
        else
        {
            throw new IllegalStateException("Selected type is not supported by add permission!");
        }

        return result;
    }
}
