package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renderer for the {@link com.atlassian.jira.sharing.type.GroupShareType}.
 *
 * @since v3.13
 */
public class GroupShareTypeRenderer extends VelocityShareTypeRenderer
{
    private static final String GROUPS_KEY = "groups";
    private static final String BOLD_START = "<b>";
    private static final String BOLD_END = "</b>";

    private final GroupManager groupManager;

    public GroupShareTypeRenderer(final EncodingConfiguration encoding, final VelocityTemplatingEngine templatingEngine, GroupManager groupManager)
    {
        super(encoding, templatingEngine);
        this.groupManager = groupManager;
    }

    public String renderPermission(final SharePermission permission, final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.equals("permission-type", GroupShareType.TYPE, permission.getType());

        final String groupName = TextUtils.htmlEncode(permission.getParam1());
        return authenticationContext.getI18nHelper().getText("common.sharing.shared.display.group", GroupShareTypeRenderer.BOLD_START,
            GroupShareTypeRenderer.BOLD_END, groupName);
    }

    public String getSimpleDescription(final SharePermission permission, final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.equals("permission-type", GroupShareType.TYPE, permission.getType());

        final String groupName = TextUtils.htmlEncode(permission.getParam1());
        return authenticationContext.getI18nHelper().getText("common.sharing.shared.display.group.desc", groupName);
    }

    public String getShareTypeEditor(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);

        final Map<String, Object> params = new HashMap<String, Object>();
        final List<String> groups = new ArrayList<String>(getGroupsForUser(authenticationContext.getLoggedInUser()));
        Collections.sort(groups);
        params.put(GroupShareTypeRenderer.GROUPS_KEY, groups);
        return renderVelocity("share-type-group-selector.vm", params, authenticationContext);
    }

    public boolean isAddButtonNeeded(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);

        return !getGroupsForUser(authenticationContext.getLoggedInUser()).isEmpty();
    }

    public String getShareTypeLabel(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);

        return authenticationContext.getI18nHelper().getText("common.sharing.shared.description.group");
    }

    public Map<String, String> getTranslatedTemplates(final JiraAuthenticationContext authenticationContext, final TypeDescriptor<? extends SharedEntity> type, final RenderMode mode)
    {
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.notNull("type", type);
        Assertions.notNull("mode", mode);

        final Map<String, String> templates = new HashMap<String, String>();

        if (mode == RenderMode.EDIT)
        {
            templates.put("share_group_display", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.shared.template.group", GroupShareTypeRenderer.BOLD_START, GroupShareTypeRenderer.BOLD_END)));
            templates.put("share_group_description", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.shared.template.group.desc")));
        }
        else if (mode == RenderMode.SEARCH)
        {
            /**
             * This code generated the translation key based on the ShareType. This means the following keys are used:
             * common.sharing.search.template.group.desc.SearchRequest
             * common.sharing.search.template.group.desc.PortalPage
             */
            templates.put("share_group_description", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.search.template.group.desc." + type.getName())));
        }
        return Collections.unmodifiableMap(templates);
    }

    // /CLOVER:OFF
    List<String> getGroupsForUser(final User user)
    {
        if (user == null)
        {
            return Collections.emptyList();
        }
        Collection<String> groupNames = groupManager.getGroupNamesForUser(user);
        return new ArrayList<String>(groupNames);
    }
    // /CLOVER:ON
}
