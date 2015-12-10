package com.atlassian.jira.bc.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;

import java.util.Collection;

/**
 * This class contains methods for managing JIRA constants such as issue types, statuses, priorities and resolutions.
 *
 * @since v4.2
 */
@PublicApi
public interface ConstantsService
{
    /**
     * Returns a ServiceOutcome containing the Status that has the given id. If the Status with the provided id is not
     * found, or if the calling user does not have permission to see the Status, the ServiceOutcome will contain the
     * appropriate error. Otherwise, the status can be obtained by calling {@link com.atlassian.jira.bc.ServiceOutcome#getReturnedValue()}
     * on the returned ServiceOutcome.
     *
     * @param user the calling user
     * @param statusId a String containing a Status id
     * @return a ServiceOutcome
     */
    ServiceOutcome<Status> getStatusById(User user, String statusId);

    /**
     * Returns a ServiceOutcome containing the Status that has the given Name. If the Status with the provided Name is not
     * found, or if the calling user does not have permission to see the Status, the ServiceOutcome will contain the
     * appropriate error. Otherwise, the status can be obtained by calling {@link com.atlassian.jira.bc.ServiceOutcome#getReturnedValue()}
     * on the returned ServiceOutcome.
     *
     * @param user the calling user
     * @param statusName a String containing a Status name
     * @return a ServiceOutcome
     */
    ServiceOutcome<Status> getStatusByName(User user, String statusName);

    /**
     * Returns a ServiceOutcome containing the Status that has the given Name.
     * The preference is to find the status by its translated name.  If no matching translated name is found the true (untranslated) name
     * will be tried.
     * If the Status with the provided Name is not found, or if the calling user does not have permission to see the Status, the ServiceOutcome will contain the
     * appropriate error. Otherwise, the status can be obtained by calling {@link com.atlassian.jira.bc.ServiceOutcome#getReturnedValue()}
     * on the returned ServiceOutcome.
     *
     * @param user the calling user
     * @param statusName a String containing a Status name
     * @return a ServiceOutcome
     */
    ServiceOutcome<Status> getStatusByTranslatedName(User user, String statusName);

    /**
     * Returns a ServiceOutcome containing all Statuses. Only statuses that the calling user has permission to see
     * will be returned in the ServiceOutcome
     *
     * @param user the calling user
     * @return a ServiceOutcome
     */
    ServiceOutcome<Collection<Status>> getAllStatuses(User user);

    /**
     * Returns a ServiceOutcome containing all IssueTypes. Only issue types that the calling user has permission to see
     * will be returned in the ServiceOutcome
     *
     * @param user the calling user
     * @return a ServiceOutcome
     */
    ServiceOutcome<Collection<IssueType>> getAllIssueTypes(User user);

    /**
     * Returns a ServiceOutcome containing the IssueType that has the given id. If there is no IssueType with the given
     * id, or if the calling user does not have permission to see the IssueType, the ServiceOutcome will contain an
     * error message to that effect. Otherwise, the IssueType can be obtained by calling {@link com.atlassian.jira.bc.ServiceOutcome#getReturnedValue()} on the returned ServiceOutcome.
     *
     * @param user the calling user
     * @param issueTypeId a String containing an IssueType id
     * @return a ServiceOutcome
     */
    ServiceOutcome<IssueType> getIssueTypeById(User user, String issueTypeId);

    /**
     * Returns a ServiceOutcome containing all StatusCategories.
     *
     * @param user the calling user. Will be used for formatting error messages
     * @return a ServiceOutcome
     */
    ServiceOutcome<Collection<StatusCategory>> getAllStatusCategories(User user);

    /**
     * Returns a ServiceOutcome containing StatusCategories visible to any user.
     *
     * @param user the calling user. Will be used for formatting error messages
     * @return a ServiceOutcome
     */
    ServiceOutcome<Collection<StatusCategory>> getUserVisibleStatusCategories(User user);

    /**
     * Returns a ServiceOutcome containing StatusCategory that has the gived id. If the Status with the provided id is not found,
     * the ServiceOutcome will contain the appropriate error.
     * Otherwise, the status can be obtained by calling {@link com.atlassian.jira.bc.ServiceOutcome#getReturnedValue()} on the returned ServiceOutcome.
     *
     * @param user the calling user.  Will be used for formatting error messages
     * @param id a String containing a StatusCategory id
     */
    ServiceOutcome<StatusCategory> getStatusCategoryById(User user, String id);

    /**
     * Returns a ServiceOutcome containing StatusCategory that has the gived key.
     * If the Status with the provided key is not  found, the ServiceOutcome will contain the appropriate error.
     * Otherwise, the status can be obtained by calling {@link com.atlassian.jira.bc.ServiceOutcome#getReturnedValue()} on the returned ServiceOutcome.
     *
     * @param user the calling user.  Will be used for formatting error messages
     * @param key a String containing a StatusCategory id
     */
    ServiceOutcome<StatusCategory> getStatusCategoryByKey(User user, String key);

    /**
     * Returns a ServiceOutcome containing default StatusCategory. The method returns status category independently on dark feature state.
     *
     * @param user the calling User. Will be used for formatting error messages
     * @return a ServiceOutcome
     */
    ServiceOutcome<StatusCategory> getDefaultStatusCategory(User user);

    /**
     * Returns a boolean value indicating whether statuses should appear as lozenges or not.
     *
     * @return a boolean value
     */
    boolean isStatusAsLozengeEnabled();
}
