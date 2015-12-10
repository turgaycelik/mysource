package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.pageobjects.elements.WebDriverLocatable;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents Page Object for page of
 * "/secure/admin/EditPermissions!default.jspa?schemeId={schemeId}".
 * 
 * @since v6.2
 */
public class EditPermissionsPage extends AbstractJiraPage
{

    /**
     * @see #getSchemeId()
     */
    private final int schemeId;

    /**
     * &lt;table&gt, which holds permissions
     */
    @ElementBy(id = "edit_project_permissions")
    private PageElement permissionsTable;

    /**
     * Constructor.
     * 
     * @param schemeId
     *            {@link #getSchemeId()}
     */
    public EditPermissionsPage(final int schemeId)
    {
        this.schemeId = schemeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimedCondition isAt()
    {
        return permissionsTable.timed().isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl()
    {
        return "/secure/admin/EditPermissions!default.jspa?schemeId=" + schemeId;
    }

    /**
     * @return Identity of scheme, for which is this page.
     */
    public int getSchemeId()
    {
        return schemeId;
    }

    /**
     * Opens the Add permission page for the permission defined by it is name.
     *
     * @param permission
     *            name of permission
     * @return {@link AddPermissionPage}
     */
    public AddPermissionPage addForPermission(String permission)
    {
        PageElement addLink = getPermissionsRowByPermission(permission).getAddLink();
        String permissionKey = getQueryParameter(addLink.getAttribute("href"), "permissions");

        addLink.click();
        return pageBinder.bind(AddPermissionPage.class, schemeId, permissionKey);
    }

    /**
     * Deletes permission which is defined by it is name, and group owner of
     * permission.
     * 
     * @param permission
     *            name of permission
     * @param group
     *            of permission
     * @return {@link DeletePermissionPage}
     */
    public DeletePermissionPage deleteForGroup(final String permission, final String group)
    {
        final DeleteLink deleteLink = getPermissionsRowByPermission(permission).getPermissionByGroup(group)
                .getDeleteLink();

        // id-s must be resolved before clicking, after clicking the component
        // is invalid
        final int permissionId = deleteLink.getPermissionId();
        final int schemeId = deleteLink.getSchemeId();

        deleteLink.click();
        return pageBinder.bind(DeletePermissionPage.class, permissionId, schemeId);
    }

    /**
     * Resolves permissions row (inside permissions table) by provided
     * permission's name.
     * 
     * @param permission
     *            name of permission
     * @return resolved row {@link PageElement}
     **/
    public PermissionsRow getPermissionsRowByPermission(final String permission)
    {
        return permissionsTable.find(
                By.xpath("//td[descendant-or-self::*/text() = '" + permission + "']/ancestor::tr"),
                PermissionsRow.class);
    }

    /**
     * Determines whether a row exists for the specified permission.
     *
     * @param permission
     *            name of permission
     * @return boolean value indicating whether the permission row exists
     **/
    public boolean hasPermissionRow(String permission)
    {
        return getPermissionsRowByPermission(permission).isPresent();
    }
    
    public List<String> getPermissionsDeleteLinkIdsByPermission(final String permission)
    {
        List<PageElement> deleteLinks = getPermissionsRowByPermission(permission).findAll(By.partialLinkText("Delete"));
        return
            Lists.transform(deleteLinks, new Function<PageElement, String>()
            {
                @Override
                public String apply(PageElement link)
                {
                    return link.getAttribute("id");
                }
            });
    }

    /**
     * Resolves permission for provided id.
     * 
     * @param id
     *            of permission
     * @return li item which represents permission
     */
    public PermissionsRowPermission getPermissionById(final int id)
    {
        return permissionsTable.find(By.xpath("//a[contains(@href, '&id=" + id + "&')]/ancestor::tr"),
                PermissionsRowPermission.class);
    }

    /**
     * Resolves query parameter from provided {@link URL}.
     * 
     * @param url
     *            owner of parameters
     * @param parameterName
     *            name of parameter
     * @return resolved parameter
     */
    private static String getQueryParameter(final String url, final String parameterName)
    {
        final Matcher matcher = Pattern.compile(".*[?&]" + parameterName + "=([^&]*).*").matcher(url);
        if (matcher.matches())
        {
            return matcher.group(1);
        }
        else
        {
            throw new RuntimeException("Query parameter '" + parameterName + "' can not be found inside url: " + url);
        }
    }

    // Page Objects which represents internal structure of page.

    /**
     * Permissions row (inside permissions table) row with
     * {@link PermissionsRowPermission}-s.
     */
    public static final class PermissionsRow extends WebDriverElement
    {

        /**
         * Constructor.
         * 
         * @param locator
         */
        public PermissionsRow(final By locator)
        {
            super(locator);
        }

        /**
         * Constructor.
         * 
         * @param locator
         * @param parent
         */
        public PermissionsRow(final By locator, final WebDriverLocatable parent)
        {
            super(locator, parent);
        }

        /**
         * {@link PermissionsRowPermission} for provided group.
         * 
         * @param group
         *            name of group e.g.: anyone
         * @return resolved permission
         */
        public PermissionsRowPermission getPermissionByGroup(final String group)
        {
            return find(By.xpath(".//li[contains(text(), 'Group')][contains(text(), '" + group + "')]"),
                    PermissionsRowPermission.class);
        }

        /**
         * Determines whether a group permission exists.
         *
         * @param group
         *            name of group e.g.: anyone
         * @return boolean value indicating whether the group permission exists
         **/
        public boolean hasPermissionForGroup(String group)
        {
            return getPermissionByGroup(group).isPresent();
        }

        /**
         * @return Add link for a permission
         */
        public PageElement getAddLink()
        {
            return find(By.partialLinkText("Add"));
        }
    }

    /**
     * Single permission of {@link PermissionsRow}.
     */
    public static final class PermissionsRowPermission extends WebDriverElement
    {

        /**
         * @see #getDeleteLink()
         */
        @ElementBy(partialLinkText = "Delete")
        private DeleteLink deleteLink;

        /**
         * Constructor.
         * 
         * @param locator
         * @param parent
         */
        public PermissionsRowPermission(final By locator, final WebDriverLocatable parent)
        {
            super(locator, parent);
        }

        /**
         * Constructor.
         * 
         * @param locator
         */
        public PermissionsRowPermission(final By locator)
        {
            super(locator);
        }

        /**
         * @return Delete link for this permission.
         */
        public DeleteLink getDeleteLink()
        {
            return deleteLink;
        }

    }

    /**
     * Represents a Delete Link for an permission.
     */
    public static final class DeleteLink extends WebDriverElement
    {

        /**
         * Constructor.
         * 
         * @param locator
         * @param parent
         * @param timeoutType
         */
        public DeleteLink(final By locator, final WebDriverLocatable parent, final TimeoutType timeoutType)
        {
            super(locator, parent, timeoutType);
        }

        /**
         * @return id of {@link PermissionsRowPermission permission} of this
         *         link.
         */
        public int getPermissionId()
        {
            return Integer.parseInt(getQueryParameter(getAttribute("href"), "id"));
        }

        /**
         * @return ID of scheme, which is owner of {@link #getPermissionId()
         *         permission}.
         */
        public int getSchemeId()
        {
            return Integer.parseInt(getQueryParameter(getAttribute("href"), "schemeId"));
        }

    }

}
