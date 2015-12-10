package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.SecurityLevelJsonBean;

/**
 * @since v5.0
 */
class SecurityLevelBeanExample
{
    /**
     * Example SecurityLevel bean. JSON:
     */
    static final SecurityLevelJsonBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new SecurityLevelJsonBean();
        DOC_EXAMPLE.setId("10000");
        DOC_EXAMPLE.setDescription("Only the reporter and internal staff can see this issue.");
        DOC_EXAMPLE.setName("Reporter Only");
        DOC_EXAMPLE.setSelf("http://localhost:8090/jira/rest/api/2/securitylevel/10021");
    }
}
