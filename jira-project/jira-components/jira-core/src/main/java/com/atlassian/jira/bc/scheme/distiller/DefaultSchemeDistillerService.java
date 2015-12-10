package com.atlassian.jira.bc.scheme.distiller;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResults;
import com.atlassian.jira.scheme.distiller.SchemeDistiller;
import com.atlassian.jira.scheme.distiller.SchemeRelationships;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;


/**
 * 
 */
public class DefaultSchemeDistillerService implements SchemeDistillerService
{

    private static final Logger log = Logger.getLogger(DefaultSchemeDistillerService.class);

    private SchemeDistiller schemeDistiller;
    private SchemeManagerFactory schemeManagerFactory;
    private PermissionManager permissionManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private SchemeFactory schemeFactory;


    public DefaultSchemeDistillerService(SchemeDistiller schemeDistiller, SchemeManagerFactory schemeManagerFactory, PermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext, SchemeFactory schemeFactory)
    {
        this.schemeDistiller = schemeDistiller;
        this.schemeManagerFactory = schemeManagerFactory;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.schemeFactory = schemeFactory;
    }

    public DistilledSchemeResults distillSchemes(User user, Collection schemes, ErrorCollection errorCollection)
    {
        if (hasAdminPermission(user))
        {
            return schemeDistiller.distillSchemes(schemes);
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return null;
    }

    public Scheme persistNewSchemeMappings(User user, DistilledSchemeResult distilledSchemeResult, ErrorCollection errorCollection) throws DataAccessException
    {
        if (hasAdminPermission(user))
        {
            ErrorCollection localErrorCollection = new SimpleErrorCollection();
            // We need to check that the original schemes that form the distilled scheme have not been altered
            validateDistilledSchemeResultIntegrity(user, distilledSchemeResult, localErrorCollection);
            if (!localErrorCollection.hasAnyErrors())
            {
                return schemeDistiller.persistNewSchemeMappings(distilledSchemeResult);
            }
            else
            {
                errorCollection.addErrors(localErrorCollection.getErrors());
                errorCollection.addErrorMessages(localErrorCollection.getErrorMessages());
            }
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return null;
    }

    private void validateDistilledSchemeResultIntegrity(User user, DistilledSchemeResult distilledSchemeResult, ErrorCollection errorCollection)
    {
        // Run through the tempName and make sure that it is still a valid unique name
        isValidNewSchemeName(user, null, distilledSchemeResult.getResultingSchemeTempName(), distilledSchemeResult.getType(), errorCollection);

        if (!errorCollection.hasAnyErrors())
        {
            // Run through each of the original schemes and make sure they have not changed since we first got them
            StringBuilder originalSchemesWithErrors = new StringBuilder();
            for (final Object o : distilledSchemeResult.getOriginalSchemes())
            {
                Scheme originalScheme = (Scheme) o;
                String origSchemeError = validateOriginalSchemeNotAltered(originalScheme, distilledSchemeResult, errorCollection);
                if (origSchemeError != null)
                {
                    if (originalSchemesWithErrors.length() != 0)
                    {
                        originalSchemesWithErrors.append(", ");
                    }
                    originalSchemesWithErrors.append(origSchemeError);
                }
            }

            if (originalSchemesWithErrors.length() > 0)
            {
                errorCollection.addError(distilledSchemeResult.getResultingSchemeTempName(),
                        jiraAuthenticationContext.getI18nHelper().getText("admin.scheme.distiller.service.modified.original.schemes",
                                "<strong>",originalSchemesWithErrors.toString(), "</strong>"));
            }
        }
    }

    private String validateOriginalSchemeNotAltered(Scheme originalScheme, DistilledSchemeResult distilledSchemeResult, ErrorCollection errorCollection)
    {
        String result = null;
        try
        {
            String type = distilledSchemeResult.getType();
            String origSchemeName = originalScheme.getName();
            GenericValue schemeGV = schemeManagerFactory.getSchemeManager(type).getScheme(origSchemeName);
            if (schemeGV != null)
            {
                Scheme schemeFromDb = schemeFactory.getSchemeWithEntitiesComparable(schemeGV);
                if (!schemeFromDb.containsSameEntities(originalScheme) ||
                        !stringsEquivalent(schemeFromDb.getName(), originalScheme.getName()) ||
                        !stringsEquivalent(schemeFromDb.getDescription(), originalScheme.getDescription()))
                {
                    result = origSchemeName;
                }
            }
            else
            {
                result = origSchemeName;
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to retrieve scheme from db.", e);
            result = originalScheme.getName();
        }
        return result;
    }

    private boolean stringsEquivalent(String string1, String string2)
    {
        if (string1 == null && string2 == null)
        {
            return true;
        }

        if (string1 != null && string2 != null)
        {
            return string1.equals(string2);
        }
        return false;
    }

    public SchemeRelationships getSchemeRelationships(User user, DistilledSchemeResults distilledSchemeResults, ErrorCollection errorCollection)
    {
        if (hasAdminPermission(user))
        {
            return schemeDistiller.getSchemeRelationships(distilledSchemeResults);
        }
        else
        { 
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return null;
    }

    public void isValidNewSchemeName(User user, String fieldName, String newSchemeName, String schemeType, ErrorCollection errorCollection)
    {
        if (hasAdminPermission(user))
        {
            GenericValue newScheme = null;
            try
            {
                newScheme = schemeManagerFactory.getSchemeManager(schemeType).getScheme(newSchemeName);
            }
            catch (GenericEntityException e)
            {
                // we don't need to do anything since this means the name did not resolve to a scheme
            }
            if (newScheme != null)
            {
                if (fieldName != null)
                {
                    errorCollection.addError(fieldName, getText("admin.scheme.distiller.service.scheme.name.exists"));
                }
                else
                {
                    errorCollection.addErrorMessage(getText("admin.scheme.distiller.service.scheme.name.exists"));
                }
            }
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private boolean hasAdminPermission(User currentUser)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }
}
