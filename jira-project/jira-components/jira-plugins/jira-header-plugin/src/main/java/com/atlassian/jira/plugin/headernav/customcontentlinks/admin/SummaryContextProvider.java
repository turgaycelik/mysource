package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.plugin.headernav.customcontentlinks.CustomContentLinkServiceFactory;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLink;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: tdavies Date: 19/11/12 Time: 2:44 PM To change this template use File | Settings |
 * File Templates.
 */
public class SummaryContextProvider implements CacheableContextProvider
{
    static final Pattern PATTERN = Pattern.compile("/*project-config/+([^/]+)");
    private final ProjectManager projectManager;
    private final CustomContentLinkServiceFactory customContentLinkServiceFactory;
    private final VelocityRequestContextFactory requestContextFactory;
    private ContentLinkAdminDescriptionProvider contentLinkAdminDescriptionProvider;

    public SummaryContextProvider(
            final ProjectManager projectManager,
            final CustomContentLinkServiceFactory customContentLinkServiceFactory,
            final VelocityRequestContextFactory requestContextFactory,
            ContentLinkAdminDescriptionProvider contentLinkAdminDescriptionProvider)
    {
        this.projectManager = projectManager;
        this.customContentLinkServiceFactory = customContentLinkServiceFactory;
        this.requestContextFactory = requestContextFactory;
        this.contentLinkAdminDescriptionProvider = contentLinkAdminDescriptionProvider;
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getProjectKey();
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        // nothing required
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        Map<String,Object> contextMap = new HashMap<String, Object>(context);
        List<CustomContentLink> allLinks = customContentLinkServiceFactory.getCustomContentLinkService().getCustomContentLinks(getProjectKey());
        List<CustomContentLink> shownLinks = allLinks.subList(0, Math.min(5, allLinks.size()));
        contextMap.put("totalSize", allLinks.size());
        contextMap.put("shownSize", shownLinks.size());
        contextMap.put("links", shownLinks);
        contextMap.put("manageCustomContentLinksLink", requestContextFactory.getJiraVelocityRequestContext().getBaseUrl() + "/plugins/servlet/custom-content-links-admin?entityKey=" + getProjectKey());
        contextMap.put("description", contentLinkAdminDescriptionProvider.getDescription());
        return contextMap;
    }

    private String getProjectKey() {
        HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null) {
            final Matcher matcher = PATTERN.matcher(request.getPathInfo());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new IllegalArgumentException("Expected URL '" + request.getPathInfo() + " to match /project-config/KEY");
            }
        } else {
            throw new IllegalStateException("No HTTP request is running");
        }
    }
}
