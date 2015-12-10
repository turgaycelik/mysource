package com.atlassian.jira.web.action.admin.scheme.comparison;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResults;
import com.atlassian.jira.scheme.distiller.SchemeDistiller;
import com.atlassian.jira.scheme.distiller.SchemeRelationships;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.web.action.admin.scheme.AbstractSchemeToolAction;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * This action services the comparison tool and shows all the information about the compared schemes.
 */
@WebSudoRequired
public class SchemeComparisonToolAction extends AbstractSchemeToolAction
{
    public static final String SCHEME_TOOL_NAME = "SchemeComparisonTool";
    private SchemeDistiller schemeDistiller;
    private DistilledSchemeResults distilledSchemeResults;

    public SchemeComparisonToolAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, SchemeDistiller schemeDistiller, ApplicationProperties applicationProperties)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
        this.schemeDistiller = schemeDistiller;
    }

    public String doDefault() throws Exception
    {
        getDistilledSchemeResults();
        return INPUT;
    }

    public DistilledSchemeResults getDistilledSchemeResults()
    {
        if (distilledSchemeResults == null)
        {
            Collection<Scheme> schemesToCompare = getSchemeObjs();
            if (schemesToCompare != null)
            {
                distilledSchemeResults = schemeDistiller.distillSchemes(schemesToCompare);
            }
        }
        return distilledSchemeResults;
    }

    public SchemeRelationships getSchemeRelationships()
    {
        return schemeDistiller.getSchemeRelationships(getDistilledSchemeResults());
    }

    public int getSchemeDifferencePercentage()
    {
        return (int)(getSchemeRelationships().getSchemeDifferencePercentage() * 100);
    }

    public String getSchemeComparisonDifference()
    {
        String differenceString = NumberFormat.getPercentInstance().format(getSchemeRelationships().getSchemeDifferencePercentage());
        if (getSchemeRelationships().getSchemeDifferencePercentage() == 0)
        {
            differenceString += " (" + getText("admin.scheme.picker.comparison.identical") + ')';
        }
        return differenceString;
    }

    public List<String> getSchemeEntitiesByDisplayName(Collection<SchemeEntity> schemeEntities)
    {
        List<String> displayNames = new ArrayList<String>(schemeEntities.size());
        for (SchemeEntity schemeEntity : schemeEntities)
        {
            displayNames.add(getSchemeTypeForEntity(schemeEntity));
        }
        Collections.sort(displayNames);
        return displayNames;
    }

    public int getTotalDistilledFromSchemes()
    {
        int i = 0;
        for (final Object o : distilledSchemeResults.getDistilledSchemeResults())
        {
            DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) o;
            i += distilledSchemeResult.getOriginalSchemes().size();
        }
        return i;
    }

    public String getSchemeTypeForEntity(SchemeEntity schemeEntity)
    {
        String displayName = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            final NotificationType notificationType = getNotificationType(schemeEntity);
            displayName = formatDisplayNameAndArgument(notificationType, schemeEntity.getParameter());
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            final SecurityType securityType = getSecurityType(schemeEntity);
            displayName = formatDisplayNameAndArgument(securityType, schemeEntity.getParameter());
        }
        return displayName;
    }

    private SecurityType getSecurityType(SchemeEntity schemeEntity)
    {
        return ComponentAccessor.getComponentOfType(PermissionTypeManager.class).getSecurityType(schemeEntity.getType());
    }

    private NotificationType getNotificationType(SchemeEntity schemeEntity)
    {
        return ComponentAccessor.getComponentOfType(NotificationTypeManager.class).getNotificationType(
                schemeEntity.getType());
    }

    private String formatDisplayNameAndArgument(NotificationType notificationType, String argument)
    {
        if (argument == null)
        {
            return notificationType.getDisplayName();
        }
        return notificationType.getDisplayName() + " (" + notificationType.getArgumentDisplay(argument) + ')';
    }

    private String formatDisplayNameAndArgument(SecurityType securityType, String argument)
    {
        if (argument == null)
        {
            return securityType.getDisplayName();
        }
        return securityType.getDisplayName() + " (" + securityType.getArgumentDisplay(argument) + ')';
    }

    public String getSchemeDisplayName()
    {
        String displayName = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.schemes.notifications.notifications");
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.common.words.permissions");
        }
        return displayName;
    }

    public String getComparisonToolDescription()
    {
        String displayName = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.scheme.comparsion.desc.1.notifications","<br/>");
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            displayName = getText("admin.scheme.comparsion.desc.1.permissions","<br/>");
        }
        return displayName;
    }

    public String getEditPage()
    {
        String editPage = null;
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            editPage = "EditNotifications";
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(getDistilledSchemeResults().getSchemeType()))
        {
            editPage = "EditPermissions";
        }

        return editPage;
    }

    public String getParameters()
    {
        StringBuilder params = new StringBuilder();
        params.append("?selectedSchemeType=");
        params.append(getSelectedSchemeType());

        return params.toString();
    }

    public String getColumnWidthPercentage()
    {
        Collection schemes = getSchemeRelationships().getSchemes();
        if (schemes != null)
        {
            return 100 / (schemes.size() + 1) + "%";
        }
        return "100%";
    }


    public String getToolName()
    {
        return SCHEME_TOOL_NAME;
    }
}
