package com.atlassian.jira.plugin.issueview;

import java.util.Set;

/**
 * The purpose of this interface is to provide information about requested fields to @{link IssueView#getContent} and @{link IssueView#wriiteHeaders} methods.
 * Requested fields are evaluated based on "field" url parameters in @{link IssueViewReqestParamsHelperImpl#getIssueViewFieldParams} method
 *
 * @since v4.00
 */
public interface IssueViewFieldParams
{
    /**
     * Method returns set of field ids requested for view. Set contains all field ids including non-orderable fields
     *
     * @return set of non-custom field ids requested in view parameters
     */
    Set<String> getFieldIds();

    /**
     * Method returns set of custom field ids requested for view.
     *
     * @return set of custom field ids requested in view parameters
     */
    Set<String> getCustomFieldIds();

    /**
     * Method returns set of orderable field ids requested for view.
     *
     * @return set of orderable field ids requested in view parameters
     */
    Set<String> getOrderableFieldIds();

    /**
     * Method returns true if allcustom parameter in url was specified, otherwise false
     *
     * @return true if allcustom parameter in url was specified, otherwise false
     */
    boolean isAllCustomFields();

    /**
     * Method returns true if any valid field was specified in url parameters
     *
     * @return true if any valid field was requested
     */
    boolean isAnyFieldDefined();

    /**
     * Method returns true if custom issue view was requested by specyfing "field" parameter in request url
     *
     * @return true if custom issue view was requested
     */
    boolean isCustomViewRequested();
}
