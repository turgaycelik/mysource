package com.atlassian.jira.startup;

import java.io.File;

import javax.servlet.ServletContext;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestClusteringChecklistLauncher
{
    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock @AvailableInContainer private ClusterManager mockClusterManager;
    @Mock @AvailableInContainer private ClusterNodeProperties mockClusterNodeProperties;
    @Mock @AvailableInContainer private I18nHelper.BeanFactory mockI18nHelperBeanFactory;
    @Mock @AvailableInContainer private JiraHome mockJiraHome;

    @Mock private ServletContext servletContext;
    @Mock private JohnsonEventContainer mockJohnsonEventContainer;
    private MockI18nHelper i18nHelper = new MockI18nHelper();

    @Before
    public void setupMocks()
    {
        when(servletContext.getAttribute(JohnsonEventContainer.class.getName())).thenReturn(mockJohnsonEventContainer);
        when(mockI18nHelperBeanFactory.getInstance(any(User.class))).thenReturn(i18nHelper);
    }

    @Test
    public void failureCausesMultipleJohnsonEvent()
    {
        when(mockClusterManager.isClustered()).thenReturn(true);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(true);

        final ClusteringChecklistLauncher checklistLauncher = new ClusteringChecklistLauncher(servletContext);
        checklistLauncher.start();

        verify(mockJohnsonEventContainer, times(3)).addEvent(any(Event.class));
    }

    @Test
    public void singleFailureCausesSingleJohnsonEvent()
    {
        when(mockClusterManager.isClustered()).thenReturn(true);
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(true);
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");

        final ClusteringChecklistLauncher checklistLauncher = new ClusteringChecklistLauncher(servletContext);
        checklistLauncher.start();

        verify(mockJohnsonEventContainer).addEvent(any(Event.class));
    }

    @Test
    public void successDoesNotRaiseJohnsonEvent()
    {
        when(mockClusterManager.isClustered()).thenReturn(true);
        when(mockClusterManager.isClusterLicensed()).thenReturn(true);
        when(mockClusterNodeProperties.propertyFileExists()).thenReturn(true);
        when(mockClusterNodeProperties.getNodeId()).thenReturn("a");
        when(mockClusterNodeProperties.getSharedHome()).thenReturn("b");
        when(mockJiraHome.getHome()).thenReturn(new File("b"));
        when(mockJiraHome.getLocalHome()).thenReturn(new File("c"));

        final ClusteringChecklistLauncher checklistLauncher = new ClusteringChecklistLauncher(servletContext);
        checklistLauncher.start();

        verify(mockJohnsonEventContainer, never()).addEvent(any(Event.class));
    }
}
