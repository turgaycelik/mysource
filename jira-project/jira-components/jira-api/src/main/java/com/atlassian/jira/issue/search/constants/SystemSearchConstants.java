package com.atlassian.jira.issue.search.constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.project.version.VersionManager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import net.jcip.annotations.ThreadSafe;

/**
 * Contains the constants used by systems fields for searching. It is designed to provide a safe link between all of
 * those string constants in JIRA.
 *
 * @since v4.0
 */
@ThreadSafe
public final class SystemSearchConstants
{
    /**
     * The ID of the query searcher.
     */
    public static final String QUERY_SEARCHER_ID = "text";

    public static final String FIX_FOR_VERSION = "fixversion";
    public static final String FIX_FOR_VERSION_CHANGEITEM = "Fix Version";
    public static final String ISSUE_PROPERTY = "issue.property";

    //We don't want to create an instance of this class.
    private SystemSearchConstants()
    {}

    private static final SimpleFieldSearchConstants PRIORITY = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_PRIORITY,
        IssueFieldConstants.PRIORITY, IssueFieldConstants.PRIORITY, IssueFieldConstants.PRIORITY, IssueFieldConstants.PRIORITY,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.PRIORITY);

    public static SimpleFieldSearchConstants forPriority()
    {
        return PRIORITY;
    }

    private static final SimpleFieldSearchConstants PROJECT = new SimpleFieldSearchConstants(DocumentConstants.PROJECT_ID,
        IssueFieldConstants.PROJECT, "pid", IssueFieldConstants.PROJECT, IssueFieldConstants.PROJECT, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
        JiraDataTypes.PROJECT);

    public static SimpleFieldSearchConstants forProject()
    {
        return PROJECT;
    }

    private static final SimpleFieldSearchConstants ISSUE_TYPE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_TYPE, new ClauseNames(
        IssueFieldConstants.ISSUE_TYPE, "type"), "type", IssueFieldConstants.ISSUE_TYPE, IssueFieldConstants.ISSUE_TYPE,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.ISSUE_TYPE);

    public static SimpleFieldSearchConstants forIssueType()
    {
        return ISSUE_TYPE;
    }

    private static final SimpleFieldSearchConstantsWithEmpty COMPONENT = new SimpleFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_COMPONENT,
        new ClauseNames("component"), "component", DocumentConstants.ISSUE_COMPONENT, ProjectComponentManager.NO_COMPONENTS, ProjectComponentManager.NO_COMPONENTS,
        IssueFieldConstants.COMPONENTS, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.COMPONENT);

    public static SimpleFieldSearchConstantsWithEmpty forComponent()
    {
        return COMPONENT;
    }

    /**
     * The "SearcherId" for affected version comes from the DocumentConstants as per 3.13.
     */
    private static final SimpleFieldSearchConstantsWithEmpty AFFECTED_VERSION = new SimpleFieldSearchConstantsWithEmpty(
        DocumentConstants.ISSUE_VERSION, new ClauseNames("affectedVersion"), "version", DocumentConstants.ISSUE_VERSION, VersionManager.NO_VERSIONS,
        FieldIndexer.NO_VALUE_INDEX_VALUE, IssueFieldConstants.AFFECTED_VERSIONS, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY,
        JiraDataTypes.VERSION);

    public static SimpleFieldSearchConstantsWithEmpty forAffectedVersion()
    {
        return AFFECTED_VERSION;
    }

    /**
     * The "SearcherId" for fixFor version comes from the DocumentConstants as per 3.13.
     */
    private static final SimpleFieldSearchConstantsWithEmpty FIXFOR_VERSION = new SimpleFieldSearchConstantsWithEmpty(
        DocumentConstants.ISSUE_FIXVERSION, new ClauseNames("fixVersion"), "fixfor", DocumentConstants.ISSUE_FIXVERSION, VersionManager.NO_VERSIONS,
        FieldIndexer.NO_VALUE_INDEX_VALUE, IssueFieldConstants.FIX_FOR_VERSIONS, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY,
        JiraDataTypes.VERSION);

    public static SimpleFieldSearchConstantsWithEmpty forFixForVersion()
    {
        return FIXFOR_VERSION;
    }

    private static final SimpleFieldSearchConstants RESOLUTION = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_RESOLUTION,
        IssueFieldConstants.RESOLUTION, IssueFieldConstants.RESOLUTION, IssueFieldConstants.RESOLUTION, IssueFieldConstants.RESOLUTION,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.RESOLUTION);

    public static SimpleFieldSearchConstants forResolution()
    {
        return RESOLUTION;
    }

    private static final SimpleFieldSearchConstants STATUS = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_STATUS,
        IssueFieldConstants.STATUS, IssueFieldConstants.STATUS, IssueFieldConstants.STATUS, IssueFieldConstants.STATUS,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.STATUS);

    public static SimpleFieldSearchConstants forStatus()
    {
        return STATUS;
    }

    private static final ClauseInformation STATUS_CATEGORY = new DefaultClauseInformation(DocumentConstants.ISSUE_STATUS,
            "statusCategory", null, OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.STATUS_CATEGORY);

    public static ClauseInformation forStatusCategory()
    {
        return STATUS_CATEGORY;
    }

    private static final SimpleFieldSearchConstants SUMMARY = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_SUMMARY,
        IssueFieldConstants.SUMMARY, "summary", IssueFieldConstants.SUMMARY, IssueFieldConstants.SUMMARY, OperatorClasses.TEXT_OPERATORS, JiraDataTypes.TEXT);

    public static SimpleFieldSearchConstants forSummary()
    {
        return SUMMARY;
    }

    private static final SimpleFieldSearchConstants DESCRIPTION = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_DESC,
        IssueFieldConstants.DESCRIPTION, "description", IssueFieldConstants.DESCRIPTION, IssueFieldConstants.DESCRIPTION, OperatorClasses.TEXT_OPERATORS,
        JiraDataTypes.TEXT);

    public static SimpleFieldSearchConstants forDescription()
    {
        return DESCRIPTION;
    }

    private static final SimpleFieldSearchConstants ENVIRONMENT = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_ENV,
        IssueFieldConstants.ENVIRONMENT, "environment", IssueFieldConstants.ENVIRONMENT, IssueFieldConstants.ENVIRONMENT, OperatorClasses.TEXT_OPERATORS,
        JiraDataTypes.TEXT);

    public static SimpleFieldSearchConstants forEnvironment()
    {
        return ENVIRONMENT;
    }

    private static final SimpleFieldSearchConstantsWithEmpty LABELS = new SimpleFieldSearchConstantsWithEmpty(
        DocumentConstants.ISSUE_LABELS, "labels", "labels", DocumentConstants.ISSUE_LABELS, FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE,
        FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE, IssueFieldConstants.LABELS, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
        JiraDataTypes.LABEL);

    public static SimpleFieldSearchConstantsWithEmpty forLabels()
    {
        return LABELS;
    }

    public static CommentsFieldSearchConstants forComments()
    {
        return CommentsFieldSearchConstants.getInstance();
    }

    private static final SimpleFieldSearchConstants CREATED_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_CREATED, new ClauseNames(
        IssueFieldConstants.CREATED, "createdDate"), IssueFieldConstants.CREATED, IssueFieldConstants.CREATED, IssueFieldConstants.CREATED,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forCreatedDate()
    {
        return CREATED_DATE;
    }

    private static final SimpleFieldSearchConstants UPDATE_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_UPDATED, new ClauseNames(
        IssueFieldConstants.UPDATED, "updatedDate"), IssueFieldConstants.UPDATED, IssueFieldConstants.UPDATED, IssueFieldConstants.UPDATED,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forUpdatedDate()
    {
        return UPDATE_DATE;
    }

    private static final SimpleFieldSearchConstants LAST_VIEWED_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_ID,
        new ClauseNames(IssueFieldConstants.LAST_VIEWED), IssueFieldConstants.LAST_VIEWED, IssueFieldConstants.LAST_VIEWED, IssueFieldConstants.LAST_VIEWED,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forLastViewedDate()
    {
        return LAST_VIEWED_DATE;
    }

    private static final SimpleFieldSearchConstants DUE_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_DUEDATE, new ClauseNames("due",
        IssueFieldConstants.DUE_DATE), IssueFieldConstants.DUE_DATE, IssueFieldConstants.DUE_DATE, IssueFieldConstants.DUE_DATE,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forDueDate()
    {
        return DUE_DATE;
    }

    private static final SimpleFieldSearchConstants RESOLUTION_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_RESOLUTION_DATE,
        new ClauseNames("resolved", IssueFieldConstants.RESOLUTION_DATE), IssueFieldConstants.RESOLUTION_DATE, IssueFieldConstants.RESOLUTION_DATE,
        IssueFieldConstants.RESOLUTION_DATE, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forResolutionDate()
    {
        return RESOLUTION_DATE;
    }

    private static final UserFieldSearchConstantsWithEmpty REPORTER = new UserFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_AUTHOR,
        IssueFieldConstants.REPORTER, "reporter", "reporterSelect", IssueFieldConstants.REPORTER, DocumentConstants.ISSUE_NO_AUTHOR,
        IssueFieldConstants.REPORTER, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    public static UserFieldSearchConstantsWithEmpty forReporter()
    {
        return REPORTER;
    }

    private static final UserFieldSearchConstantsWithEmpty ASSIGNEE = new UserFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_ASSIGNEE,
        IssueFieldConstants.ASSIGNEE, "assignee", "assigneeSelect", IssueFieldConstants.ASSIGNEE, DocumentConstants.ISSUE_UNASSIGNED,
        IssueFieldConstants.ASSIGNEE, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    public static UserFieldSearchConstantsWithEmpty forAssignee()
    {
        return ASSIGNEE;
    }

    private static final UserFieldSearchConstantsWithEmpty CREATOR = new UserFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_CREATOR,
            IssueFieldConstants.CREATOR, "creator", "creatorSelect", IssueFieldConstants.CREATOR, DocumentConstants.ISSUE_ANONYMOUS_CREATOR,
            IssueFieldConstants.CREATOR, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    public static UserFieldSearchConstantsWithEmpty forCreator()
    {
        return CREATOR;
    }

    public static SavedFilterSearchConstants forSavedFilter()
    {
        return SavedFilterSearchConstants.getInstance();
    }

    public static AllTextSearchConstants forAllText()
    {
        return AllTextSearchConstants.getInstance();
    }

    public static IssueIdConstants forIssueId()
    {
        return IssueIdConstants.getInstance();
    }

    public static IssueKeyConstants forIssueKey()
    {
        return IssueKeyConstants.getInstance();
    }

    public static IssueParentConstants forIssueParent()
    {
        return IssueParentConstants.getInstance();
    }

    private static final SimpleFieldSearchConstants WORK_RATIO = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_WORKRATIO,
        IssueFieldConstants.WORKRATIO, IssueFieldConstants.WORKRATIO, IssueFieldConstants.WORKRATIO, IssueFieldConstants.WORKRATIO,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.NUMBER);

    public static SimpleFieldSearchConstants forWorkRatio()
    {
        return WORK_RATIO;
    }

    private static final DefaultClauseInformation CURRENT_ESTIMATE = new DefaultClauseInformation(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR,
        new ClauseNames("remainingEstimate", IssueFieldConstants.TIME_ESTIMATE), IssueFieldConstants.TIME_ESTIMATE,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DURATION);

    public static DefaultClauseInformation forCurrentEstimate()
    {
        return CURRENT_ESTIMATE;
    }

    private static final DefaultClauseInformation ORIGINAL_ESTIMATE = new DefaultClauseInformation(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG,
        new ClauseNames("originalEstimate", IssueFieldConstants.TIME_ORIGINAL_ESTIMATE), IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DURATION);

    public static DefaultClauseInformation forOriginalEstimate()
    {
        return ORIGINAL_ESTIMATE;
    }

    private static final DefaultClauseInformation TIME_SPENT = new DefaultClauseInformation(DocumentConstants.ISSUE_TIME_SPENT,
        IssueFieldConstants.TIME_SPENT, IssueFieldConstants.TIME_SPENT, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DURATION);

    public static DefaultClauseInformation forTimeSpent()
    {
        return TIME_SPENT;
    }

    private static final DefaultClauseInformation SECURITY_LEVEL = new DefaultClauseInformation(DocumentConstants.ISSUE_SECURITY_LEVEL,
        new ClauseNames("level"), IssueFieldConstants.SECURITY, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.ISSUE_SECURITY_LEVEL);

    public static DefaultClauseInformation forSecurityLevel()
    {
        return SECURITY_LEVEL;
    }

    private static final DefaultClauseInformation VOTES = new DefaultClauseInformation(DocumentConstants.ISSUE_VOTES, "votes",
        IssueFieldConstants.VOTES, OperatorClasses.EQUALITY_AND_RELATIONAL, JiraDataTypes.NUMBER);

    public static DefaultClauseInformation forVotes()
    {
        return VOTES;
    }

    private static final DefaultClauseInformation VOTERS = new DefaultClauseInformation(DocumentConstants.ISSUE_VOTERS, "voter",
        IssueFieldConstants.VOTERS, OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.USER);

    public static DefaultClauseInformation forVoters()
    {
        return VOTERS;
    }

    private static final DefaultClauseInformation WATCHES = new DefaultClauseInformation(DocumentConstants.ISSUE_WATCHES,
        new ClauseNames("watchers"), IssueFieldConstants.WATCHES, OperatorClasses.EQUALITY_AND_RELATIONAL, JiraDataTypes.NUMBER);

    public static DefaultClauseInformation forWatches()
    {
        return WATCHES;
    }

    private static final DefaultClauseInformation WATCHERS = new DefaultClauseInformation(DocumentConstants.ISSUE_WATCHERS, "watcher",
        IssueFieldConstants.WATCHERS, OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.USER);

    public static DefaultClauseInformation forWatchers()
    {
        return WATCHERS;
    }

    private static final DefaultClauseInformation ATTACHMENT = new DefaultClauseInformation(DocumentConstants.ISSUE_ATTACHMENT, "attachments",
            IssueFieldConstants.ATTACHMENT, OperatorClasses.EMPTY_ONLY_OPERATORS, JiraDataTypes.ATTACHMENT);

    public static DefaultClauseInformation forAttachments()
    {
        return ATTACHMENT;
    }

    private static final DefaultClauseInformation PROJECT_CATEGORY = new DefaultClauseInformation(DocumentConstants.PROJECT_ID, "category", null,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.PROJECT_CATEGORY);

    public static DefaultClauseInformation forProjectCategory()
    {
        return PROJECT_CATEGORY;
    }

    private static final DefaultClauseInformation PROGRESS = new DefaultClauseInformation(DocumentConstants.ISSUE_PROGRESS, "progress",
            IssueFieldConstants.PROGRESS, OperatorClasses.EQUALITY_AND_RELATIONAL, JiraDataTypes.NUMBER);

    public static DefaultClauseInformation forProgress()
    {
        return PROGRESS;
    }

    private static final Set<String> SYSTEM_NAMES;

    public static Set<String> getSystemNames()
    {
        return SYSTEM_NAMES;
    }

    public static boolean isSystemName(final String name)
    {
        return SYSTEM_NAMES.contains(name);
    }

    //NOTE: This code must be after all the static variable declarations that we need to access. Basically, make this
    //the last code in the file.
    static
    {
        Set<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try
        {
            for (final Method constantMethod : getConstantMethods())
            {
                names.addAll(getNames(constantMethod));
            }
        }
        catch (final RuntimeException e)
        {
            getLogger().error("Unable to calculate system JQL names: Unexpected Error.", e);
            names = Collections.emptySet();
        }
        SYSTEM_NAMES = Collections.unmodifiableSet(names);
    }

    private static final Map<String, ClauseInformation> CLAUSE_INFORMATION_MAP = Maps.uniqueIndex(
        ImmutableSet.of(
            forAffectedVersion(),
            forAllText(),
            forAssignee(),
            forComments(),
            forComponent(),
            forCreatedDate(),
            forCreator(),
            forCurrentEstimate(),
            forDescription(),
            forDueDate(),
            forEnvironment(),
            forFixForVersion(),
            forIssueId(),
            forIssueKey(),
            forIssueParent(),
            forIssueType(),
            forLabels(),
            forLastViewedDate(),
            forOriginalEstimate(),
            forPriority(),
            forProgress(),
            forProject(),
            forProjectCategory(),
            forReporter(),
            forResolution(),
            forResolutionDate(),
            forSavedFilter(),
            forSecurityLevel(),
            forStatus(),
            forStatusCategory(),
            forSummary(),
            forTimeSpent(),
            forUpdatedDate(),
            forVoters(),
            forVotes(),
            forWatchers(),
            forWatches(),
            forWorkRatio(),
            forAttachments(),
            forIssueProperty()
        ), new Function<ClauseInformation, String>()
        {
            @Override
            public String apply(ClauseInformation input)
            {
                return input.getFieldId() != null ? input.getFieldId() : input.getJqlClauseNames().getPrimaryName();
            }
        }
    );

    public static ClauseInformation forIssueProperty()
    {
        return new PropertyClauseInformation(new ClauseNames(ISSUE_PROPERTY));
    }

    public static ClauseInformation getClauseInformationById(String id)
    {
        return CLAUSE_INFORMATION_MAP.get(id);
    }

    private static Collection<String> getNames(final Method constantMethod)
    {
        try
        {
            final ClauseInformation information = (ClauseInformation) constantMethod.invoke(null);
            if (information == null)
            {
                logConstantError(constantMethod, "Clause information was not available.", null);
                return Collections.emptySet();
            }

            final ClauseNames names = information.getJqlClauseNames();
            if (names == null)
            {
                logConstantError(constantMethod, "The ClauseName was not available.", null);
                return Collections.emptySet();
            }

            final Set<String> strings = names.getJqlFieldNames();
            if (strings == null)
            {
                logConstantError(constantMethod, "The ClauseName returned no values.", null);
                return Collections.emptySet();
            }

            return strings;
        }
        catch (final InvocationTargetException e)
        {
            Throwable exception;
            if (e.getTargetException() != null)
            {
                exception = e.getTargetException();
            }
            else
            {
                exception = e;
            }
            logConstantError(constantMethod, null, exception);
        }
        catch (final IllegalAccessException e)
        {
            logConstantError(constantMethod, null, e);
        }
        catch (final SecurityException e)
        {
            logConstantError(constantMethod, "Security Error.", e);
        }
        catch (final RuntimeException e)
        {
            logConstantError(constantMethod, "Unexpected Error.", e);
        }
        return Collections.emptySet();
    }

    private static Collection<Method> getConstantMethods()
    {
        final Method[] methods;
        try
        {
            methods = SystemSearchConstants.class.getMethods();
        }
        catch (final SecurityException e)
        {
            getLogger().error("Unable to calculate system JQL names: " + e.getMessage(), e);
            return Collections.emptySet();
        }

        final List<Method> returnMethods = new ArrayList<Method>(methods.length);
        for (final Method method : methods)
        {
            final int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
            {
                continue;
            }

            if (method.getParameterTypes().length != 0)
            {
                continue;
            }

            final Class<?> returnType = method.getReturnType();
            if (!ClauseInformation.class.isAssignableFrom(returnType))
            {
                continue;
            }

            returnMethods.add(method);
        }

        return returnMethods;
    }

    private static void logConstantError(final Method constantMethod, final String msg, final Throwable th)
    {
        String actualMessage = msg;
        if ((msg == null) && (th != null))
        {
            actualMessage = th.getMessage();
        }

        getLogger().error("Unable to calculate system JQL names for '" + constantMethod.getName() + "': " + actualMessage, th);
    }

    private static Logger getLogger()
    {
        return Logger.getLogger(SystemSearchConstants.class);
    }
}
