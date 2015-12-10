package com.atlassian.jira.web.sitemesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

/**
 * Class that holds all the logic for the projectconfig decorator.
 *
 * @since v4.4
 */
public class AdminDecoratorHelper
{
    public static final String SYSTEM_ADMIN_TOP_NAVIGATION_BAR = "system.admin.top.navigation.bar";
    public static final String SYSTEM_ADMIN_TOP = "system.admin.top";
    public static final String ACTIVE_SECTION_KEY = "admin.active.section";
    public static final String ACTIVE_TAB_LINK_KEY = "admin.active.tab";
    private static final Logger log = LoggerFactory.getLogger(AdminDecoratorHelper.class);
    private static final String NAV_PANEL_LOCATION = "atl.jira.proj.config.sidebar";
    private static final String PROJECT_CONFIG = "atl.jira.proj.config";

    private final WebInterfaceManager webInterfaceManager;
    private final ProjectService service;
    private final JiraAuthenticationContext authCtx;
    private final SimpleLinkManager linkManager;
    private final SoyTemplateRenderer soyTemplateRenderer;

    private String projectKey;
    private String currentTab;
    private String currentSection;
    private String selectedMenuSection;

    private ProjectService.GetProjectResult result;
    private List<Header> headers;
    private String headerHtml;
    private Map<String,Object> sideMenuSoyRenderData;
    private LinkedList<PathSegment> pathToLink;

    public AdminDecoratorHelper(WebInterfaceManager webInterfaceManager, ProjectService projectService,
            JiraAuthenticationContext authenticationContext,
            SimpleLinkManager linkManager, SoyTemplateRendererProvider soyTemplateRendererProvider)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.service = projectService;
        this.authCtx = authenticationContext;
        this.linkManager = linkManager;
        this.soyTemplateRenderer = soyTemplateRendererProvider.getRenderer();
        // The sideMenuSoyRenderData will be instantiated when we call setCurrentTab shortly. Don't do it in the constructor.
    }

    public boolean isProjectAdministration()
    {
        return getProject() != null;
    }

    public boolean isAdminMenusAllHidden()
    {
        return (getSelectedMenuSection() == null || getSelectedMenuSection().equals(SYSTEM_ADMIN_TOP_NAVIGATION_BAR));
    }

    public AdminDecoratorHelper setProject(String projectKey)
    {
        clearCache();
        this.projectKey = StringUtils.stripToNull(projectKey);
        return this;
    }

    public AdminDecoratorHelper setCurrentTab(String currentTab)
    {
        clearCache();
        this.currentTab = StringUtils.stripToNull(currentTab);
        this.sideMenuSoyRenderData = makeSideMenuSoyRenderData(null, null,SYSTEM_ADMIN_TOP, SYSTEM_ADMIN_TOP_NAVIGATION_BAR, null, 0);
        return this;
    }

    public AdminDecoratorHelper setCurrentSection(String currentSection)
    {
        clearCache();

        this.currentSection = StringUtils.stripToNull(currentSection);
        if(StringUtils.contains(currentSection, "/"))
        {
            this.selectedMenuSection = currentSection.substring(0, currentSection.indexOf("/"));
        }
        else
        {
            this.selectedMenuSection = currentSection;
        }
        //Project config admin pages need to highlight the project menu!
        if(StringUtils.equals(PROJECT_CONFIG, selectedMenuSection))
        {
            this.selectedMenuSection = "admin_project_current";
        }

        return this;
    }

    public String getSelectedMenuSection()
    {
        if (this.selectedMenuSection == null && pathToLink != null)
        {
            for (PathSegment pathSegment : pathToLink)
            {
                if (SYSTEM_ADMIN_TOP_NAVIGATION_BAR.equals(pathSegment.getParentSectionId()))
                {
                    this.selectedMenuSection = pathSegment.getSectionId();
                }
            }
        }
        return this.selectedMenuSection;
    }

    public boolean hasKey()
    {
        return projectKey != null;
    }

    public List<Header> getHeaders()
    {
        if (this.headers != null)
        {
            return this.headers;
        }

        Project project = getProject();
        Map<String, Object> context;
        String headerPanelSection;
        if (project == null)
        {
                List<Map<String,Object>> adminNavigationSections = getSoyReadyAdminNavigationSections();
                context = MapBuilder.build("admin.active.section", currentSection, "admin.active.tab", currentTab, "adminNavigationPrimary", adminNavigationSections);
                headerPanelSection = "system.admin.decorator.header";
        }
        else
        {
            List<Map<String,Object>> adminNavigationLinksPrimary = getSoyReadyNavigationLinksForSection("atj.jira.proj.nav.switcher", "project-navswitcher");
            List<Map<String,Object>> adminNavigationLinksSecondary = getSoyReadyNavigationLinksForSection("atj.jira.proj.nav.switcher", "project-navswitcher-secondary");
            context = new HashMap<String,Object>();
            context.put("project", project);
            context.put(ACTIVE_SECTION_KEY, currentSection);
            context.put(ACTIVE_TAB_LINK_KEY, currentTab);
            context.put("adminNavigationPrimary",adminNavigationLinksPrimary);
            context.put("adminNavigationSecondary",adminNavigationLinksSecondary);
            headerPanelSection = "atl.jira.proj.config.header";
        }
        final List<WebPanel> panels = webInterfaceManager.getDisplayableWebPanels(headerPanelSection, Collections.<String, Object>emptyMap());
        final List<Header> headers = new ArrayList<Header>(panels.size());
        for (WebPanel panel : panels)
        {
            headers.add(new Header(panel, context));
        }
        this.headers = headers;
        return this.headers;
    }

    private List<Map<String,Object>> getSoyReadyNavigationLinksForSection(String parentSection, String section)
    {
        List<SimpleLink> projectAdminLinks = getLinksForSection(parentSection,section);
        List<Map<String,Object>> soyedMainLinkSections = new ArrayList<Map<String,Object>>();
        if (projectAdminLinks == null)
        {
            return soyedMainLinkSections;
        }
        for (SimpleLink currentLink : projectAdminLinks)
        {
            Map<String,Object> soyedSection = new HashMap<String,Object>();
            final String id = currentLink.getId();
            soyedSection.put("id", id);
            soyedSection.put("link", currentLink.getUrl());
            soyedSection.put("label", currentLink.getLabel());
            soyedSection.put("isSelected", id.equalsIgnoreCase("project-admin-link"));
            if (StringUtils.isNotEmpty(currentLink.getStyleClass()))
            {
                soyedSection.put("styleClass",currentLink.getStyleClass());
            }
            soyedMainLinkSections.add(soyedSection);
        }
        return soyedMainLinkSections;
    }

    private List<Map<String,Object>> getSoyReadyAdminNavigationSections()
    {
        Iterable<SimpleLinkSection> mainAdminSections = Iterables.concat(Lists.transform(getChildSectionsForSection(SYSTEM_ADMIN_TOP), new Function<SimpleLinkSection, List<SimpleLinkSection>>()
        {
            @Override
            public List<SimpleLinkSection> apply(final SimpleLinkSection section)
            {
                return getChildSectionsForSection(section.getId());
            }
        }));
        List<Map<String,Object>> soyedMainLinkSections = new ArrayList<Map<String,Object>>();
        for (SimpleLinkSection currentSection : mainAdminSections)
        {
            Map<String,Object> soyedSection = new HashMap<String,Object>();
            SimpleLink firstLink = findFirstLinkForSection(currentSection,authCtx.getUser().getDirectoryUser(),getJiraHelper());
            if (firstLink == null)
            {
                firstLink = findFirstLinkForMainSection(currentSection, authCtx.getLoggedInUser(), getJiraHelper());
            }

            if (firstLink != null)
            {
                soyedSection.put("link",firstLink.getUrl());
                soyedSection.put("label",currentSection.getLabel());
                soyedSection.put("isSelected",currentSection.getId().equalsIgnoreCase(getSelectedMenuSection()));
                soyedMainLinkSections.add(soyedSection);
            }
        }
        return soyedMainLinkSections;
    }

    private SimpleLink findFirstLinkForMainSection(final SimpleLinkSection adminMainSection, final User user, final JiraHelper helper)
    {
        final List<SimpleLink> links = linkManager.getLinksForSection(adminMainSection.getId(), user, helper);
        if (!links.isEmpty())
        {
            return links.get(0);
        }

        return null;
    }

    @Nullable
    private SimpleLink findFirstLinkForSection(@Nonnull final SimpleLinkSection adminHeaderSection, @Nullable final User user, @Nonnull final JiraHelper helper)
    {
        final List<SimpleLinkSection> nonEmptySubSections = linkManager.getNotEmptySectionsForLocation(adminHeaderSection.getId(), user, helper);
        for (final SimpleLinkSection subSection : nonEmptySubSections)
        {
            if(PROJECT_CONFIG.equals(subSection.getId()))
            {
                continue;
            }
            final String subSectionKey = adminHeaderSection.getId() + "/" + subSection.getId();
            final List<SimpleLink> links = linkManager.getLinksForSection(subSectionKey, user, helper);
            if (!links.isEmpty())
            {
                return links.get(0);
            }
            final SimpleLink firstLinkOfSubSection = findFirstLinkForSection(subSection, user, helper);
            if (firstLinkOfSubSection != null)
            {
                return firstLinkOfSubSection;
            }
        }
        return null;
    }


    public String getHeaderHtml()
    {
        if (headerHtml != null)
        {
            return headerHtml;
        }
        StringBuilder builder = new StringBuilder();
        for (Header header : getHeaders())
        {
            builder.append(header.getHtml());
        }

        return headerHtml = trimToEmpty(builder.toString());
    }

    public boolean isHasHeader()
    {
        return isNotEmpty(getHeaderHtml());
    }

    public String getSideMenuHtml(String sectionId)
    {
        List<SimpleLinkSection> sections = this.getChildSectionsForSection(SYSTEM_ADMIN_TOP_NAVIGATION_BAR);
        SimpleLinkSection section = null;
        for (SimpleLinkSection simpleLinkSection: sections)
        {
            if (simpleLinkSection.getId().equals(sectionId))
            {
                section = simpleLinkSection;
                break;
            }
        }
        if (section == null)
        {
            return null;
        }
        String menuLabel = null;
        Map<String, Object> currentSectionMenuData = null;
        if (!StringUtils.equals("admin_project_current", sectionId))
        {
            menuLabel = section.getTitle() != null ? section.getTitle() : section.getLabel();
            if (sideMenuSoyRenderData != null)
            {
                for(Map<String, Object> sectionMenuData : (List<Map<String,Object>>) sideMenuSoyRenderData.get("sections"))
                {
                    if (sectionMenuData != null && sectionMenuData.get("sectionId").equals(sectionId))
                    {
                        currentSectionMenuData = sectionMenuData;
                        currentSectionMenuData.put("menuLabel",menuLabel);
                        currentSectionMenuData.remove("label");
                        break;
                    }
                }
            }
        }
        else
        {
            currentSectionMenuData = makeSideMenuSoyRenderData(null,null,SYSTEM_ADMIN_TOP_NAVIGATION_BAR,sectionId,null, 0);
        }
        if (currentSectionMenuData == null)
        {
            return null;
        }
        try
        {
            return soyTemplateRenderer.render("com.atlassian.jira.jira-header-plugin:admin-side-nav-menu","JIRA.Templates.Menu.admin.side.navigation.menusection", currentSectionMenuData);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for side admin menu");
            log.debug("Exception: ",e);
        }
        return null;
    }

    private Map<String, Object> makeSideMenuSoyRenderData(String title, String label, String parentSectionId, String sectionId, String sectionLabel, int weight)
    {
        if (!getSectionContainsAtleastOneLink(parentSectionId,sectionId))
        {
            return null;
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("title",title);
        data.put("label",label);
        data.put("menuLabel",sectionLabel);
        data.put("parentSectionId",parentSectionId);
        data.put("sectionId",sectionId);
        data.put("links",makeLinksForSection(parentSectionId,sectionId));
        data.put("sections",makeChildSectionsForSection(sectionId));
        data.put("selectedMenu",getSelectedMenuSection());
        data.put("weight",weight);
        return data;
    }

    private List<Map<String, Object>> makeChildSectionsForSection(String sectionId)
    {
        List<Map<String, Object>> sections = new ArrayList<Map<String, Object>>();
        for (SimpleLinkSection linkSection: getChildSectionsForSection(sectionId))
        {
            Map<String, Object> section = makeSideMenuSoyRenderData(linkSection.getTitle(), linkSection.getLabel(), sectionId, linkSection.getId(), null, linkSection.getWeight());
            if (section != null)
            {
                sections.add(section);
            }
        }
        if (sectionId.toLowerCase().equals(PROJECT_CONFIG))
        {
            sections.addAll(getProjectAdminNavPanelSections());
            Collections.sort(sections, new NavSectionComparator());
        }
        return sections;
    }

    private List<Map<String, Object>> getProjectAdminNavPanelSections()
    {
        final List<WebPanelModuleDescriptor> webPanelDescriptors = webInterfaceManager.getDisplayableWebPanelDescriptors(NAV_PANEL_LOCATION, Collections.<String, Object>emptyMap());
        final List<Map<String, Object>> panelSectionList = Lists.newArrayListWithExpectedSize(webPanelDescriptors.size());
        for (final WebPanelModuleDescriptor panelDescriptor : webPanelDescriptors)
        {
            CollectionUtils.addIgnoreNull(panelSectionList, SafePluginPointAccess.call(new Callable<Map<String, Object>>()
            {

                @Override
                public Map<String, Object> call() throws Exception
                {
                    return makeSideMenuSoyRenderDataForWebPanel(panelDescriptor);
                }
            }).getOrNull());
        }

        return panelSectionList;
    }

    private Map<String, Object> makeSideMenuSoyRenderDataForWebPanel(WebPanelModuleDescriptor panelDescriptor)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("title", panelDescriptor.getKey());
        data.put("sectionId", panelDescriptor.getKey());
        data.put("weight", panelDescriptor.getWeight());
        Map<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put("project", getProject());
        data.put("html", panelDescriptor.getModule().getHtml(contextMap));

        return data;
    }

    private List<Map<String, Object>> makeLinksForSection(String parentSectionId, String sectionId)
    {
        List<Map<String, Object>> links = new ArrayList<Map<String, Object>>();
        for (SimpleLink simpleLink: getLinksForSection(parentSectionId,sectionId))
        {
            Map<String, Object> link = new HashMap<String, Object>();
            link.put("url",simpleLink.getUrl());
            link.put("id",simpleLink.getId());
            link.put("label",simpleLink.getLabel());
            link.put("accessKey",simpleLink.getAccessKey());
            link.put("selected",isSelectedLink(simpleLink));
            links.add(link);
        }
        return links;
    }

    private boolean isSelectedLink(SimpleLink simpleLink)
    {
        boolean selected = (simpleLink.getId() != null && simpleLink.getId().equalsIgnoreCase(currentTab));

        if (!selected && StringUtils.isBlank(currentTab))
        {
            if (getHttpRequest().getRequestURI().endsWith(JiraUrl.extractActionFromURL(simpleLink.getUrl())))
            {
                selected = true;
            }
        }

        if (selected)
        {
            selectedMenuSection = findMenuSection(simpleLink);
            if (!StringUtils.isBlank(currentTab) && !getHttpRequest().getRequestURI().endsWith(JiraUrl.extractActionFromURL(simpleLink.getUrl())))
            {
                PathSegment pathSegment = pathToLink.getLast();
                pathToLink.add(new PathSegment(pathSegment.getSectionId(),currentTab,simpleLink.getLabel(),simpleLink.getUrl(),true));
            }
        }
        return selected;
    }

    private String findMenuSection(SimpleLink simpleLink)
    {
        List<PathSegment> pathToLink = getPathToLink(simpleLink);
        for (PathSegment pathSegment : pathToLink)
        {
            if (SYSTEM_ADMIN_TOP_NAVIGATION_BAR.equals(pathSegment.getParentSectionId()))
            {
                return pathSegment.getSectionId();
            }
        }
        return null;
    }

    private Project getProject()
    {
        ProjectService.GetProjectResult projectResult = getProjectResult();
        if (projectResult != null && projectResult.isValid())
        {
            return projectResult.getProject();
        }
        else
        {
            return null;
        }
    }

    private ProjectService.GetProjectResult getProjectResult()
    {
        if (result == null && hasKey())
        {
            result = service.getProjectByKeyForAction(authCtx.getLoggedInUser(), projectKey, ProjectAction.EDIT_PROJECT_CONFIG);
        }
        return result;
    }

    private void clearCache()
    {
        result = null;
        headers = null;
        headerHtml = null;
    }

    public HttpServletRequest getHttpRequest()
    {
        return ExecutingHttpRequest.get();
    }

    public static class Header
    {
        private final Map<String, Object> contextMap;
        private final WebPanel panel;

        private Header(WebPanel panel, Map<String, Object> contextMap)
        {
            this.contextMap = contextMap;
            this.panel = panel;
        }

        public String getHtml()
        {
            return panel.getHtml(contextMap);
        }
    }

    protected String encode(String string)
    {
        return JiraUrlCodec.encode(string, true);
    }

    private JiraHelper getJiraHelper()
    {
        final MapBuilder<String, Object> context = MapBuilder.newBuilder();
        context.add("project", getProject());
        if (getProject() != null)
        {
            context.add("projectKeyEncoded", encode(getProject().getKey()));
        }

        return new JiraHelper(getHttpRequest(), getProject(), context.toMap());
    }

    public List<SimpleLinkSection> getMainAdminSections()
    {
        return getChildSectionsForSection(SYSTEM_ADMIN_TOP_NAVIGATION_BAR);
    }

    public List<SimpleLinkSection> getChildSectionsForSection(String location)
    {
        return linkManager.getSectionsForLocation(location,authCtx.getLoggedInUser(),getJiraHelper());

    }

    public List<SimpleLink> getLinksForSection(String parentSection, String section)
    {
        if (parentSection.equals(PROJECT_CONFIG) && getProject() == null)
        {
            return new ArrayList<SimpleLink>();
        }
        final MapBuilder<String, Object> context = MapBuilder.newBuilder();
        context.add("project", getProject());
        if (getProject() != null)
        {
            context.add("projectKeyEncoded", encode(getProject().getKey()));
        }
        String sectionId = parentSection+"/"+section;
        return linkManager.getLinksForSection(sectionId,authCtx.getLoggedInUser(),getJiraHelper());
    }

    public boolean getSectionContainsAtleastOneLink(String parentSection, String section)
    {
        LinkedList<Pair<String,String>> parentAndSectionList = new LinkedList<Pair<String, String>>();
        parentAndSectionList.add(Pair.nicePairOf(parentSection,section));
        while (!parentAndSectionList.isEmpty())
        {
            final Pair<String,String> currentPair = parentAndSectionList.pop();
            final List<SimpleLinkSection> sectionList = getChildSectionsForSection(currentPair.second());
            if (sectionList != null)
            {
                for (final SimpleLinkSection aSectionList : sectionList)
                {
                    parentAndSectionList.addLast(Pair.of(currentPair.second(), aSectionList.getId()));
                }
            }
            final List<SimpleLink> links = getLinksForSection(currentPair.first(),currentPair.second());
            if (links != null && !links.isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    public LinkedList<PathSegment> getPathToLink(SimpleLink simpleLink)
    {
        if (pathToLink != null)
        {
            return pathToLink;
        }
        pathToLink = generatePathToLink(SYSTEM_ADMIN_TOP,simpleLink);
        return pathToLink;
    }

    private LinkedList<PathSegment> generatePathToLink(String section,SimpleLink simpleLink)
    {
        LinkedList<LinkedList<PathSegment>> potentiallPathsList = new LinkedList<LinkedList<PathSegment>>();
        LinkedList<PathSegment> sectionsList = new LinkedList<PathSegment>();
        sectionsList.add(new PathSegment(null,section,null,null,false));
        potentiallPathsList.add(sectionsList);
        while (!potentiallPathsList.isEmpty())
        {
            final LinkedList<PathSegment> currentList = potentiallPathsList.pop();
            final PathSegment currentPathSegment = currentList.getLast();
            for (final SimpleLinkSection simpleLinkSection : getChildSectionsForSection(currentPathSegment.getSectionId()))
            {
                boolean found = false;
                LinkedList<PathSegment> currentPathSegmentList = new LinkedList<PathSegment>();
                currentPathSegmentList.addAll(currentList);
                SimpleLinkSection currentLinkSection = simpleLinkSection;
                String name = currentLinkSection.getTitle() != null ? currentLinkSection.getTitle() : currentLinkSection.getLabel();
                List<SimpleLink> linksForSection = getLinksForSection(currentPathSegment.getSectionId(), currentLinkSection.getId());
                String linkForSection = null;
                if (linksForSection != null && !linksForSection.isEmpty())
                {
                    //linkForSection = linksForSection.get(0).getUrl();
                    if (linksForSection.contains(simpleLink))
                    {
                        found = true;
                    }
                }
                currentPathSegmentList.addLast(new PathSegment(currentPathSegment.getSectionId(), currentLinkSection.getId(), name, linkForSection, linkForSection != null));
                if (found)
                {
                    return currentPathSegmentList;
                }
                else
                {
                    potentiallPathsList.addLast(currentPathSegmentList);
                }
            }
        }
        return null;
    }

    private class PathSegment
    {
        private final String parentSectionId;
        private final String sectionId;
        private final String name;
        private final String link;
        private final boolean hasLink;

        public PathSegment(String parentSectionId, String sectionId, String name, String link, boolean hasLink)
        {
            this.parentSectionId = parentSectionId;
            this.sectionId = sectionId;
            this.name = name;
            this.link = link;
            this.hasLink = hasLink;
        }

        public String getParentSectionId()
        {
            return parentSectionId;
        }

        public String getSectionId()
        {
            return sectionId;
        }

        public String getName()
        {
            return name;
        }

        public String getLink()
        {
            return link;
        }

        public boolean isHasLink()
        {
            return hasLink;
        }
    }

    private static final class NavSectionComparator implements Comparator<Map<String, Object>>
    {
        @Override
        public int compare(final Map<String, Object> item1, final Map<String, Object> item2)
        {
            return ((Integer) item1.get("weight")).compareTo((Integer) item2.get("weight"));
        }
    }
}
