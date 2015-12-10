/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarServiceHelper;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.JiraVelocityHelper;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MemoizingMap;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.velocity.VelocityHelper;
import com.opensymphony.util.TextUtils;

import java.util.Map;

public class JiraMailQueueUtils
{
    private static final JiraUtils JIRA_UTILS = new JiraUtils();
    private static final JiraKeyUtils JIRA_KEY_UTILS = new JiraKeyUtils();
    private static final UserUtils USER_UTILS = new UserUtils();
    private static final TextUtils TEXT_UTILS = new TextUtils();
    private static final VelocityHelper VELOCITY_HELPER = new VelocityHelper();

    private static final MemoizingMap.Master<String, Object> MASTER;

    static
    {
        final MemoizingMap.Master.Builder<String, Object> builder = MemoizingMap.Master.builder();
        // NOTE: if adding a parameter here please update the doc online at
        // http://confluence.atlassian.com/display/JIRA/Contents+of+the+Velocity+Context+for+email+templates
        builder.add("jirautils", JIRA_UTILS);
        builder.add("jirakeyutils", JIRA_KEY_UTILS);
        builder.add("build", ComponentAccessor.getComponent(BuildUtilsInfo.class));
        builder.add("urlModeAbsolute", UrlMode.ABSOLUTE);

        // the next three were copied from VelocityContextUtils in atlassian-velocity
        builder.add("userutils", USER_UTILS);
        builder.add("velocityhelper", VELOCITY_HELPER);
        builder.add("textutils", TEXT_UTILS);

        // lazy

        builder.addLazy("constantsManager", new Supplier<ConstantsManager>()
        {
            public ConstantsManager get()
            {
                return ComponentAccessor.getConstantsManager();
            }
        });
        builder.addLazy("projectManager", new Supplier<ProjectManager>()
        {
            public ProjectManager get()
            {
                return ComponentAccessor.getProjectManager();
            }
        });
        builder.addLazy("customFieldManager", new Supplier<CustomFieldManager>()
        {
            public CustomFieldManager get()
            {
                return ComponentAccessor.getCustomFieldManager();
            }
        });
        builder.addLazy("applicationProperties", new Supplier<ApplicationProperties>()
        {
            public ApplicationProperties get()
            {
                return ComponentAccessor.getApplicationProperties();

            }
        });
        builder.addLazy("jiraUserUtils", new Supplier<UserUtil>()
        {
            public UserUtil get()
            {
                return ComponentAccessor.getUserUtil();
            }
        });
        builder.addLazy("mailPluginsHelper", new Supplier<JiraMailPluginsHelper>()
        {
            public JiraMailPluginsHelper get()
            {
                return new JiraMailPluginsHelperImpl(ComponentAccessor.getPluginAccessor());
            }
        });
        builder.addLazy("userformat", new Supplier<UserFormatManager>()
        {
            public UserFormatManager get()
            {
                return ComponentAccessor.getComponentOfType(UserFormatManager.class);
            }
        });
        builder.addLazy("webResourceManager", new Supplier<WebResourceManager>()
        {
            public WebResourceManager get()
            {
                return ComponentAccessor.getWebResourceManager();
            }
        });
        builder.addLazy("avatarService", new Supplier<AvatarService>()
        {
            public AvatarService get()
            {
                return ComponentAccessor.getComponent(AvatarService.class);
            }
        });
        builder.addLazy("avatarServiceHelper", new Supplier<AvatarServiceHelper>()
        {
            public AvatarServiceHelper get()
            {
                return new AvatarServiceHelper(ComponentAccessor.getAvatarService());
            }
        });

        // override the default velocity helper with ours
        builder.addLazy("velocityhelper", new Supplier<JiraVelocityHelper>()
        {
            public JiraVelocityHelper get()
            {
                return new JiraVelocityHelper(ComponentAccessor.getFieldManager());
            }
        });
        MASTER = builder.master();
    }

    /**
     * Add context params needed by the body of JIRA messages
     *
     * @param params initial context parameters
     * @return a map of context parameters
     */
    public static Map<String, Object> getContextParamsBody(final Map<String, Object> params)
    {
        return MASTER.toMap(params);
    }

    /**
     * Get the context params needed by the body of JIRA messages
     *
     * @return a MemoizingMap.Master of context parameters
     */
    public static MemoizingMap.Master<String, Object> getContextParamsMaster()
    {
        return MASTER;
    }
}
