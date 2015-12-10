package com.atlassian.jira.functest.framework.admin;

/**
 * Actions to be performed on the issue security schemes in JIRA's administration.
 *
 * @since v4.1
 */
public interface IssueSecuritySchemes
{
    /**
     * API for managing issue security scheme data in JIRA.
     *
     */
    interface IssueSecurityScheme {

        IssueSecurityLevel getLevel(String name);

        IssueSecurityLevel newLevel(String name, String description);
    }

    // TODO port remaining methods from JIRAWebTest

    /**
     * Get issue permission
     *
     * @param name
     * @return
     */
    IssueSecurityScheme getScheme(String name);

    /**
     * Create new issue security with given <tt>name</tt> and
     * <tt>description</tt>. 
     *
     * @param name name of the security scheme to create
     * @param description description of the security scheme to create
     */
    IssueSecurityScheme newScheme(String name, String description);

    /**
     * Delete issue security scheme with given name.
     *
     * @param name
     * @return
     */
    IssueSecuritySchemes deleteScheme(String name);

}
