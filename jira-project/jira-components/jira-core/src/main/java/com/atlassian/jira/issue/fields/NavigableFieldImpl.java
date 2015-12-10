/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.apache.log4j.Logger;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.apache.velocity.exception.VelocityException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

public abstract class NavigableFieldImpl extends AbstractField implements NavigableField
{
    private static final Logger log = Logger.getLogger(NavigableFieldImpl.class);

    private final String columnHeadingKey;
    protected final VelocityTemplatingEngine templatingEngine;
    protected final ApplicationProperties applicationProperties;

    private String defaultSortOrder;

    public NavigableFieldImpl(final String id, final String nameKey, final String columnHeadingKey,
            final VelocityTemplatingEngine templatingEngine, final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext authenticationContext)
    {
        super(id, nameKey, authenticationContext);
        this.columnHeadingKey = columnHeadingKey;
        this.templatingEngine = templatingEngine;
        this.applicationProperties = applicationProperties;
        defaultSortOrder = null;
    }

    public NavigableFieldImpl(final String id, final String nameKey, final String columnHeadingKey,
            final String defaultSortOrder, final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext)
    {
        super(id, nameKey, authenticationContext);
        this.columnHeadingKey = columnHeadingKey;
        this.templatingEngine = templatingEngine;
        this.applicationProperties = applicationProperties;
        this.defaultSortOrder = defaultSortOrder;
    }

    public String getColumnHeadingKey()
    {
        return columnHeadingKey;
    }

    public String getColumnCssClass()
    {
        return getId();
    }

    public String getDefaultSortOrder()
    {
        return defaultSortOrder;
    }

    void setDefaultSortOrder(final String s)
    {
        defaultSortOrder = s;
    }

    /**
     * A default implementation that returns a {@link MappedSortComparator} from {@link #getSorter()}.
     */
    public FieldComparatorSource getSortComparatorSource()
    {
        final LuceneFieldSorter sorter = getSorter();
        if (sorter == null)
        {
            return null;
        }
        else
        {
            return new MappedSortComparator(sorter);
        }
    }

    @Override
    public List<SortField> getSortFields(boolean sortOrder)
    {
        final FieldComparatorSource sorter = getSortComparatorSource();
        List<SortField> sortFields = new ArrayList<SortField>();
        if (sorter != null)
        {
            // lucene needs a field name. In some cases however, we don't have one. as it just caches the
            // ScoreDocComparator for each field (and we can assume these are the same for a given field, we can
            // just put the field name here if it isn't found.
            String fieldName = getSorter() != null ? getSorter().getDocumentConstant() : "field_" + getId();
            SortField sortField = new SortField(fieldName, sorter, sortOrder);
            sortFields.add(sortField);
        }
        return sortFields;
    }

    protected String renderTemplate(final String template, final Map velocityParams)
    {
        try
        {
            return templatingEngine.render(file(TEMPLATE_DIRECTORY_PATH + template)).applying(velocityParams).asHtml();
        }
        catch (final VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + TEMPLATE_DIRECTORY_PATH + "/" + template + "'.", e);
        }

        return "";
    }

    protected Map<String, Object> getVelocityParams(final FieldLayoutItem fieldLayoutItem, final I18nHelper i18nHelper, final Map displayParams, final Issue issue)
    {
        final Map<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put("req", ExecutingHttpRequest.get()); // todo: remove when no longer used
        velocityParams.put("field", this);
        velocityParams.put("i18n", i18nHelper);
        velocityParams.put("urlcodec", new URLCodec()); // TODO: this should probably be named differently from Velocity usages of JiraUrlCodec
        velocityParams.put("issue", issue);
        velocityParams.put("displayParams", displayParams);
        velocityParams.put("fieldLayoutItem", fieldLayoutItem);
        return CompositeMap.of(velocityParams, JiraVelocityUtils.getDefaultVelocityParams(authenticationContext));
    }

    public String getHiddenFieldId()
    {
        // The fields that extend this class usually are not backed by orderable field and therefore cannot be hidden.
        return null;
    }

    protected ApplicationProperties getApplicationProperties()
    {
        return applicationProperties;
    }

    public String prettyPrintChangeHistory(final String changeHistory)
    {
        return changeHistory;
    }

    public String prettyPrintChangeHistory(final String changeHistory, final I18nHelper i18nHelper)
    {
        return changeHistory;
    }
}
