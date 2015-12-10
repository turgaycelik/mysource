package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.option.GroupTextOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * The base renderer for Version searcher renderers
 *
 * @since v5.2
 */
public abstract class AbstractVersionRenderer extends AbstractProjectConstantsRenderer<VersionSearchInput, VersionsOptions> implements SearchRenderer
{
    private static final Logger log = Logger.getLogger(AbstractVersionRenderer.class);

    private static final Function<Version, OptionWithValidity> VERSION_TO_OPTION = new Function<Version, OptionWithValidity>()
    {
        @Override
        public OptionWithValidity get(Version version)
        {
            return new OptionWithValidity(createId(version.getName()), version.getName());
        }
    };

    private static final Function<VersionSearchInput, String> INPUT_OPTION_VALUE = new Function<VersionSearchInput, String>()
    {
        @Override
        public String get(VersionSearchInput input)
        {
            if (input.isVersion())
            {
                return createId(input.getValue());
            }
            if (input.isNoVersion())
            {
                return VersionManager.NO_VERSIONS;
            }
            if (input.isAllUnreleased())
            {
                return VersionManager.ALL_UNRELEASED_VERSIONS;
            }
            if (input.isAllReleased())
            {
                return VersionManager.ALL_RELEASED_VERSIONS;
            }

            throw new IllegalArgumentException("Invalid input");
        }
    };

    private final VersionManager versionManager;
    private final boolean unreleasedOptionsFirst;
    private final FieldVisibilityManager fieldVisibilityManager;

    protected AbstractVersionRenderer(SimpleFieldSearchConstantsWithEmpty constants, String searcherNameKey, ProjectManager projectManager, VersionManager versionManager,
            VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, final FieldVisibilityManager fieldVisibilityManager,
            PermissionManager permissionManager, boolean unreleasedOptionsFirst)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, constants, projectManager, searcherNameKey, permissionManager);

        this.fieldVisibilityManager = fieldVisibilityManager;
        this.versionManager = versionManager;
        this.unreleasedOptionsFirst = unreleasedOptionsFirst;
    }

    @Override
    public boolean isShown(User user, SearchContext searchContext)
    {
        final boolean hasOptions = hasAnyValidOption(user, searchContext);
        return hasOptions && !fieldVisibilityManager.isFieldHiddenInAllSchemes(searchConstants.getFieldId(), searchContext, user);
    }


    protected boolean hasAnyValidOption(final User searcher, SearchContext searchContext)
    {
        List<Long> projectIds = searchContext.getProjectIds();

        if (CollectionUtils.isEmpty(projectIds))
        {
            Collection<Project> visibleProjects = getVisibleProjects(searcher);
            for (Project visibleProject : visibleProjects)
            {
                if (!(versionManager.getVersions(visibleProject.getId(), false)).isEmpty())
                {
                    return true;
                }
            }
        }
        else
        {
            for (Long projectId : projectIds)
            {
                if (isValidProject(projectId, searcher))
                {
                    if (!(versionManager.getVersions(projectId, false)).isEmpty())
                    {
                        return true;
                    }
                }
                else
                {
                    log.warn("Project for search context " + searchContext + " is invalid");
                }
            }
        }

        return false;
    }

    @Override
    VersionsOptions getValidOptions(final User searcher, SearchContext searchContext)
    {
        VersionsOptions options = new VersionsOptions();

        List<Long> projectIds = searchContext.getProjectIds();

        if (CollectionUtils.isEmpty(projectIds))
        {
            SortedSet<OptionWithValidity> allVersionOptions = Sets.newTreeSet(new OptionComparator());
            Collection<Project> visibleProjects = getVisibleProjects(searcher);

            if (CollectionUtils.isNotEmpty(visibleProjects))
            {
                allVersionOptions.addAll(CollectionUtil.transform(versionManager.getAllVersionsForProjects(visibleProjects, false), VERSION_TO_OPTION));
            }

            if (!allVersionOptions.isEmpty())
            {
                options.released(new GroupTextOption("", "", Lists.<Option>newArrayList(allVersionOptions)));
            }
        }
        else if (projectIds.size() == 1)
        {
            Long projectId = projectIds.iterator().next();
            if (isValidProject(projectId, searcher))
            {
                Collection<Version> unreleasedVersions = versionManager.getVersionsUnreleased(projectId, false);
                if (!unreleasedVersions.isEmpty())
                {
                    SortedSet<OptionWithValidity> unreleasedVersionOptions = Sets.newTreeSet(new OptionComparator());
                    unreleasedVersionOptions.addAll(CollectionUtil.transform(unreleasedVersions, VERSION_TO_OPTION));

                    options.unreleased(new GroupTextOption(VersionManager.ALL_UNRELEASED_VERSIONS, getI18n(searcher).getText("common.filters.unreleasedversions"),
                            Lists.<Option>newArrayList(unreleasedVersionOptions)));

                    options.allUnreleased(new OptionWithValidity(VersionManager.ALL_UNRELEASED_VERSIONS, getI18n(searcher).getText("common.filters.unreleasedversions")));
                }

                Collection<Version> releasedVersions = versionManager.getVersionsReleasedDesc(projectId, false);
                if (!releasedVersions.isEmpty())
                {
                    SortedSet<OptionWithValidity> releasedVersionOptions = Sets.newTreeSet(new OptionComparator());
                    releasedVersionOptions.addAll(CollectionUtil.transform(releasedVersions, VERSION_TO_OPTION));

                    options.released(new GroupTextOption(VersionManager.ALL_RELEASED_VERSIONS, getI18n(searcher).getText("common.filters.releasedversions"),
                            Lists.<Option>newArrayList(releasedVersionOptions)));

                    options.allReleased(new OptionWithValidity(VersionManager.ALL_RELEASED_VERSIONS, getI18n(searcher).getText("common.filters.releasedversions")));
                }
            }
            else
            {
                log.warn("Project for search context " + searchContext + " is invalid");
            }
        }
        else
        {
            SortedSet<OptionWithValidity> allVersionOptions = Sets.newTreeSet(new OptionComparator());

            for (Long projectId : projectIds)
            {
                if (isValidProject(projectId, searcher))
                {
                    allVersionOptions.addAll(CollectionUtil.transform(versionManager.getVersions(projectId, false), VERSION_TO_OPTION));
                }
                else
                {
                    log.warn("Project for search context " + searchContext + " is invalid");
                }
            }

            if (!allVersionOptions.isEmpty())
            {
                options.released(new GroupTextOption("", "", Lists.<Option>newArrayList(allVersionOptions)));
            }
        }

        return options;
    }

    private static class OptionComparator implements Comparator<Option>
    {
        @Override
        public int compare(Option option1, Option option2)
        {
            return option1.getName().compareToIgnoreCase(option2.getName());
        }
    }

    @Override
    Function<VersionSearchInput, String> inputValueToOptionIdFunction()
    {
        return INPUT_OPTION_VALUE;
    }

    @Override
    OptionWithValidity createBlankOption(User searcher)
    {
        return new OptionWithValidity(VersionManager.NO_VERSIONS, getI18n(searcher).getText("common.filters.noversion"));
    }

    @Override
    void createGroups(List<GroupTextOption> groups, VersionsOptions options, Set<Option> invalidOptions)
    {
        if (unreleasedOptionsFirst)
        {
            addOption(groups, "", options.getNo(), options.getAllUnreleased(), options.getAllReleased());
            addOption(groups, "invalid", invalidOptions);
            addOption(groups, options.getUnreleased());
            addOption(groups, options.getReleased());
        }
        else
        {
            addOption(groups, "", options.getNo(), options.getAllReleased(), options.getAllUnreleased());
            addOption(groups, "invalid", invalidOptions);
            addOption(groups, options.getReleased());
            addOption(groups, options.getUnreleased());
        }
    }

    @Override
    String nameFromSelectedValue(User searcher, VersionSearchInput searchInput)
    {
        if (searchInput.isNoVersion())
        {
            return getI18n(searcher).getText("common.filters.noversion");
        }
        else if (searchInput.isAllUnreleased())
        {
            return getI18n(searcher).getText("common.filters.unreleasedversions");
        }
        else if (searchInput.isAllReleased())
        {
            return getI18n(searcher).getText("common.filters.releasedversions");
        }

        return searchInput.getValue();
    }

    @Override
    Collection<GenericProjectConstantsLabel> getSelectedLabels(User searcher, Collection<VersionSearchInput> selectedValues)
    {
        return getSelectedObjects(selectedValues, new VersionLabelFunction(searcher));
    }

    class VersionLabelFunction implements Function<VersionSearchInput, GenericProjectConstantsLabel>
    {
        private final User searcher;

        /**
         * Constructor specifying whether the version label should link to the browse fix for version page
         *
         * @param searcher The User doing the search
         * @since v3.10.2
         */
        VersionLabelFunction(final User searcher)
        {
            this.searcher = searcher;
        }

        public GenericProjectConstantsLabel get(VersionSearchInput input)
        {
            if (input.isNoVersion())
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("common.filters.noversion"));
            }
            else if (input.isAllUnreleased())
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("common.filters.unreleasedversions"));
            }
            else if (input.isAllReleased())
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("common.filters.releasedversions"));
            }

            return new GenericProjectConstantsLabel(input.getValue());
        }
    }
}
