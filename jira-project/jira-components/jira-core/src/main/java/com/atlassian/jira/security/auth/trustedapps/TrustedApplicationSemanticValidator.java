package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;

import java.security.PublicKey;

/**
 * Business and system level validations.
 * 
 * @since v3.12
 */
public class TrustedApplicationSemanticValidator implements TrustedApplicationValidator
{
    private final TrustedApplicationManager manager;

    public TrustedApplicationSemanticValidator(final TrustedApplicationManager manager)
    {
        Assertions.notNull("manager", manager);
        this.manager = manager;
    }

    public boolean validate(final JiraServiceContext context, final I18nHelper helper, final SimpleTrustedApplication application)
    {
        if (application.getId() <= 0)
        {
            final String appId = application.getApplicationId();
            if (!StringUtils.isBlank(appId))
            {
                if (manager.get(appId) != null)
                {
                    context.getErrorCollection().addErrorMessage(helper.getText("admin.trustedapps.edit.applicationid.already.exists", appId));
                }
            }
        }
        else
        {
            if (manager.get(application.getId()) == null)
            {
                context.getErrorCollection().addErrorMessage(helper.getText("admin.trustedapps.edit.id.not.found", String.valueOf(application.getId())));
            }
        }
        final PublicKey publicKey = KeyFactory.getPublicKey(application.getPublicKey());
        if (publicKey instanceof KeyFactory.InvalidPublicKey)
        {
            context.getErrorCollection().addErrorMessage(helper.getText("admin.trustedapps.edit.public.key.invalid", publicKey.toString()));
        }
        return !context.getErrorCollection().hasAnyErrors();
    }
}
