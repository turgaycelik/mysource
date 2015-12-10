package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Default implementation of {@link com.atlassian.jira.functest.framework.admin.IssueSecuritySchemes}.
 *
 * @since v4.2
 */
public class IssueSecuritySchemesImpl extends AbstractFuncTestUtil implements IssueSecuritySchemes,
        IssueSecuritySchemes.IssueSecurityScheme {

    private static final String ADD_SECURITY_SCHEME_LINK_ID = "add_securityscheme";
    private static final String DEL_SECURITY_SCHEME_LINK_ID_FORMAT = "del_%s";
    private static final String FORM_PARAM_NAME = "name";
    private static final String FORM_PARAM_DESC = "description";

    private String currentSchemeName;

    public IssueSecuritySchemesImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    private static String deleteSchemeLinkFor(String schemeName)
    {
        return String.format(DEL_SECURITY_SCHEME_LINK_ID_FORMAT, schemeName);
    }

    WebTester tester()
    {
        return tester;
    }

    JIRAEnvironmentData envData()
    {
        return environmentData;
    }

    public IssueSecurityScheme getScheme(String name)
    {
        gotoIssueSecuritySchemes();
        tester.assertLinkPresentWithText(name);
        tester.clickLinkWithText(name);
        currentSchemeName = name;
        return this;
    }

    public IssueSecurityScheme newScheme(String name, String description)
    {
        gotoIssueSecuritySchemes();
        tester.clickLink(ADD_SECURITY_SCHEME_LINK_ID);
        tester.setFormElement(FORM_PARAM_NAME, name);
        tester.setFormElement(FORM_PARAM_DESC, description);
        tester.submit("Add");
        return getScheme(name);
    }

    public IssueSecuritySchemes deleteScheme(final String name)
    {
        gotoIssueSecuritySchemes();
        tester.clickLink(deleteSchemeLinkFor(name));
        tester.submit("Delete");
        tester.assertLinkNotPresentWithText(name);
        return this;
    }

    public IssueSecurityLevel getLevel(String name)
    {
        return new IssueSecurityLevelImpl(this, name);
    }

    public IssueSecurityLevel newLevel(String name, String description)
    {
        tester.setFormElement(FORM_PARAM_NAME, name);
        tester.setFormElement(FORM_PARAM_DESC, description);
        tester.submit("Add Security Level");
        tester.assertLinkPresent("add_" + name);
        return new IssueSecurityLevelImpl(this, name);
    }

    private void gotoIssueSecuritySchemes()
    {
        getNavigation().gotoAdminSection("security_schemes");
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }

}
