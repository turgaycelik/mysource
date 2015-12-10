package com.atlassian.jira.functest.framework.fields;

/**
 * If you're tired of continuously redefining the same constants for fields in all your functional tests, why not keep
 * them here in one place?
 * <p/>
 * These are constants intended for use in setting form elements, e.g. <code>tester.setFormElement(xxx, "some value");</code>.
 * They could also be used in assertions or anything you like really.
 *
 * @since v4.2
 */
public interface EditFieldConstants
{
    // TimeTracking related fields
    String TIMETRACKING = "timetracking";
    String TIMETRACKING_ORIGINALESTIMATE = TIMETRACKING + "_originalestimate";
    String TIMETRACKING_REMAININGESTIMATE = TIMETRACKING + "_remainingestimate";

    // Worklog related fields
    String WORKLOG = "worklog";
    String WORKLOG_ACTIVATE = WORKLOG + "_activate";
    String WORKLOG_TIMELOGGED = WORKLOG + "_timeLogged";

    String DUEDATE = "duedate";

    String REPORTER = "reporter";

    String SUMMARY = "summary";
}
