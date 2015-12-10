package com.atlassian.jira.plugin.searchrequestview;

import java.io.IOException;
import java.io.Writer;

/**
 * This interface defines an access error handler for the search request views. This is used
 * to allow customization of search request permission violation and search request not found
 * errors.
 */
public interface SearchRequestViewAccessErrorHandler
{
    public static final String PERMISSION_VIOLATION = "permissionViolation";
    public static final String SEARCH_REQUEST_DOES_NOT_EXIST = "searchRequestDoesNotExist";

    /**
     * Prints the HTML headers for non-typial HTML such as Word or Excel views.
     * (e.g.: requestHeaders.addHeader("content-disposition", "attachment;filename="sample.doc";");)
     * This method will be called before either writeSearchRequestDoesNotExistError or writePermissionViolationError.
     * <p/>
     * <strong>Please make certain that you set the content type and character set in this method.</strong>
     * @param requestHeaders
     */
    public void writeErrorHeaders(RequestHeaders requestHeaders);

    /**
     * Prints the error to display to the users if there has been a permission violation error looking up the
     * SearchRequest that this view is meant to service.
     * @param writer
     */
    public void writePermissionViolationError(Writer writer) throws IOException;

    /**
     * Prints the error to display to the users if there has been a permission violation error looking up the
     * SearchRequest that this view is meant to service.
     * @param writer
     */
    public void writeSearchRequestDoesNotExistError(Writer writer) throws IOException;
}
