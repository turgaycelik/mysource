package com.atlassian.jira.sharing.type;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A renderer for the {@link com.atlassian.jira.sharing.type.GlobalShareType}.
 *
 * @since v3.13
 */
public class GlobalShareTypeRenderer implements ShareTypeRenderer
{
    public String renderPermission(final SharePermission permission, final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.equals("permission-type", GlobalShareType.TYPE, permission.getType());

        return getText(authenticationContext, "common.sharing.shared.template.everyone");
    }

    public String getSimpleDescription(final SharePermission permission, final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.equals("permission-type", GlobalShareType.TYPE, permission.getType());

        return getText(authenticationContext, "common.sharing.shared.display.everyone.desc");
    }

    public String getShareTypeEditor(final JiraAuthenticationContext authenticationContext)
    {
        return "";
    }

    public boolean isAddButtonNeeded(final JiraAuthenticationContext userCtx)
    {
        return true;
    }

    public String getShareTypeLabel(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);
        return authenticationContext.getI18nHelper().getText("common.sharing.shared.description.everyone");
    }

    public Map<String, String> getTranslatedTemplates(final JiraAuthenticationContext authenticationContext, final TypeDescriptor<? extends SharedEntity> type, final RenderMode mode)
    {
        notNull("authenticationContext", authenticationContext);
        final Map<String, String> templates = new HashMap<String, String>();
        templates.put("share_global_display", StringEscapeUtils.escapeJavaScript(getText(authenticationContext,
            "common.sharing.shared.template.everyone")));
        templates.put("share_global_description", StringEscapeUtils.escapeJavaScript(getText(authenticationContext,
            "common.sharing.shared.template.everyone.desc")));
        templates.put("share_global_warning", StringEscapeUtils.escapeJavaScript(getText(authenticationContext,
            "common.sharing.shared.template.everyone.warning")));

        return Collections.unmodifiableMap(templates);
    }

    private String getText(final JiraAuthenticationContext ctx, final String key)
    {
        return ctx.getI18nHelper().getText(key);
    }
}
