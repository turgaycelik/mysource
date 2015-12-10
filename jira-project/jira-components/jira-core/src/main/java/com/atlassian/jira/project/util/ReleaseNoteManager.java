/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import javax.annotation.Nonnull;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static com.atlassian.jira.template.TemplateSources.file;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ReleaseNoteManager
{
    private static final Logger log = Logger.getLogger(ReleaseNoteManager.class);

    public static final String RELEASE_NOTE_NAME = "jira.releasenotes.templatenames";
    public static final String RELEASE_NOTE_DEFAULT = "jira.releasenotes.default";
    public static final String RELEASE_NOTE_TEMPLATE = "jira.releasenotes.templates";
    public static final String TEMPLATES_DIR = "templates/jira/project/releasenotes/";

    private Map<String, String> styles;

    private final ApplicationProperties applicationProperties;
    private final VelocityTemplatingEngine templatingEngine;
    private final ConstantsManager constantsManager;
    private final SearchProvider searchProvider;
    private final CustomFieldManager customFieldManager;

    public ReleaseNoteManager(final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final ConstantsManager constantsManager, final SearchProvider searchProvider,
            final CustomFieldManager customFieldManager)
    {
        this.applicationProperties = applicationProperties;
        this.templatingEngine = templatingEngine;
        this.constantsManager = constantsManager;
        this.searchProvider = searchProvider;
        this.customFieldManager = customFieldManager;
    }

    public Map<String, String> getStyles()
    {
        if (styles == null)
        {
            loadReleaseNoteTemplates();
        }
        return styles;
    }

    private List<String> splitString(final String strings)
    {
        if (strings == null)
        {
            return Collections.emptyList();
        }

        final List<String> stringList = newArrayList();
        final StringTokenizer tokenizer = new StringTokenizer(strings, ",");
        while (tokenizer.hasMoreTokens())
        {
            stringList.add(tokenizer.nextToken().trim());
        }
        return stringList;
    }

    private void loadReleaseNoteTemplates()
    {
        final List<String> allReleaseNoteNames = splitString(applicationProperties.getDefaultBackedString(RELEASE_NOTE_NAME));
        final List<String> allReleaseNoteTemplates = splitString(applicationProperties.getDefaultBackedString(RELEASE_NOTE_TEMPLATE));
        if (allReleaseNoteTemplates.size() != allReleaseNoteNames.size())
        {
            throw new RuntimeException("Error loading release notes; differing numbers of names and templates specified in properties file.");
        }

        styles = new HashMap<String, String>(allReleaseNoteTemplates.size());

        for (int i = 0; i < allReleaseNoteNames.size(); i++)
        {
            styles.put(allReleaseNoteNames.get(i), allReleaseNoteTemplates.get(i));
        }
    }

    /**
     * Returns a release note for this version, using the specified releaseNoteStyleName.  The issues returned will be
     * the issues that the user has permission to see.
     *
     * @throws IllegalArgumentException if there is no matching template for this releaseNoteStyleName
     */
    public String getReleaseNote(final Action action, final String releaseNoteStyleName, final Version version,
            final User user, final GenericValue project)
    {
        try
        {
            String templateName = getStyles().get(releaseNoteStyleName);

            // use Default
            if (templateName == null)
            {
                final String defaultType = applicationProperties.getDefaultBackedString(RELEASE_NOTE_DEFAULT);
                if (isNotBlank(defaultType))
                {
                    templateName = getStyles().get(defaultType);
                }
            }

            // use first
            if (templateName == null)
            {
                templateName = getFirstStyle();
            }

            if (templateName == null)
            {
                log.error("No styles available for release notes");
                throw new IllegalArgumentException("No styles available for release notes");
            }

            final Map<String, Object> templateVariables = getTemplateVariables(action, version, user, project);

            return templatingEngine.render(file(TEMPLATES_DIR + templateName)).applying(templateVariables).asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Exception occurred while attempting to get velocity body for release note template.", e);
            return null;
        }
    }

    private String getFirstStyle()
    {
        final Collection<String> values = getStyles().values();
        if (values != null)
        {
            final Iterator iterator = values.iterator();
            if (iterator.hasNext())
            {
                return (String) iterator.next();
            }
        }
        return null;
    }

    private Map<String, Object> getTemplateVariables(final Action action, final Version version, final User user, final GenericValue project)
    {
        final List<IssuesByType> issueTypes = newArrayList();
        for (final IssueType  issueType : constantsManager.getAllIssueTypeObjects())
        {
            issueTypes.add(new IssuesByType(issueType, user, version.getLong("id")));
        }
        return MapBuilder.<String,Object>newBuilder().
                add("action", action).
                add("req", ExecutingHttpRequest.get()).
                add("issueTypes", issueTypes).
                add("appProps", applicationProperties).
                add("version", version.getName()).
                add("versionObj", version).
                add("project", project.getString("name")).
                add("textUtils", new TextUtils()).
                add("requestContext", new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext()).
                add("constantsManager", constantsManager).
                add("customFieldManager", customFieldManager).
                toMutableMap();
    }

    public class IssuesByType
    {
        private final IssueType issueType;
        private final User user;
        private final Long fixForVersion;
        private Collection<Issue> issues;

        private IssuesByType(final IssueType issueType, final User user, @Nonnull final Long fixForVersion)
        {
            this.issueType = issueType;
            this.user = user;
            this.fixForVersion = notNull("fixForVersion", fixForVersion);
        }

        public String getName()
        {
            return issueType.getNameTranslation();
        }

        public Collection<Issue> getIssues()
        {
            if (issues == null)
            {
                final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
                queryBuilder.where().fixVersion(fixForVersion).and().issueType(issueType.getId());
                queryBuilder.orderBy().issueKey(SortOrder.ASC);
                try
                {
                    issues = searchProvider.search(queryBuilder.buildQuery(), user, PagerFilter.getUnlimitedFilter()).getIssues();
                }
                catch (SearchException e)
                {
                    log.error("Error searching for issues", e);
                }
            }
            return issues;
        }
    }
}
