package com.atlassian.jira.issue.operation;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Map;

@PublicApi
public class IssueOperations
{
    public static final ScreenableIssueOperation CREATE_ISSUE_OPERATION =
            new ScreenableSingleIssueOperationImpl(0L, "admin.issue.operations.create", "admin.issue.operations.create.desc");
    public static final ScreenableIssueOperation EDIT_ISSUE_OPERATION =
            new ScreenableSingleIssueOperationImpl(1L, "admin.issue.operations.edit", "admin.issue.operations.edit.desc");
    public static final ScreenableIssueOperation VIEW_ISSUE_OPERATION =
            new ScreenableSingleIssueOperationImpl(2L, "admin.issue.operations.view", "admin.issue.operations.view.desc");

    // Move Issue is not screenable - it is a wizard, so it is not possible to associate a screen with it.
    public static final IssueOperation MOVE_ISSUE_OPERATION =
            new IssueOperationImpl("admin.issue.operations.move", "admin.issue.operations.move.desc");

    private static final Map<Long, ScreenableIssueOperation> SCREENABLE_ISSUE_OPERATIONS = ImmutableMap.of(
            CREATE_ISSUE_OPERATION.getId(), CREATE_ISSUE_OPERATION,
            EDIT_ISSUE_OPERATION.getId(), EDIT_ISSUE_OPERATION,
            VIEW_ISSUE_OPERATION.getId(), VIEW_ISSUE_OPERATION );

    public static Collection<ScreenableIssueOperation> getIssueOperations()
    {
        return SCREENABLE_ISSUE_OPERATIONS.values();
    }

    public static ScreenableIssueOperation getIssueOperation(Long id)
    {
        return SCREENABLE_ISSUE_OPERATIONS.get(id);
    }

    /**
     * @deprecated There are countless simpler and faster ways to get an {@code I18nHelper} and ask it to translate
     *          something for you.  Asking {@code IssueOperations} to do it does not make any sense.  Since v6.3.
     */
    @Deprecated
    public static String getText(String key)
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText(key);
    }
}
