package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.SearchContextRenderHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.query.Query;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.builder.CompareToBuilder;
import webwork.action.Action;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A search renderer for the issue type field in the new issue navigator.
 *
 * @since v5.2
 */
public class IssueTypeSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    // Returns true iff the given option is special (e.g. "All Standard").
    private final Predicate SPECIAL_OPTION_PREDICATE = new Predicate()
    {
        @Override
        public boolean evaluate(Object object)
        {
            String optionId = ((Option)object).getId();
            return ConstantsManager.ALL_STANDARD_ISSUE_TYPES.equals(optionId) ||
                    ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES.equals(optionId);
        }
    };

    private final Comparator<Option> ISSUE_TYPE_OPTION_COMPARATOR = new Comparator<Option>()
    {
        @Override
        public int compare(Option first, Option second)
        {
            return new CompareToBuilder()
                    .append(first.getName(), second.getName())
                    .toComparison();
        }
    };

    private final ConstantsManager constantsManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final OptionSetManager optionSetManager;
    private final PermissionManager permissionManager;
    private final SubTaskManager subTaskManager;

    public IssueTypeSearchRenderer(
            final ApplicationProperties applicationProperties,
            final ConstantsManager constantsManager,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final OptionSetManager optionSetManager,
            final PermissionManager permissionManager,
            final SimpleFieldSearchConstants searchConstants,
            final String searcherNameKey,
            final SubTaskManager subTaskManager,
            final VelocityTemplatingEngine templatingEngine,
            final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        super(velocityRequestContextFactory, applicationProperties,
                templatingEngine, searchConstants, searcherNameKey);

        this.constantsManager = constantsManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.optionSetManager = optionSetManager;
        this.permissionManager = permissionManager;
        this.subTaskManager = subTaskManager;
    }

    /**
     * Construct edit HTML parameters and add them to a template parameters map.
     *
     * @param fieldValuesHolder Contains the values the user has selected.
     * @param searchContext The search context.
     * @param user The user performing the search.
     * @param velocityParameters The template parameters.
     */
    public void addEditParameters(final FieldValuesHolder fieldValuesHolder,
            final SearchContext searchContext,
            final User user,
            final Map<String, Object> velocityParameters)
    {
        Collection selectedOptions = (List)fieldValuesHolder.get(DocumentConstants.ISSUE_TYPE);
        velocityParameters.put("selectedOptions", selectedOptions);

        Collection<Option> validOptions = getVisibleOptions(user, searchContext);
        velocityParameters.put("optionCSSClasses", getOptionCSSClasses(validOptions));

        Collection<Option> invalidOptions = getInvalidOptions(user, selectedOptions, validOptions);
        SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParameters);
        velocityParameters.putAll(processOptions(validOptions, invalidOptions));
        velocityParameters.put("invalidOptions", invalidOptions);
    }

    /**
     * Construct view HTML parameters and add them to a template parameters map.
     *
     * @param fieldValuesHolder Contains the values the user has selected.
     * @param searchContext The search context.
     * @param user The user performing the search.
     * @param velocityParameters The template parameters.
     */
    public void addViewParameters(final FieldValuesHolder fieldValuesHolder,
            final SearchContext searchContext,
            final User user,
            final Map<String, Object> velocityParameters)
    {
        final Collection<String> issueTypes = new ArrayList<String>();
        final Collection<String> invalidIssueTypes = new ArrayList<String>();
        final Collection<String> issueTypeIds = new ArrayList<String>(
                (Collection)fieldValuesHolder.get(DocumentConstants.ISSUE_TYPE));

        // The IDs of all valid options in the search context.
        Collection<String> validOptionIds = Collections2.transform(
                getVisibleOptions(user, searchContext),
                new Function<Option, String>()
        {
            @Override
            public String apply(Option option)
            {
                return option.getId();
            }
        });

        if (issueTypeIds != null)
        {
            // Is "All Standard Issue Types" selected? It's always valid.
            String allStandard = ConstantsManager.ALL_STANDARD_ISSUE_TYPES;
            if (issueTypeIds.contains(allStandard))
            {
                issueTypeIds.remove(allStandard);
                issueTypes.add(getI18n(user).getText(
                        "common.filters.allstandardissuetypes"));
            }

            // Is "All Sub-Task Issue Types" selected? It's invalid if subtasks
            // are disabled or there are no subtask types in the search context.
            String allSubtasks = ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES;
            if (issueTypeIds.contains(allSubtasks))
            {
                String name = getI18n(user).getText(
                        "common.filters.allsubtaskissuetypes");

                issueTypes.add(name);
                issueTypeIds.remove(allSubtasks);

                if (!subTaskManager.isSubTasksEnabled() ||
                        !validOptionIds.contains(allSubtasks))
                {
                    invalidIssueTypes.add(name);
                }
            }

            if (!issueTypeIds.isEmpty())
            {
                for (IssueConstant issueConstant : constantsManager.convertToConstantObjects("IssueType", issueTypeIds))
                {
                    issueTypes.add(issueConstant.getNameTranslation());
                    if (!validOptionIds.contains(issueConstant.getId()))
                    {
                        invalidIssueTypes.add(issueConstant.getNameTranslation());
                    }
                }
            }
        }

        velocityParameters.put("invalidIssueTypes", invalidIssueTypes);
        velocityParameters.put("selectedIssueTypes", issueTypes);
        SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParameters);
    }

    @Override
    public String getEditHtml(User user,
            SearchContext searchContext,
            FieldValuesHolder fieldValuesHolder,
            Map<?, ?> displayParameters, Action action)
    {
        Map<String, Object> velocityParameters = getVelocityParams(user,
                searchContext, null, fieldValuesHolder, displayParameters, action);

        addEditParameters(fieldValuesHolder, searchContext, user, velocityParameters);
        return renderEditTemplate("issuetype-searcher" + EDIT_TEMPLATE_SUFFIX,
                velocityParameters);
    }

    @Override
    public boolean isShown(User user, SearchContext searchContext)
    {
        return true;
    }

    @Override
    public String getViewHtml(User user,
            SearchContext searchContext,
            FieldValuesHolder fieldValuesHolder,
            Map<?, ?> displayParameters,
            Action action)
    {
        Map<String, Object> velocityParameters = getVelocityParams(user,
                searchContext, null, fieldValuesHolder, displayParameters, action);

        addViewParameters(fieldValuesHolder, searchContext, user, velocityParameters);
        return renderViewTemplate("issuetype-searcher" + VIEW_TEMPLATE_SUFFIX,
                velocityParameters);
    }

    @Override
    public boolean isRelevantForQuery(User user, Query query)
    {
        return isRelevantForQuery(SystemSearchConstants.forIssueType().getJqlClauseNames(), query);
    }

    /**
     * @param i18nHelper An i18n helper.
     * @return All possible options (not just those that are visible).
     */
    private Collection<Option> getAllOptions(I18nHelper i18nHelper)
    {
        Collection<Option> allOptions = new ArrayList<Option>();
        allOptions.add(new TextOption(ConstantsManager.ALL_STANDARD_ISSUE_TYPES,
                i18nHelper.getText("common.filters.allstandardissuetypes")));
        allOptions.add(new TextOption(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES,
                i18nHelper.getText("common.filters.allsubtaskissuetypes")));

        allOptions.addAll(Collections2.transform(
                constantsManager.getAllIssueTypeObjects(),
                new Function<IssueConstant, Option>()
        {
            @Override
            public Option apply(@Nullable IssueConstant input)
            {
                return new IssueConstantOption(input);
            }
        }));

        return allOptions;
    }

    /**
     * @param user The user performing the search.
     * @param selectedOptions Options the user has selected.
     * @param validOptions All options that may be selected.
     * @return Invalid options that the user has selected.
     */
    private Collection<Option> getInvalidOptions(final User user,
            final Collection selectedOptions,
            final Collection<Option> validOptions)
    {
        Collection<Option> invalidOptions = new ArrayList<Option>();

        if (selectedOptions != null)
        {
            for (Option option : getAllOptions(getI18n(user)))
            {
                if (!validOptions.contains(option) &&
                        selectedOptions.contains(option.getId()))
                {
                    invalidOptions.add((Option)option);
                }
            }
        }

        return invalidOptions;
    }

    /**
     * Constructs a mapping of issue type options to their CSS classes.
     *
     * @param options The issue type options.
     * @return A map of options to CSS classes.
     */
    private Map<Option, String> getOptionCSSClasses(Collection<Option> options)
    {
        Map<Option, String> optionClassesMap = new HashMap<Option, String>();
        for (Option option : options)
        {
            @SuppressWarnings({"unchecked"})
            Collection<FieldConfigScheme> fieldConfigSchemes =
                    issueTypeSchemeManager.getAllRelatedSchemes(option.getId());

            StringBuilder cssClasses = new StringBuilder();
            for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
            {
                FieldConfig fieldConfig = fieldConfigScheme.getOneAndOnlyConfig();
                cssClasses.append(fieldConfig.getId()).append(" ");
            }

            optionClassesMap.put(option, cssClasses.toString());
        }

        return optionClassesMap;
    }

    /**
     * @param searchContext The search context.
     * @param user The user performing the search.
     * @return All issue type options visible in the given search context.
     */
    protected Collection<Option> getOptionsInSearchContext(
            SearchContext searchContext, User user)
    {
        Collection<Option> options = new HashSet<Option>();
        Collection<FieldConfig> fieldConfigs = getProjectFieldConfigs(
                getProjectsInSearchContext(searchContext, user));

        for (FieldConfig fieldConfig : fieldConfigs)
        {
            OptionSet optionSet = optionSetManager.getOptionsForConfig(fieldConfig);
            options.addAll(optionSet.getOptions());
        }

        return options;
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User searcher,
            final SearchContext searchContext,
            final FieldLayoutItem fieldLayoutItem,
            final FieldValuesHolder fieldValuesHolder,
            final Map<?, ?> displayParameters,
            final Action action)
    {
        Map<String, Object> velocityParameters = super.getVelocityParams(
                searcher, searchContext, fieldLayoutItem, fieldValuesHolder,
                displayParameters, action);

        velocityParameters.put("isKickass", true);

        return velocityParameters;
    }

    /**
     * @param user The user performing the sarch.
     * @param searchContext The context of the search.
     * @return All issue type options that should be visible to the user.
     */
    private Collection<Option> getVisibleOptions(final User user,
            final SearchContext searchContext)
    {
        I18nHelper i18nHelper = getI18n(user);
        Collection<Option> options = new ArrayList<Option>();
        Collection<Option> optionsInSearchContext = getOptionsInSearchContext(
                searchContext, user);

        // Add the "All Standard Issue Types" option and the standard types.
        options.add(new TextOption(ConstantsManager.ALL_STANDARD_ISSUE_TYPES,
                i18nHelper.getText("common.filters.allstandardissuetypes")));
        options.addAll(CollectionUtils.select(optionsInSearchContext,
                IssueConstantOption.STANDARD_OPTIONS_PREDICATE));

        if (subTaskManager.isSubTasksEnabled())
        {
            Collection<Option> subtaskOptions = CollectionUtils.select(
                    optionsInSearchContext,
                    IssueConstantOption.SUB_TASK_OPTIONS_PREDICATE);

            if (subtaskOptions.size() > 0)
            {
                options.addAll(subtaskOptions);
                options.add(new TextOption(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES,
                        i18nHelper.getText("common.filters.allsubtaskissuetypes")));
            }
        }

        return options;
    }

    private Collection<FieldConfig> getProjectFieldConfigs(
            Collection<Project> projects)
    {
        Collection<FieldConfig> fieldConfigs = new HashSet<FieldConfig>();
        for (Project project : projects)
        {
            fieldConfigs.add(issueTypeSchemeManager.getConfigScheme(project).getOneAndOnlyConfig());
        }

        return fieldConfigs;
    }

    /**
     * @param searchContext The search context.
     * @param user The user performing the search.
     * @return Projects that should be visible to the user.
     */
    private Collection<Project> getProjectsInSearchContext(
            SearchContext searchContext, User user)
    {
        Collection<Project> projects = new ArrayList<Project>();
        for (Project project : permissionManager.getProjectObjects(
                Permissions.BROWSE, user))
        {
            boolean isVisibleProject = searchContext.isForAnyProjects() ||
                    searchContext.getProjectIds().contains(project.getId());

            if (isVisibleProject)
            {
                projects.add(project);
            }
        }

        return projects;
    }

    /**
     * Processes the given options, extracting "Special Options" and splitting
     * the remaining options into the standard and sub-task issue type groups.
     *
     * @param validOptions Valid options.
     * @param invalidOptions Invalid options.
     * @return The result of processing the given options.
     */
    private Map<String, Object> processOptions(Collection<Option> validOptions,
            Collection<Option> invalidOptions)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        Collection<Option> options = new TreeSet<Option>(ISSUE_TYPE_OPTION_COMPARATOR);
        options.addAll(validOptions);
        options.addAll(invalidOptions);

        // We need to actually remove special options from the collection as
        // STANDARD_OPTIONS_PREDICATE matches them (they'd be in two groups).
        result.put("specialOptions", CollectionUtils.select(options,
                SPECIAL_OPTION_PREDICATE));
        options = CollectionUtils.selectRejected(options,
                SPECIAL_OPTION_PREDICATE);

        result.put("standardOptions", CollectionUtils.select(options,
                IssueConstantOption.STANDARD_OPTIONS_PREDICATE));

        result.put("subtaskOptions", CollectionUtils.select(options,
                IssueConstantOption.SUB_TASK_OPTIONS_PREDICATE));

        return result;
    }
}