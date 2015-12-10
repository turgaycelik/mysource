package com.atlassian.jira.bc.issue.fields.screen;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.util.TextUtils;

import static com.atlassian.jira.bc.ServiceOutcomeImpl.error;

/**
 *
 * @since v5.2
 */
public class DefaultFieldScreenService implements FieldScreenService
{
    private final I18nHelper.BeanFactory i18nFactory;
    private final FieldScreenManager fieldScreenManager;
    private final PermissionManager permissionManager;

    public DefaultFieldScreenService(I18nHelper.BeanFactory i18nFactory, FieldScreenManager fieldScreenManager, PermissionManager permissionManager)
    {
        this.i18nFactory = i18nFactory;
        this.fieldScreenManager = fieldScreenManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public ServiceOutcome<FieldScreen> copy(FieldScreen screenToCopy, String copyName, String copyDescription, ApplicationUser loggedInUser)
    {
        I18nHelper i18n = i18nFactory.getInstance(loggedInUser);

        if (!TextUtils.stringSet(copyName))
        {
            return error(i18n.getText("admin.common.errors.validname"));
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            return error(i18n.getText("admin.errors.screens.no.permission"));
        }

        for (FieldScreen fieldScreen : fieldScreenManager.getFieldScreens())
        {
            if (copyName.equals(fieldScreen.getName()))
            {
                return error(i18n.getText("admin.errors.screens.duplicate.screen.name"));
            }
        }

        // Copy the screenToCopy
        FieldScreen copy = new FieldScreenImpl(fieldScreenManager, null);
        copy.setName(copyName);
        copy.setDescription(copyDescription);
        copy.store();

        // Iterate over each tab and create it
        for (FieldScreenTab fieldScreenTab : screenToCopy.getTabs())
        {
            FieldScreenTab copyFieldScreenTab = copy.addTab(fieldScreenTab.getName());

            for (FieldScreenLayoutItem fieldScreenLayoutItem : fieldScreenTab.getFieldScreenLayoutItems())
            {
                copyFieldScreenTab.addFieldScreenLayoutItem(fieldScreenLayoutItem.getFieldId());
            }
        }

        return ServiceOutcomeImpl.ok(copy);
    }
}
