package com.atlassian.jira.web.tags;

import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.servlet.jsp.JspException;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.component.IssuePager;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Holds the unit tests for {@link com.atlassian.jira.web.tags.IssueTableTag}
 *
 * @since v6.3.8
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssueTableTag
{
    @Mock
    private IssueTableWebComponent mockIssueTableWebComponent;

    @Mock
    private IssuePager mockIssuePager;

    @Mock
    private IssueTableLayoutBean mockLayoutBean;

    @Mock
    private Issue mockIssue;

    @Mock
    private Writer mockWriter;

    private long selctedIssueKey = 0l;

    @Test
    public void isStreamyMethodCalledOnWebComponent() throws JspException
    {
        final List<Issue> issues = Arrays.asList(mockIssue);
        final IssueTableTag issueTableTag = createWebResourceRequireTag();
        issueTableTag.setLayoutBean(mockLayoutBean);
        issueTableTag.setIssuePager(mockIssuePager);
        issueTableTag.setIssues(issues);
        issueTableTag.setSelectedIssueId(selctedIssueKey);
        issueTableTag.doEndTag();

        verify(mockIssueTableWebComponent).asHtml(mockWriter, mockLayoutBean, issues, mockIssuePager, selctedIssueKey);
    }

    private IssueTableTag createWebResourceRequireTag()
    {
        return new IssueTableTag()
        {
            private static final long serialVersionUID = 1557257791852341077L;

            @Override
            protected Writer getJspWriter()
            {
                return mockWriter;
            }

            @Override
            protected IssueTableWebComponent getIssueTableWebComponent()
            {
                return mockIssueTableWebComponent;
            }
        };
    }
}
