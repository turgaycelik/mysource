package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactoryImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.web.bean.TimeTrackingGraphBean;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactoryImpl;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * This is a test for {@link com.atlassian.jira.issue.views.util.DefaultIssueViewUtil}.
 *
 * @since v4.1
 */
public class TestDefaultIssueViewUtil extends MockControllerTestCase
{
    @Test
    public void testCreateAggregateBean() throws Exception
    {
        MockIssue issue = new MockIssue(1);

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(78L, 28L, 38L, 0);

        final AggregateTimeTrackingCalculator calculator = getMock(AggregateTimeTrackingCalculator.class);
        expect(calculator.getAggregates(issue)).andReturn(bean);

        final AggregateTimeTrackingCalculatorFactory factory = getMock(AggregateTimeTrackingCalculatorFactoryImpl.class);
        expect(factory.getCalculator(issue)).andReturn(calculator);

        final DefaultIssueViewUtil defaultIssueViewUtil = instantiate(DefaultIssueViewUtil.class);

        assertSame(bean, defaultIssueViewUtil.createAggregateBean(issue));

        verify();
    }

    @Test
    public void testCreateTimeTrackingBean() throws Exception
    {
        final AggregateTimeTrackingBean aggBean = new AggregateTimeTrackingBean(78L, 28L, 38L, 0);

        final NoopI18nHelper nHelper = new NoopI18nHelper();
        TimeTrackingGraphBean ttBean = new TimeTrackingGraphBean(new TimeTrackingGraphBean.Parameters(nHelper));

        final TimeTrackingGraphBeanFactory graphBeanFactory = getMock(TimeTrackingGraphBeanFactoryImpl.class);
        expect(graphBeanFactory.createBean(EasyMock.same(aggBean), eq(TimeTrackingGraphBeanFactory.Style.NORMAL), EasyMock.same(nHelper))).andReturn(ttBean);

        final DefaultIssueViewUtil defaultIssueViewUtil = instantiate(DefaultIssueViewUtil.class);
        assertSame(ttBean, defaultIssueViewUtil.createTimeTrackingBean(aggBean, nHelper));

        verify();
    }
}
