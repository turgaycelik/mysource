package com.atlassian.jira.bulkedit.operation;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.JiraActionSupport;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.web.bean.BulkEditBean;

/**
 * This interface is implemented by plugin vendors to define new bulk operations.
 * <p/>
 * Bulk operations are added by plugins via {@link com.atlassian.jira.bulkedit.BulkOperationManager#addBulkOperation}.
 *
 * @see com.atlassian.jira.bulkedit.BulkOperationManager
 * @deprecated Since 6.3.6 use {@link com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation}
 */
@PublicSpi
public interface BulkOperation extends IssueOperation
{
    /**
     * Determines whether the operation can be performed with the given set of issues
     *
     * @see com.atlassian.jira.web.bean.BulkEditBean#getSelectedIssues()
     */
    public boolean canPerform(BulkEditBean bulkEditBean, User remoteUser);

    /**
     * Performs the operation on the given set of issues
     */
    public void perform(BulkEditBean bulkEditBean, User remoteUser) throws Exception;

    /**
     * Returns the &quot;operation name&quot;.
     * <p/>
     * <p>This is used to build up the name of the {@link JiraActionSupport action} used to render the details screen
     * in the bulk change UI.</p>
     * <p/>
     * <p>The action name that is generated as {@code operationName + "Details.jspa"}. See BulkChooseOperation
     * for details</p>
     *
     * @return The &quot;operation name&quot;
     */
    public String getOperationName();

    /**
     * An i18n key to be used to render the error message to be displayed when the user can not perform this bulk
     * operation.
     *
     * @return An i18n key to be used to render the error message to be displayed when the user can not perform this bulk
     * operation.
     */
    String getCannotPerformMessageKey();
}
