package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;

/**
 * Default implementation of
 * {@link IssueSecurityLevel}.
 *
 * @since v4.2
 */
public class IssueSecurityLevelImpl extends AbstractFuncTestUtil implements IssueSecurityLevel
{
    private static final String ADD_SECURITY_LINK_ID_FORMAT = "add_%s";

    private final IssueSecuritySchemesImpl schemes;
    private final String name;
    private final String addSecurityLinkId;


    public IssueSecurityLevelImpl(IssueSecuritySchemesImpl schemes, String name)
    {
        super(schemes.tester(), schemes.envData(), 3);
        this.schemes = schemes;
        this.name = name;
        this.addSecurityLinkId = createLinkId();
    }

    private String createLinkId()
    {
        return String.format(ADD_SECURITY_LINK_ID_FORMAT, this.name);
    }

    public IssueSecuritySchemes.IssueSecurityScheme scheme()
    {
        return schemes;
    }

    public IssueSecurityLevel addIssueSecurity(IssueSecurity issueSecurity)
    {
        tester.clickLink(addSecurityLinkId);
        issueSecurity.chooseOnForm(tester);
        tester.submit();
        return this;
    }

    public IssueSecurityLevel addIssueSecurity(IssueSecurity issueSecurity, String paramValue)
    {
        tester.clickLink(addSecurityLinkId);
        issueSecurity.chooseOnForm(tester, paramValue);
        tester.submit();
        return this;
    }

}
