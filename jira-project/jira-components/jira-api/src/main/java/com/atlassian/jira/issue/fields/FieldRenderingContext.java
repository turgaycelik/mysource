/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.issue.fields;

/**
 * FieldRenderingContext strings are passed in the displayParameters map as keys with a TRUE value. This allows us to test that
 * we are for instance on the ViewIssue page, or rendering an excel view.
 *
 * @since v3.12
 */
public final class FieldRenderingContext
{
    /**
     * used to signify we are on the ViewIssue page
     */
    public static final String ISSUE_VIEW = "view_issue";

    /**
     * used to signify we are rendering for excel
     */
    public static final String EXCEL_VIEW = "excel_view";

    /**
     * used to signify we are rendering for emails
     */
    public static final String EMAIL_VIEW = "email_view";

    /**
     * This is a display hint that is placed into the displayParameters when rendering the issue navigator. This value
     * allows field views the ability to determine whether a field is being rendered in the context of the issue
     * navigator or in some other context. An example of where this is used can be found in the jira-labels-plugin in
     * the column-view-label.vm.
     */
    public static final String NAVIGATOR_VIEW = "navigator_view";


    /**
     * used to signify we are rendering simplified view used for printing
     */
    public static final String PRINT_VIEW = "print_view";
}
