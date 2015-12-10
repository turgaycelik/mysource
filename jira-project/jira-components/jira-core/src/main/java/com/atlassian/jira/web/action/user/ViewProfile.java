package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.plugin.profile.OptionalUserProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.conditions.UserIsTheLoggedInUserCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.plugin.webresource.SuperBatchFilteringWriter;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.AbstractPluggableTabPanelAction;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.defaultString;

@SuppressWarnings ({ "UnusedDeclaration" })
public class ViewProfile extends AbstractPluggableTabPanelAction<ViewProfilePanelModuleDescriptor>
{
    private static final String CONTENTONLY = "contentonly";
    private static final String JSON_CONTENT = "json";

    private String name;
    private User profileUser;
    private static final String USER_NOT_FOUND_VIEW = "usernotfound";
    private final SimpleLinkManager simpleLinkManager;
    private final WebResourceManager webResourceManager;
    private final UserPropertyManager userPropertyManager;
    protected final CrowdService crowdService;
    private final AvatarService avatarService;
    private final UserHistoryManager userHistoryManager;

    private boolean contentOnly = false;
    private boolean asJson = false;
    private boolean noTitle = false;

    private ViewProfilePanelModuleDescriptor selectedDescriptor = null;

    public ViewProfile(final UserPropertyManager userPropertyManager)
    {
        this(ComponentAccessor.getComponentOfType(SimpleLinkManager.class),
                ComponentAccessor.getComponentOfType(WebResourceManager.class),
                ComponentAccessor.getComponentOfType(PluginAccessor.class),
                ComponentAccessor.getComponentOfType(CrowdService.class), userPropertyManager,
                ComponentAccessor.getComponentOfType(AvatarService.class),
                ComponentAccessor.getComponentOfType(UserHistoryManager.class)
                );
    }

    public ViewProfile(final SimpleLinkManager SimpleLinkManager, final WebResourceManager webResourceManager,
            final PluginAccessor pluginAccessor, final CrowdService crowdService,
            final UserPropertyManager userPropertyManager, AvatarService avatarService, UserHistoryManager userHistoryManager)
    {
        super(pluginAccessor);
        super.setPersistenceKey(SessionKeys.VIEW_PROFILE_TAB);
        simpleLinkManager = SimpleLinkManager;
        this.webResourceManager = webResourceManager;
        this.crowdService = crowdService;
        this.userPropertyManager = userPropertyManager;
        this.avatarService = avatarService;
        this.userHistoryManager = userHistoryManager;
    }

    protected String doExecute() throws Exception
    {
        parseAcceptsHeader();
        if (getLoggedInUser() == null)
        {
            if (contentOnly)
            {
                ServletActionContext.getResponse().setStatus(401);
                return NONE;
            }

            return "securitybreach";
        }

        if (getUser() == null)
        {
            if (contentOnly)
            {
                ServletActionContext.getResponse().setStatus(401);
                return NONE;
            }

            return USER_NOT_FOUND_VIEW;
        }

        if (getUser() != null)
        {
            userHistoryManager.addUserToHistory(UserHistoryItem.ASSIGNEE, getLoggedInApplicationUser(), ApplicationUsers.from(getUser()));
        }

        if (contentOnly)
        {
            if (canSeeTab(getSelectedTab()))
            {
                return (this.asJson) ? JSON_CONTENT : CONTENTONLY;
            }
            else
            {
                ServletActionContext.getResponse().setStatus(401);
                return NONE;                
            }
        }
        else
        {
            webResourceManager.requireResourcesForContext("atl.userprofile");
            webResourceManager.requireResourcesForContext("jira.userprofile");
            webResourceManager.requireResource("jira.webresources:userprofile");
        }

        return super.doExecute();
    }

    private void parseAcceptsHeader()
    {
        final String accepts = defaultString(request.getHeader("Accept")).toLowerCase();
        if (accepts.contains("application/json"))
        {
            this.asJson = true;
        }
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public void setContentOnly(boolean contentOnly)
    {
        this.contentOnly = contentOnly;
    }

    public boolean isNoTitle()
    {
        final String NO_TITLE = "noTitle";
        boolean descriptorNoTitle = getSelectedProfilePanelDescriptor().getParams().containsKey(NO_TITLE) && "true".equalsIgnoreCase(getSelectedProfilePanelDescriptor().getParams().get(NO_TITLE));

        return noTitle || descriptorNoTitle;
    }

    public void setNoTitle(boolean noTitle)
    {
        this.noTitle = noTitle;
    }

    public String getAvatarUrl(User user)
    {
        if(user != null)
        {
            return avatarService.getAvatarURL(getLoggedInApplicationUser(), ApplicationUsers.from(user)).toString();
        }
        return null;
    }

    public User getUser()
    {
        if (profileUser == null)
        {
            if (name == null)
            {
                profileUser = getLoggedInUser();
            }
            else
            {
                profileUser = crowdService.getUser(name);
            }
        }
        return profileUser;
    }

    public ViewProfilePanelModuleDescriptor getSelectedProfilePanelDescriptor()
    {
        return getSelectedTabPanel();
    }

    public String getLabelForSelectedTab()
    {
        final ViewProfilePanelModuleDescriptor moduleDescriptor = getSelectedProfilePanelDescriptor();

        return moduleDescriptor.getName();
    }

    public String getHtmlForSelectedTab()
    {
        final ViewProfilePanelModuleDescriptor moduleDescriptor = getSelectedProfilePanelDescriptor();

        final String tabHtml = moduleDescriptor.getModule().getHtml(profileUser);
        final StringBuilder strBuilder = new StringBuilder();

        strBuilder.append(tabHtml);

        if (contentOnly)
        {
            // Put this after the HTML so that we don't have to come up with crazy css
            // selector to skip over resources that may or may not get dynamically injected
            // into the page.
            final SuperBatchFilteringWriter writer = new SuperBatchFilteringWriter();
            webResourceManager.includeResources(writer, UrlMode.AUTO);
            strBuilder.append(writer.toString());
        }

        return strBuilder.toString();
    }

    public String getTabHtmlForJSON() throws IOException
    {
        final String tabPanelHTML = getHtmlForSelectedTab();

        return JSONEscaper.escape(tabPanelHTML);
    }

    public boolean isHasMoreThanOneProfileTabs()
    {
        return getTabDescriptors().size() > 1;
    }

    @Override
    protected boolean canSeeTab(String key)
    {
        for (ViewProfilePanelModuleDescriptor descriptor : getTabPanels())
        {
            if (descriptor.getCompleteKey().equals(key))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected List<ViewProfilePanelModuleDescriptor> getTabPanelModuleDescriptors()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(ViewProfilePanelModuleDescriptor.class);
    }

    @Override
    protected boolean isTabPanelHidden(ViewProfilePanelModuleDescriptor descriptor) throws PermissionException
    {
        return false;
    }

    public List<ViewProfilePanelModuleDescriptor> getTabDescriptors()
    {
        return getTabPanels();
    }

    @Override
    protected List<ViewProfilePanelModuleDescriptor> initTabPanels()
    {
        final List<ViewProfilePanelModuleDescriptor> allDescriptors = super.initTabPanels();
        final List<ViewProfilePanelModuleDescriptor> moduleDescriptors = new ArrayList<ViewProfilePanelModuleDescriptor>();

        for (ViewProfilePanelModuleDescriptor descriptor : allDescriptors)
        {
            final ViewProfilePanel profilePanel = descriptor.getModule();
            if (profilePanel instanceof OptionalUserProfilePanel)
            {
                final OptionalUserProfilePanel optionalPanel = (OptionalUserProfilePanel) profilePanel;
                if (optionalPanel.showPanel(getUser(), getLoggedInUser()))
                {
                    moduleDescriptors.add(descriptor);
                }
            }
            else
            {
                moduleDescriptors.add(descriptor);
            }
        }
        Collections.sort(moduleDescriptors, ModuleDescriptorComparator.COMPARATOR);

        return moduleDescriptors;
    }

    public List<SimpleLinkSection> getSectionsForMenu()
    {
        final List<SimpleLinkSection> sections = new ArrayList<SimpleLinkSection>();
        sections.add(new SimpleLinkSectionImpl("operations", getText("common.concepts.tools"), null, null, "icon-tools", null));

        return sections;
    }

    public List<SimpleLink> getSectionLinks(String key)
    {
        final User remoteUser = getLoggedInUser();
        final HttpServletRequest servletRequest = ServletActionContext.getRequest();
        servletRequest.setAttribute(UserIsTheLoggedInUserCondition.PROFILE_USER, getUser());

        final Map<String, Object> params = MapBuilder.<String, Object>build(UserIsTheLoggedInUserCondition.PROFILE_USER, getUser());

        final JiraHelper helper = new JiraHelper(servletRequest, null, params);

        return simpleLinkManager.getLinksForSection("system.user.profile.links/" + key, remoteUser, helper);
    }

}
