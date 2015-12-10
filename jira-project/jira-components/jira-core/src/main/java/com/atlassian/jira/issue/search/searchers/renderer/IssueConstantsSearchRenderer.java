package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.comparator.ConstantsComparator;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.SearchContextRenderHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.emptyList;

/**
 * Provides the search renderer for issue constants (priority, status, resolution).
 *
 * @since v4.0
 */
public abstract class IssueConstantsSearchRenderer<T extends IssueConstant> extends AbstractSearchRenderer implements SearchRenderer
{
    private final SimpleFieldSearchConstants constants;
    private final ConstantsManager constantsManager;
    private final FieldVisibilityManager fieldVisibilityManager;

    public IssueConstantsSearchRenderer(SimpleFieldSearchConstants constants, String searcherNameKey, ConstantsManager constantsManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine, FieldVisibilityManager fieldVisibilityManager)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, constants, searcherNameKey);
        this.constants = constants;
        this.constantsManager = constantsManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public abstract Collection<T> getSelectListOptions(SearchContext searchContext);

    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        Collection<String> selectedValues = (Collection<String>) fieldValuesHolder.get(constants.getUrlParameter());
        velocityParams.put("selectedValues", selectedValues);
        Collection<T> validSelectListOptions = getSelectListOptions(searchContext);

        SortedSet<T> invalidSelections = getInvalidSelections(selectedValues, validSelectListOptions, getAllSelectListOptions());
        velocityParams.put("invalidSelections", invalidSelections);
        if (!invalidSelections.isEmpty())
        {
            SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParams);
        }

        velocityParams.put("selectListOptions", validSelectListOptions);
        return renderEditTemplate("constants-searcher-edit.vm", velocityParams);
    }

    protected Collection<T> getAllSelectListOptions()
    {
        return getSelectListOptions(new SearchContextImpl(emptyList(), emptyList(), emptyList()));
    }

    private SortedSet<T> getInvalidSelections(Collection<String> selectedKeys, Collection<T> validOptions, Collection<T> allOptions)
    {
        SortedSet<T> invalidOptions = new TreeSet<T>(ConstantsComparator.COMPARATOR);
        if (selectedKeys != null) {
            for (T selectListOption : allOptions)
            {
                if (!validOptions.contains(selectListOption) && selectedKeys.contains(selectListOption.getId()))
                {
                    invalidOptions.add(selectListOption);
                }
            }
        }
        return invalidOptions;
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(constants.getFieldId(), searchContext, user);
    }

    public String getViewHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        final Collection selectedValues = (Collection) fieldValuesHolder.get(constants.getUrlParameter());
        SortedSet<T> invalidSelections = getInvalidSelections(selectedValues, getSelectListOptions(searchContext), getAllSelectListOptions());
        if (!invalidSelections.isEmpty())
        {
            SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParams);
        }
        velocityParams.put("selectedObjects", constantsManager.convertToConstantObjects(constants.getUrlParameter(), selectedValues));
        velocityParams.put("invalidSelections", invalidSelections);
        return renderViewTemplate("constants-searcher-view.vm", velocityParams);
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return isRelevantForQuery(constants.getJqlClauseNames(), query);
    }
}
