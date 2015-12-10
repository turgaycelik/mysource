package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@WebSudoRequired
public class ConfigureIssueTypeOptionScheme extends ConfigureOptionScheme
{
    private final ComponentFactory componentFactory;
    private List<Project> associatedProjects;

    public ConfigureIssueTypeOptionScheme(final FieldConfigSchemeManager configSchemeManager,
            final IssueTypeSchemeManager issueTypeSchemeManager, final FieldManager fieldManager,
            final OptionSetManager optionSetManager, final IssueTypeManageableOption manageableOptionType,
            final BulkMoveOperation bulkMoveOperation, final SearchProvider searchProvider,
            final ConstantsManager constantsManager, final IssueManager issueManager, final ComponentFactory factory,
            final EventPublisher eventPublisher)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType, bulkMoveOperation, searchProvider,
            constantsManager, issueManager, eventPublisher);
        componentFactory = factory;
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();

        if (CollectionUtils.exists(issueTypeSchemeManager.getAllSchemes(), new FieldConfigPredicate(getSchemeId(), getName())))
        {
            addError("name", getText("admin.errors.issuetypes.duplicate.name"));
        }

        if ((getSelectedOptions() != null) && (getSelectedOptions().length > 0))
        {
            boolean hasNormalIssueType = false;
            for (int i = 0; i < getSelectedOptions().length; i++)
            {
                final String id = getSelectedOptions()[i];
                final IssueType issueType = constantsManager.getIssueTypeObject(id);
                if (!issueType.isSubTask())
                {
                    hasNormalIssueType = true;
                    break;
                }
            }

            if (!hasNormalIssueType)
            {
                addErrorMessage(getText("admin.errors.issuetypes.must.select.standard.issue.type"));
            }
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Find all possibly affected issues.
        final List<GenericValue> associatedProjects = getConfigScheme().getAssociatedProjects();
        if ((associatedProjects != null) && !associatedProjects.isEmpty())
        {
            final List<Long> projectIds = GenericValueUtils.transformToLongIdsList(associatedProjects);
            final Collection<Option> obseleteOptions = CollectionUtils.subtract(getOriginalOptions(), getNewOptions());
            if ((obseleteOptions != null) && !obseleteOptions.isEmpty())
            {
                final List<String> obseleteOptionIds = new ArrayList<String>(obseleteOptions.size());
                for (final Option option : obseleteOptions)
                {
                    obseleteOptionIds.add(option.getId());
                }

                final Query query = getQuery(projectIds, obseleteOptionIds);
                final SearchResults searchResults = searchProvider.search(query, getLoggedInUser(), PagerFilter.getUnlimitedFilter());
                final List affectedIssues = searchResults.getIssues();
                if ((affectedIssues != null) && !affectedIssues.isEmpty())
                {
                    // Prepare for Update
                    configScheme = new FieldConfigScheme.Builder(getConfigScheme()).setName(getName()).setDescription(getDescription()).toFieldConfigScheme();
                    final List<String> optionIds = new ArrayList<String>(Arrays.asList(getSelectedOptions()));

                    return migrateIssues(this, affectedIssues, optionIds);
                }
            }
        }

        return super.doExecute();
    }

    public List<Project> getUsedIn()
    {
        if (associatedProjects == null)
        {
            ProjectIssueTypeSchemeHelper helper = componentFactory.createObject(ProjectIssueTypeSchemeHelper.class);
            associatedProjects = helper.getProjectsUsingScheme(getConfigScheme());
        }
        return associatedProjects;
    }

    /**
     * Whether or not you're allowed to add or remove an option to the current list
     * @return boolean for
     */
    @Override
    public boolean isAllowEditOptions()
    {
        return !issueTypeSchemeManager.getDefaultIssueTypeScheme().getId().equals(getSchemeId());
    }
}
