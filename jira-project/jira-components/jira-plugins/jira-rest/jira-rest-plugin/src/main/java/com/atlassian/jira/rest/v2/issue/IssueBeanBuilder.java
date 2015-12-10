package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.workflow.IssueWorkflowManager;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.log4j.Logger;

import javax.ws.rs.core.UriBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Builder for {@link IssueBean} instances.
 *
 * @since v5.0
 */
public class IssueBeanBuilder
{
    private static final Logger LOG = Logger.getLogger(IssueBeanBuilder.class);

    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext authContext;
    private final FieldManager fieldManager;
    private final ResourceUriBuilder resourceUriBuilder;
    private final BeanBuilderFactory beanBuilderFactory;
    private final ContextUriInfo contextUriInfo;
    private final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    private final IssueWorkflowManager issueWorkflowManager;

    /**
     * The list of fields to include in the bean. If null, include all fields.
     */
    private IncludedFields fieldsToInclude;
    /**
     * the expand query string
     */
    private String expand;

    /**
     * The UriInfo to use when generating links. Will use ContextUriInfo unless one is
     * explicitly specified via {@link #uriBuilder(javax.ws.rs.core.UriBuilder)}
     */
    private UriBuilder uriBuilder;

    /**
     * The issue for which to build the bean.
     */
    private final Issue issue;

    public IssueBeanBuilder(
            final FieldLayoutManager fieldLayoutManager,
            final JiraAuthenticationContext authContext,
            final FieldManager fieldManager,
            final ResourceUriBuilder resourceUriBuilder,
            final BeanBuilderFactory beanBuilderFactory,
            final ContextUriInfo contextUriInfo,
            final Issue issue, IncludedFields fieldsToInclude,
            final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory,
            final IssueWorkflowManager issueWorkflowManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.authContext = authContext;
        this.fieldManager = fieldManager;
        this.resourceUriBuilder = resourceUriBuilder;
        this.beanBuilderFactory = beanBuilderFactory;
        this.contextUriInfo = contextUriInfo;
        this.issue = issue;
        this.fieldsToInclude = fieldsToInclude;
        this.issueLinkBeanBuilderFactory = issueLinkBeanBuilderFactory;
        this.issueWorkflowManager = issueWorkflowManager;
    }

    public IssueBeanBuilder expand(String expand)
    {
        this.expand = expand;
        return this;
    }

    public IssueBeanBuilder uriBuilder(final UriBuilder uriBuilder)
    {
        this.uriBuilder = uriBuilder;
        return this;
    }

    public IssueBean build()
    {
        final IssueBean bean = new IssueBean(issue.getId(), issue.getKey(), resourceUriBuilder.build(uriBuilder == null ? contextUriInfo.getBaseUriBuilder() : uriBuilder, IssueResource.class, String.valueOf(issue.getId())));
        bean.fieldsToInclude(fieldsToInclude);

        addFields(issue, bean);
        addParentLink(issue, bean);
        addTransitions(issue, bean);
        addOpsbar(issue, bean);

        if (expand != null && expand.contains("editmeta"))
        {
            final EditMetaBean editmeta = beanBuilderFactory.newEditMetaBeanBuilder()
                    .issue(issue)
                    .fieldsToInclude(fieldsToInclude)
                    .build();
            bean.editmeta(editmeta);
        }

        if (expand != null && expand.contains("changelog"))
        {
            bean.changelog(beanBuilderFactory.newChangelogBeanBuilder().build(issue));
        }
        return bean;
    }

    private void addTransitions(Issue issue, IssueBean bean)
    {
        if (isIncludeTransitions())
        {
            List<ActionDescriptor> sortedAvailableActions = issueWorkflowManager.getSortedAvailableActions(issue, authContext.getUser());
            List<TransitionBean> transitionBeans = new ArrayList<TransitionBean>();
            for (ActionDescriptor action : sortedAvailableActions)
            {
                TransitionBean transitionMetaBean = beanBuilderFactory.newTransitionMetaBeanBuilder()
                        .issue(issue)
                        .action(action)
                        .build();
                transitionBeans.add(transitionMetaBean);
            }
            bean.setTransitionBeans(transitionBeans);
        }
    }

    private void addOpsbar(Issue issue, IssueBean bean)
    {
        if (isIncludeOpsbar())
        {
            OpsbarBeanBuilder opsbarBeanBuilder = beanBuilderFactory.newOpsbarBeanBuilder(issue);
            bean.setOperations(opsbarBeanBuilder.build());
        }
    }

    private void addParentLink(Issue issue, IssueBean bean)
    {
        final IssueLinksBeanBuilder builder = issueLinkBeanBuilderFactory.newIssueLinksBeanBuilder(issue);
        final IssueRefJsonBean parentLink = builder.buildParentLink();
        if (parentLink != null)
        {
            bean.addParentField(parentLink, authContext.getI18nHelper().getText("issue.field.parent"));
        }
    }

    private void addFields(final Issue issue, final IssueBean bean)
    {
        // iterate over all the visible layout items from the field layout for this issue and attempt to add them
        // to the result
        final boolean includeRenderableFields = isIncludeRenderableFields();
        final FieldLayout layout = fieldLayoutManager.getFieldLayout(issue);
        final List<FieldLayoutItem> fieldLayoutItems = layout.getVisibleLayoutItems(issue.getProjectObject(), CollectionBuilder.list(issue.getIssueTypeObject().getId()));

        final Predicate<Field> fieldIncluded = fieldsToInclude == null ? Predicates.<Field>alwaysTrue() : new Predicate<Field>()
        {
            @Override
            public boolean apply(final Field field)
            {
                return fieldsToInclude.included(field);
            }
        };
        final Predicate<FieldLayoutItem> fieldLayoutItemIncluded = fieldsToInclude == null ? Predicates.<FieldLayoutItem>alwaysTrue() : Predicates.compose(fieldIncluded, GET_ORDERABLE_FIELD);

        for (final FieldLayoutItem fieldLayoutItem : Iterables.filter(fieldLayoutItems, fieldLayoutItemIncluded))
        {
            final OrderableField field = fieldLayoutItem.getOrderableField();
            final FieldJsonRepresentation fieldValue = getFieldValue(fieldLayoutItem, issue);
            if (fieldValue != null && fieldValue.getStandardData() != null)
            {
                bean.addField(field, fieldValue, includeRenderableFields);
            }
        }

        // Then we try to add "NavigableFields" which aren't "OrderableFields" unless they ae special ones.
        // These aren't included in the Field Layout.
        // This is a bit crap because "getAvailableNavigableFields" doesn't take the issue into account.
        // All it means is the field is not hidden in at least one project the user has BROWSE permission on.
        try
        {
            final Set<NavigableField> fields = fieldManager.getAvailableNavigableFields(authContext.getLoggedInUser());
            for (NavigableField field : Iterables.filter(fields, fieldIncluded))
            {
                if (!bean.hasField(field.getId()))
                {
                    if (!(field instanceof OrderableField) || isSpecialField(field))
                    {
                        if (field instanceof RestAwareField)
                        {
                            addRestAwareField(issue, bean, field, (RestAwareField) field);
                        }
                    }
                }
            }
        }
        catch (FieldException e)
        {
            // ignored...display as much as we can.
        }

    }

    /**
     * Returns tru if this is a special field that cannot appear in the field config for some special reason
     *
     * @param field the field
     * @return true if the field is special
     */
    private boolean isSpecialField(NavigableField field)
    {
        // At the moment only the Project System Field is special in this respect
        return field instanceof ProjectSystemField;
    }

    private void addRestAwareField(Issue issue, IssueBean bean, Field field, RestAwareField restAware)
    {
        final boolean includeRenderableFields = isIncludeRenderableFields();
        FieldJsonRepresentation fieldJsonFromIssue = restAware.getJsonFromIssue(issue, includeRenderableFields, null);
        if (fieldJsonFromIssue != null && fieldJsonFromIssue.getStandardData() != null)
        {
            bean.addField(field, fieldJsonFromIssue, includeRenderableFields);
        }
    }

    private boolean isIncludeRenderableFields()
    {
        return (expand != null && expand.contains("renderedFields"));
    }

    private boolean isIncludeTransitions()
    {
        return (expand != null && expand.contains("transitions"));
    }

    private boolean isIncludeOpsbar()
    {
        return (expand != null && expand.contains("operations"));
    }

    FieldJsonRepresentation getFieldValue(final FieldLayoutItem fieldLayoutItem, final Issue issue)
    {
        final OrderableField field = fieldLayoutItem.getOrderableField();
        if (field instanceof RestAwareField)
        {
            RestAwareField restAware = (RestAwareField) field;
            try
            {
                return restAware.getJsonFromIssue(issue, isIncludeRenderableFields(), fieldLayoutItem);
            }
            catch (RuntimeException e)
            {
                if(LOG.isDebugEnabled())
                {   LOG.debug(String.format("Cannot get value from RestAwareField %s, exception: '%s' ", field.getId(),e.getMessage()),e);

                }else{
                    LOG.info(String.format("Cannot get value from RestAwareField %s, exception: '%s' ", field.getId(),e.getMessage()));
                }
                return null;
            }
        }
        else
        {
            LOG.info(String.format("OrderableField %s not rendered in JSON", field.getId()));
            return null;
        }
    }

    private static final Function<FieldLayoutItem, Field> GET_ORDERABLE_FIELD = new Function<FieldLayoutItem, Field>()
    {
        @Override
        public Field apply(final FieldLayoutItem fieldLayoutItem)
        {
            return fieldLayoutItem.getOrderableField();
        }
    };
}
