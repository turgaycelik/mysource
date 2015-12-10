package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.admin.issuetypes.ExecutableAction;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of MultiBulkMoveBean.
 *
 * @since v4.3
 */
public class MultiBulkMoveBeanImpl implements MultiBulkMoveBean
{
    private static final Logger log = Logger.getLogger(MultiBulkMoveBeanImpl.class);
    private String operationName;

    private ListOrderedMap regularIssues;
    private List<Issue> subTaskIssues;
    private ListOrderedMap issuesInContext;
    private ListOrderedMap bulkEditBeans;

    // Post function stuff

    private Collection optionIds;
    private List<IssueConstantOption> regularOptions;
    private List<IssueConstantOption> subTaskOptions;

    private ExecutableAction executableAction;
    private String finalLocation;

    private int currentBulkEditBeanIndex = 0;

    private int subTasksDiscarded = 0;

    private final ConstantsManager constantsManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueManager issueManager;

    public MultiBulkMoveBeanImpl(String operationName, IssueManager issueManager)
    {
        this.operationName = operationName;
        this.issueManager = issueManager;
        this.constantsManager = ComponentAccessor.getConstantsManager();
        this.authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }

    public void initOptionIds(Collection optionIds)
    {
        // Set up the options list
        this.optionIds = optionIds;
        regularOptions = new ArrayList<IssueConstantOption>();
        subTaskOptions = new ArrayList<IssueConstantOption>();
        for (final Object optionId1 : optionIds)
        {
            String optionId = (String) optionId1;
            final IssueType issueType = constantsManager.getIssueTypeObject(optionId);
            if (!issueType.isSubTask())
            {
                regularOptions.add(new IssueConstantOption(issueType));
            }
            else
            {
                subTaskOptions.add(new IssueConstantOption(issueType));
            }
        }
    }

    /**
     * Initialises this MultiBulkMoveBean given a list of issues.
     * <p>
     * If this MultiBulkMoveBean links a BulkEditBean with parent issues to BulkEditBeans with subtasks, then include
     * the parent BulkEditBean in the parentBulkEditBean parameter. Otherwise you can pass null.
     * </p>
     *
     * @param issues             Issues for this MultiBulkMoveBean.
     * @param parentBulkEditBean If this MultiBulkMoveBean represents subtasks, then this is the BulkEditBean that
     *                           contains the parents of the subtasks, otherwise null.
     */
    public void initFromIssues(List issues, BulkEditBean parentBulkEditBean)
    {
        // Ensure that the order is kept
        issuesInContext = (ListOrderedMap) ListOrderedMap.decorate(new MultiHashMap());
        regularIssues = new ListOrderedMap();
        subTaskIssues = new ArrayList<Issue>();

        // First pass stores att the
        for (final Object issue2 : issues)
        {
            MutableIssue issue = (MutableIssue) issue2;
            if (!issue.isSubTask())
            {
                regularIssues.put(issue.getId(), issue);
            }
            else
            {
                subTaskIssues.add(issue);
            }
        }

        // Split it up by context, also check special rule that you can't move sub tasks & its parent all in the same go
        for (final Object issue1 : issues)
        {
            MutableIssue issue = (MutableIssue) issue1;
            // NOTE: we only do this for the bulk move operation, this is likely the correct behavior for the
            // bulk move operation but I am certain that it is not correct for the bulk migrate operation JRA-10244.
            // In bulk move the wizard will prompt the user with subtask information once it has collected the project
            // information about the parent issues, this is not need in the the issue type scheme migration since you
            // will never be changing the project
            // TODO: Why test for operation name?
            if (BulkMigrateOperation.OPERATION_NAME.equals(operationName) && issue.isSubTask() &&
                    regularIssues.containsKey(issue.getParentId()))
            {
                log.info(
                        "Sub issue: " + issue.getKey() + " : discarded since parent was also present in the bulk move");
                subTasksDiscarded++;
            }
            else
            {
                issuesInContext.put(new IssueContextImpl(issue.getProjectObject(), issue.getIssueTypeObject()), issue);
            }
        }

        // Set the bulk edit bean.. sort the keys by project
        bulkEditBeans = new ListOrderedMap();
        List keys = new ArrayList(issuesInContext.keySet());
        Collections.sort(keys);

        for (final Object key : keys)
        {
            IssueContext context = (IssueContext) key;
            Collection issuesForContext = (Collection) issuesInContext.get(context);

            BulkEditBean bulkEditBean = new BulkEditBeanImpl(issueManager);
            bulkEditBean.initSelectedIssues(issuesForContext);
            bulkEditBean.setOperationName(operationName);
            bulkEditBean.setTargetProject(context.getProjectObject());
            bulkEditBean.setTargetIssueTypeId(
                    context.getIssueTypeObject() != null ? context.getIssueTypeObject().getId() : null);
            // Set the Parent BulkEditBean - used by subtask BulkEditBean's to get to the new version of the subtask's parents.
            bulkEditBean.setParentBulkEditBean(parentBulkEditBean);

            bulkEditBeans.put(context, bulkEditBean);
        }
    }

    /**
     * This method will remap the current {@link BulkEditBean} Map to be keyed by the <em>target</em>
     * {@link IssueContext} rather than the originating {@link IssueContext}.
     */
    public void remapBulkEditBeansByTargetContext()
    {
        Map bulkEditBeans = getBulkEditBeans();
        ListOrderedMap targetKeyedBulkEditBeans = new ListOrderedMap();
        Set entries = bulkEditBeans.entrySet();
        for (final Object entry1 : entries)
        {
            Map.Entry entry = (Map.Entry) entry1;
            BulkEditBean bulkEditBean = (BulkEditBean) entry.getValue();

            // Build Target Issue contexts
            IssueContext targetIssueContext =
                    new IssueContextImpl(bulkEditBean.getTargetProject(), bulkEditBean.getTargetIssueTypeObject());

            if (targetKeyedBulkEditBeans.containsKey(targetIssueContext))
            {
                // Add to to the bulk edit bean
                BulkEditBean finalBulkEditBean = (BulkEditBean) targetKeyedBulkEditBeans.get(targetIssueContext);
                // We add the top-level issues now. Affected subtasks will be calculated later
                // by calling BulkMoveOperation().finishChooseContext()
                finalBulkEditBean.addIssues(bulkEditBean.getSelectedIssues());
            }
            else
            {
                targetKeyedBulkEditBeans.put(targetIssueContext, bulkEditBean);
            }
        }

        // Set the BulkEditBean Map to our new map (keyed by Target Context)
        setBulkEditBeans(targetKeyedBulkEditBeans);
    }

    @Override
    @Deprecated
    public void validate(final ErrorCollection errors, final BulkMoveOperation bulkMoveOperation, final User user)
    {
        validate(errors, bulkMoveOperation, ApplicationUsers.from(user));
    }

    @Override
    public void validate(final ErrorCollection errors, final BulkMoveOperation bulkMoveOperation,
                         final ApplicationUser applicationUser)
    {
        if (!regularIssues.isEmpty() && regularOptions.isEmpty())
        {
            errors.addErrorMessage(authenticationContext.getI18nHelper()
                    .getText("admin.errors.bean.issues.affected", "" + regularIssues.size()));
        }

        if (!subTaskIssues.isEmpty() && subTaskOptions.isEmpty())
        {
            errors.addErrorMessage(authenticationContext.getI18nHelper()
                    .getText("admin.errors.bean.subtasks.affected", "" + subTaskIssues.size()));
        }

        // Validate permission
        Set entries = bulkEditBeans.entrySet();
        for (final Object entry1 : entries)
        {
            Map.Entry entry = (Map.Entry) entry1;
            IssueContext issueContext = (IssueContext) entry.getKey();
            BulkEditBean bulkEditBean = (BulkEditBean) entry.getValue();
            if (!bulkMoveOperation.canPerform(bulkEditBean, applicationUser))
            {
                errors.addErrorMessage(authenticationContext.getI18nHelper()
                        .getText("admin.errors.bean.no.permission", issueContext.getProject().getString("name"),
                                issueContext.getIssueTypeObject().getName()));
            }
        }
    }

    public ListOrderedMap getIssuesInContext()
    {
        return issuesInContext;
    }

    public ListOrderedMap getBulkEditBeans()
    {
        return bulkEditBeans;
    }

    private void setBulkEditBeans(ListOrderedMap bulkEditBeans)
    {
        this.bulkEditBeans = bulkEditBeans;
    }

    public ExecutableAction getExecutableAction()
    {
        return executableAction;
    }

    public void setExecutableAction(ExecutableAction executableAction)
    {
        this.executableAction = executableAction;
    }

    public String getFinalLocation()
    {
        return finalLocation;
    }

    public void setFinalLocation(String finalLocation)
    {
        this.finalLocation = finalLocation;
    }

    public Collection getSelectedOptions()
    {
        return optionIds;
    }

    public List getRegularOptions()
    {
        return regularOptions;
    }

    public List getSubTaskOptions()
    {
        return subTaskOptions;
    }

    public int getSubTasksDiscarded()
    {
        return subTasksDiscarded;
    }

    public int getNumberOfStatusChangeRequired(BulkMoveOperation bulkMoveOperation)
    {
        int i = 0;
        for (final Object o : bulkEditBeans.values())
        {
            BulkEditBean bulkEditBean = (BulkEditBean) o;
            if (bulkEditBean.getTargetPid() != null && !bulkMoveOperation.isStatusValid(bulkEditBean))
            {
                i++;
            }
        }

        return i;
    }

    public BulkEditBean getCurrentBulkEditBean()
    {
        if (!getBulkEditBeans().isEmpty())
        {
            return (BulkEditBean) getBulkEditBeans().getValue(currentBulkEditBeanIndex);
        }
        else
        {
            return null;
        }
    }

    public void progressToNextBulkEditBean()
    {
        if (!isLastBulkEditBean())
        {
            currentBulkEditBeanIndex++;
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unable to progress to bulk edit bean with index greater than " + (currentBulkEditBeanIndex) +
                            ". " + getBulkEditBeans().size() + " bulk edit beans available");
        }
    }

    public void progressToPreviousBulkEditBean()
    {
        if (currentBulkEditBeanIndex > 0)
        {
            currentBulkEditBeanIndex--;
        }
        else
        {
            throw new IllegalArgumentException("Unable to progress to bulk edit bean with index less than 0");
        }
    }

    public boolean isLastBulkEditBean()
    {
        return currentBulkEditBeanIndex == getBulkEditBeans().size() - 1;
    }

    public IssueContext getCurrentIssueContext()
    {
        if (!getBulkEditBeans().isEmpty())
        {
            return (IssueContext) getBulkEditBeans().get(currentBulkEditBeanIndex);
        }
        else
        {
            return null;
        }
    }

    public int getCurrentBulkEditBeanIndex()
    {
        return currentBulkEditBeanIndex;
    }

    @Override
    public void setTargetProject(GenericValue targetProjectGV)
    {
        for (final Object o : getBulkEditBeans().values())
        {
            BulkEditBean bulkEditBean = (BulkEditBean) o;
            bulkEditBean.setTargetProject(targetProjectGV);
        }
    }

    @Override
    public void setTargetProject(Project targetProject)
    {
        for (final Object o : getBulkEditBeans().values())
        {
            BulkEditBean bulkEditBean = (BulkEditBean) o;
            bulkEditBean.setTargetProject(targetProject);
        }
    }
}
