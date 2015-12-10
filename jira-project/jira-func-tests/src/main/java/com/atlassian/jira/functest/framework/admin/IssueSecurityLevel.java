package com.atlassian.jira.functest.framework.admin;

import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * API to manage JIRA administrative tasks with regard to
 * issue security level.
 *
 */
public interface IssueSecurityLevel
{

    enum IssueSecurity
    {
        REPORTER("reporter", false),
        CURRENT_ASIGNEE("assignee", false),
        PROJECT_LEAD("lead", false),
        GROUP("group", true) {
            @Override
            void addParameter(final WebTester tester, String paramValue)
            {
                notNull("paramValue", paramValue);
                tester.selectOption(typeId(), paramValue);
            }
        },
        USER("user", true) {
            @Override
            void addParameter(final WebTester tester, String paramValue)
            {
                notNull("paramValue", paramValue);
                tester.setFormElement(typeId(), paramValue);
            }
        };
        // TODO other if necessary
        private static final String ISSUE_SECURITY_TYPE_RADIO_NAME = "type";

        private final String typeId;
        private final boolean hasParameter;

        IssueSecurity(final String typeId, final boolean hasParameter)
        {
            this.typeId = typeId;
            this.hasParameter = hasParameter;
        }

        public String typeId()
        {
            return typeId;
        }

        public boolean hasParameter()
        {
            return hasParameter;
        }

        final void chooseOnForm(WebTester tester)
        {
            if (hasParameter)
            {
                throw new IllegalStateException("this may be called ONLY for parameterless types");
            }
            chooseOnForm(tester, null);
        }

        final void chooseOnForm(WebTester tester, String paramValue)
        {
            if (!hasParameter && paramValue != null)
            {
                throw new IllegalArgumentException("param provided for parameterless issue security type");
            }
            tester.getDialog().setFormParameter(ISSUE_SECURITY_TYPE_RADIO_NAME, typeId);
            if (hasParameter)
            {
                addParameter(tester, paramValue);
            }
        }

        void addParameter(WebTester tester, String paramValue)
        {
            throw new AbstractMethodError("should be implemented in relevant enum values");
        }

    }

    /**
     * Get scheme of this security level.
     *
     * @return
     */
    IssueSecuritySchemes.IssueSecurityScheme scheme();

    /**
     * Add parameterless issue security (e.g. IssueSecurity#REPORTER, IssueSecurity#CURRENT_ASIGNEE)
     * to this issue security level.
     *
     * @param issueSecurity
     * @return
     */
    IssueSecurityLevel addIssueSecurity(IssueSecurity issueSecurity);

    /**
     * Add parametrized issue security (e.g. IssueSecurity#GROUP, IssueSecurity#USER)
     * to this issue security level, along with parameter value.
     *
     * @param issueSecurity
     * @param paramValue
     * @return
     * @throws NullPointerException if any of the arguments is <code>null</code>
     */
    IssueSecurityLevel addIssueSecurity(IssueSecurity issueSecurity, String paramValue);

    // TODO API for parameterized types - groups, users etc.
}
