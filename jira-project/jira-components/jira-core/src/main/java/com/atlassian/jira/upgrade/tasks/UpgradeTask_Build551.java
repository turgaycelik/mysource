package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import java.util.Collection;

/**
 * This upgrade task adds the Labels field to the first tab of the default screen
 *
 * @since v4.2
 */
public class UpgradeTask_Build551 extends AbstractUpgradeTask
{
    private final FieldScreenSchemeManager fieldScreenSchemeManager;

    public UpgradeTask_Build551(final FieldScreenSchemeManager fieldScreenSchemeManager)
    {
        super(false);
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        FieldScreenScheme defaultScheme = null;
        final Collection<FieldScreenScheme> fieldScreenSchemeCollection = fieldScreenSchemeManager.getFieldScreenSchemes();
        for (FieldScreenScheme fieldScreenScheme : fieldScreenSchemeCollection)
        {
            if (fieldScreenScheme.getId() != null && FieldScreenSchemeManager.DEFAULT_FIELD_SCREEN_SCHEME_ID.equals(fieldScreenScheme.getId()))
            {
                defaultScheme = fieldScreenScheme;
                break;
            }
        }
        if (defaultScheme != null)
        {
            final FieldScreen defaultScreen = defaultScheme.getFieldScreen(null);
            if (defaultScreen != null && defaultScreen.getTabs().size() > 0)
            {
                final FieldScreenTab firstTab = defaultScreen.getTab(0);
                //if the current field screen Tab doesn't contain the labels field yet, add it!
                final FieldScreenLayoutItem screenLayoutItem = firstTab.getFieldScreenLayoutItem(IssueFieldConstants.LABELS);
                if (screenLayoutItem == null)
                {
                    firstTab.addFieldScreenLayoutItem(IssueFieldConstants.LABELS);
                }
            }
        }
    }

    @Override
    public String getShortDescription()
    {
        return "Adds the new Labels system field to the Default Screen of the default field configuration.";
    }

    @Override
    public String getBuildNumber()
    {
        return "551";
    }
}