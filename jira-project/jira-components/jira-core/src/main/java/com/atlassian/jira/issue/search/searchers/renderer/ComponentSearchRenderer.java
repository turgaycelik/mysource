package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentComparator;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.option.GroupTextOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.transformer.ComponentSearchInput;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The new issue navigator's component search renderer.
 *
 * @since v5.2
 */
public class ComponentSearchRenderer extends AbstractProjectConstantsRenderer<ComponentSearchInput, ComponentOptions>
{
    private static final Function<String, GenericProjectConstantsLabel>
            MAKE_COMPONENT_LABEL = new Function<String, GenericProjectConstantsLabel>()
    {
        @Override
        public GenericProjectConstantsLabel get(String input)
        {
            return new GenericProjectConstantsLabel(input);
        }
    };

    private static final Function<String, Option> NAME_TO_OPTION = new Function<String, Option>()
    {
        @Override
        public Option get(String name)
        {
            return new OptionWithValidity(createId(name), name);
        }
    };

    private static final Function<ComponentSearchInput, String> INPUT_OPTION_VALUE = new Function<ComponentSearchInput, String>()
    {
        @Override
        public String get(ComponentSearchInput input)
        {
            if (input.isComponent())
            {
                return createId(input.getValue());
            }
            if (input.isNoComponent())
            {
                return ProjectComponentManager.NO_COMPONENTS;
            }

            throw new IllegalArgumentException("Invalid input");
        }
    };

    private static final Logger log = Logger.getLogger(
            ComponentSearchRenderer.class);

    private final ProjectComponentManager projectComponentManager;

    public ComponentSearchRenderer(
            ApplicationProperties applicationProperties,
            FieldVisibilityManager fieldVisibilityManager,
            ProjectComponentManager projectComponentManager,
            ProjectManager projectManager,
            SimpleFieldSearchConstantsWithEmpty searchConstants,
            String searcherNameKey,
            VelocityTemplatingEngine templatingEngine,
            VelocityRequestContextFactory velocityRequestContextFactory, final PermissionManager permissionManager)
    {
        super(velocityRequestContextFactory, applicationProperties,
                templatingEngine, fieldVisibilityManager, searchConstants,
                projectManager, searcherNameKey, permissionManager);

        this.projectComponentManager = projectComponentManager;
    }

    @Override
    ComponentOptions getValidOptions(User searcher, SearchContext searchContext)
    {
        ComponentOptions componentOptions = new ComponentOptions();

        Collection<String> componentNames = Collections.emptyList();

        if (searchContext.isSingleProjectContext())
        {
            Long projectID = searchContext.getProjectIds().get(0);
            if (isValidProject(projectID, searcher))
            {
                componentNames = Sets.newTreeSet(ProjectComponentComparator.COMPONENT_NAME_COMPARATOR);

                componentNames.addAll(CollectionUtil.transform(projectComponentManager.findAllForProject(projectID),
                    new Function<ProjectComponent, String>()
                    {
                        @Override
                        public String get(ProjectComponent component)
                        {
                            return component.getName();
                        }
                    }));
            }
            else
            {
                log.warn("Project " +  projectID + " for search context " + searchContext +
                        " is invalid.");
            }
        }
        else if (CollectionUtils.isEmpty(searchContext.getProjectIds()))
        {
            Collection<Project> visibleProjects = getVisibleProjects(searcher);
            if (CollectionUtils.isNotEmpty(visibleProjects))
            {
                componentNames = projectComponentManager.findAllUniqueNamesForProjectObjects(visibleProjects);
            }
        }
        else
        {
            List<Long> validProjectIds = Lists.newArrayList();
            for (Long projectID : searchContext.getProjectIds())
            {
                if (isValidProject(projectID, searcher))
                {
                    validProjectIds.add(projectID);
                }
                else
                {
                    log.warn("Project " +  projectID + " for search context " + searchContext +
                            " is invalid.");
                }
            }

            if (!validProjectIds.isEmpty())
            {
                componentNames = projectComponentManager.findAllUniqueNamesForProjects(validProjectIds);
            }
        }

        List<Option> options = CollectionUtil.transform(componentNames, NAME_TO_OPTION);

        componentOptions.options(new GroupTextOption("", "", Lists.<Option>newArrayList(options)));

        return componentOptions;
    }

    @Override
    Function<ComponentSearchInput, String> inputValueToOptionIdFunction()
    {
        return INPUT_OPTION_VALUE;
    }

    @Override
    OptionWithValidity createBlankOption(final User user)
    {
        return new OptionWithValidity(ProjectComponentManager.NO_COMPONENTS,
                getI18n(user).getText("common.concepts.nocomponent"));
    }

    @Override
    void createGroups(List<GroupTextOption> groups, ComponentOptions options, Set<Option> invalidOptions)
    {
        addOption(groups, "", Lists.newArrayList(options.getNo()));
        addOption(groups, options.getOptions());
        addOption(groups, "invalid", invalidOptions);
    }

    @Override
    String nameFromSelectedValue(User searcher, ComponentSearchInput searchInput)
    {
        if (searchInput.isNoComponent())
        {
            return getI18n(searcher).getText("common.concepts.nocomponent");
        }

        return searchInput.getValue();
    }

    @Override
    Collection<GenericProjectConstantsLabel> getSelectedLabels(User searcher, Collection<ComponentSearchInput> selectedValues)
    {
        List<String> componentNames = Lists.newArrayList();
        if (selectedValues != null)
        {
            for (ComponentSearchInput selectedValue : selectedValues)
            {
                componentNames.add(nameFromSelectedValue(searcher, selectedValue));
            }
        }

        return getSelectedObjects(componentNames, MAKE_COMPONENT_LABEL);
    }
}
