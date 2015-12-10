package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.option.GroupTextOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInput;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.util.CollectionUtils;
import webwork.action.Action;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * An abstract renderer for the project constants (versions and components).
 *
 * @since v4.0
 */
public abstract class AbstractProjectConstantsRenderer<I extends SearchInput, O extends Options> extends AbstractSearchRenderer implements SearchRenderer
{
    private static final String OPTION_ID_PREFIX = "id:";

    protected final ProjectManager projectManager;
    protected final SimpleFieldSearchConstantsWithEmpty searchConstants;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final PermissionManager permissionManager;

    AbstractProjectConstantsRenderer(VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, FieldVisibilityManager fieldVisibilityManager,
            SimpleFieldSearchConstantsWithEmpty searchConstants, ProjectManager projectManager, String searcherNameKey, PermissionManager permissionManager)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, searchConstants.getSearcherId(), searcherNameKey);

        this.searchConstants = searchConstants;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    static String createId(String value)
    {
        return OPTION_ID_PREFIX + escapeHtml(value);
    }

    public boolean isRelevantForQuery(final User user, Query query)
    {
        return isRelevantForQuery(searchConstants.getJqlClauseNames(), query);
    }

    public boolean isShown(final User user, SearchContext searchContext)
    {
        return !getSelectListOptions(user, searchContext).isEmpty() &&
               !fieldVisibilityManager.isFieldHiddenInAllSchemes(searchConstants.getFieldId(), searchContext, user);
    }

    List<Option> getSelectListOptions(User searcher, SearchContext searchContext)
    {
        return getValidOptions(searcher, searchContext).all();
    }

    abstract O getValidOptions(User searcher, SearchContext searchContext);


    @Override
    public String getEditHtml(User user,
            SearchContext searchContext,
            FieldValuesHolder fieldValuesHolder,
            Map<?, ?> displayParameters,
            Action action)
    {
        Map<String, Object> velocityParameters = getVelocityParams(user, searchContext, null, fieldValuesHolder,
                displayParameters, action);

        addEditParameters(user, searchContext, fieldValuesHolder, velocityParameters);

        return renderEditTemplate("project-constants-searcher-edit.vm", velocityParameters);
    }

    /**
     * Add edit template paramters to the given velocity parameters map.
     */
    void addEditParameters(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<String, Object> velocityParameters)
    {
        @SuppressWarnings("unchecked")
        Collection<I> selectedValues = (Collection<I>) fieldValuesHolder.get(searchConstants.getUrlParameter());
        velocityParameters.put("optionGroups", getOptions(searcher, selectedValues, searchContext));

        Collection<String> selectedOptionKeys = CollectionUtil.transform(selectedValues, inputValueToOptionIdFunction());
        velocityParameters.put("selectedValues", new SelectedValues(selectedOptionKeys));
    }

    private List<GroupTextOption> getOptions(User searcher, Collection<I> selectedValues, SearchContext searchContext)
    {
        O validOptions = getValidOptions(searcher, searchContext);

        validOptions.no(createBlankOption(searcher));

        Set<Option> invalidOptions = getInvalidOptions(searcher, selectedValues, validOptions.all());

        List<GroupTextOption> groups = Lists.newArrayList();

        createGroups(groups, validOptions, invalidOptions);

        return groups;
    }

    abstract OptionWithValidity createBlankOption(User searcher);

    abstract void createGroups(List<GroupTextOption> groups, O validOptions, Set<Option> invalidOptions);


    /**
     * Returns a list of invalid {@link com.atlassian.jira.issue.fields.option.TextOption}s.
     *
     * Validity is determined by the lack of presence of the selectedKey in a list of valid options and whether
     * the key has is not already flagged as being invalid.
     *
     * @param user The current User
     * @param selectedValues A collection of selected component values
     * @param allOptions A collection of valid keys
     *
     * @return A list of invalid options.
     */
    private Set<Option> getInvalidOptions(User user, Collection<I> selectedValues, Collection<Option> allOptions)
    {
        final SortedSet<Option> invalidOptions = new TreeSet<Option>();
        if (selectedValues != null)
        {
            for (I selectedValue: selectedValues)
            {
                if (selectedValue != null)
                {
                    String selectedOptionId = inputValueToOptionIdFunction().get(selectedValue);

                    if (!isValidKey(selectedOptionId, allOptions))
                    {
                        String name = nameFromSelectedValue(user, selectedValue);
                        if (name != null)
                        {
                            name = StringEscapeUtils.unescapeHtml(name);

                            if (!isValidName(name, allOptions)
                                    && !containsName(name, invalidOptions))
                            {
                                invalidOptions.add(new OptionWithValidity(selectedOptionId, name, false));
                            }
                        }
                    }
                }

            }
        }

        return invalidOptions;
    }

    abstract String nameFromSelectedValue(User user, I selectedValue);

    abstract Function<I, String> inputValueToOptionIdFunction();


    private boolean isValidKey(String key, Collection<Option> validOptions)
    {
        for (Option option : validOptions)
        {
            if (option.getId().equals(key))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isValidName(String name, Collection<Option> validOptions)
    {
        for (Option option : validOptions)
        {
            if (option.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    private boolean containsName(String name, Set<Option> options)
    {
        for (Option option : options)
        {
            if (name.equalsIgnoreCase(option.getName()))
            {
                return true;
            }
        }
        return false;
    }


    @Override
    public String getViewHtml(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder,
            Map<?, ?> displayParameters, Action action)
    {
        Map<String, Object> velocityParameters = getVelocityParams(user, searchContext, null, fieldValuesHolder,
                displayParameters, action);

        addViewParameters(user, searchContext, fieldValuesHolder, velocityParameters);

        return renderViewTemplate("project-constants-searcher-view.vm", velocityParameters);
    }

    void addViewParameters(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<String, Object> velocityParameters)
    {
        @SuppressWarnings("unchecked")
        Collection<I> selectedValues = (Collection<I>) fieldValuesHolder.get(searchConstants.getUrlParameter());

        Collection<GenericProjectConstantsLabel> selectedObjects = getSelectedLabels(searcher, selectedValues);

        O validOptions = getValidOptions(searcher, searchContext);
        validOptions.no(createBlankOption(searcher));

        velocityParameters.put("selectedObjects", filterDuplicateSelectedObjects(selectedObjects, validOptions.all()));
    }

    abstract Collection<GenericProjectConstantsLabel> getSelectedLabels(User searcher, Collection<I> selectedValues);


    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getVelocityParams(final User user, final SearchContext searchContext,
            final FieldLayoutItem fieldLayoutItem, final FieldValuesHolder fieldValuesHolder,
            final Map<?, ?> displayParameters, final Action action)
    {
        ((Map<String, Object>)displayParameters).put("kickass", true);

        return super.getVelocityParams(user, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
    }


    void addOption(List<GroupTextOption> options, String groupId, Option ... addOptions)
    {
        addOption(options, groupId, Arrays.asList(addOptions));
    }

    void addOption(List<GroupTextOption> options, String groupId, Collection<Option> addOptions)
    {
        List<Option> nonNullOptions = Lists.newArrayList();
        for (Option option : addOptions)
        {
            if (null != option)
            {
                nonNullOptions.add(option);
            }
        }
        if (!nonNullOptions.isEmpty())
        {
            options.add(new GroupTextOption(groupId, "", nonNullOptions));
        }
    }

    void addOption(List<GroupTextOption> options, GroupTextOption option)
    {
        if (null != option)
        {
            options.add(option);
        }
    }


    <T> Collection<GenericProjectConstantsLabel> getSelectedObjects(Collection<T> selectedValues, Function<T, GenericProjectConstantsLabel> function)
    {
        if (selectedValues != null && !selectedValues.isEmpty())
        {
            return CollectionUtil.transform(selectedValues, function);
        }

        return null;
    }

    Collection<GenericProjectConstantsLabel> filterDuplicateSelectedObjects(Collection<GenericProjectConstantsLabel> selectedObjects, final Collection<Option> validOptions)
    {
        if (CollectionUtils.isEmpty(selectedObjects))
        {
            return Collections.emptyList();
        }

        return Collections2.transform(ImmutableSortedSet.copyOf(selectedObjects), new com.google.common.base.Function<GenericProjectConstantsLabel, GenericProjectConstantsLabel>()
        {
            @Override
            public GenericProjectConstantsLabel apply(GenericProjectConstantsLabel label)
            {
                boolean valid = false;
                String name = label.getLabel();
                for (Option option : validOptions)
                {
                    if (label.isSameAs(option))
                    {
                        name = option.getName();
                        valid = true;
                        break;
                    }
                }
                return new GenericProjectConstantsLabel(name, valid);
            }
        });
    }

    boolean isValidProject(Long projectId, User searcher)
    {
        Project project = projectManager.getProjectObj(projectId);

        return project != null && permissionManager.hasPermission(Permissions.BROWSE, project, searcher);
    }

    Collection<Project> getVisibleProjects(User searcher)
    {
        return permissionManager.getProjectObjects(Permissions.BROWSE, searcher);
    }

    /**
     * A label with an optional url to the browse page
     *
     * @since v4.0
     */
    public static class GenericProjectConstantsLabel implements Comparable<GenericProjectConstantsLabel>
    {
        private final String browseUrl;
        private final String label;
        private final boolean valid;

        /**
         *
         * @param label the label
         */
        public GenericProjectConstantsLabel(String label)
        {
            this(label, null, true);
        }

        /**
         *
         * @param label the label
         * @param browseUrl the url linking the label to the browse page
         */
        public GenericProjectConstantsLabel(String label, String browseUrl)
        {
            this(label, browseUrl, true);
        }

        /**
         *
         * @param label the label
         * @param valid whether label is valid for context or not
         */
        public GenericProjectConstantsLabel(String label, boolean valid)
        {
            this(label, null, valid);
        }

        /**
         *
         * @param label the label
         * @param browseUrl the url linking the label to the browse page
         * @param valid whether label is valid for context or not
         */
        public GenericProjectConstantsLabel(String label, String browseUrl, boolean valid)
        {
            this.browseUrl = browseUrl;
            this.label = label;
            this.valid = valid;
        }

        public String getLabel()
        {
            return label;
        }

        public String getBrowseUrl()
        {
            return browseUrl;
        }

        public boolean isValid()
        {
            return valid;
        }

        public boolean isSameAs(Option option)
        {
            String name = option.getName();
            return null == this.label ? null == name : this.label.equalsIgnoreCase(name);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            GenericProjectConstantsLabel that = (GenericProjectConstantsLabel) o;
            return !(label != null ? !label.equalsIgnoreCase(that.label) : that.label != null);
        }

        @Override
        public int hashCode()
        {
            return label != null ? label.toLowerCase().hashCode() : 0;
        }

        @Override
        public int compareTo(GenericProjectConstantsLabel genericProjectConstantsLabel)
        {
            return String.CASE_INSENSITIVE_ORDER.compare(getLabel(), genericProjectConstantsLabel.getLabel());
        }
    }
}
