package com.atlassian.jira.project;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.user.util.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultProjectManagerProjectCreation
{
    @Mock
    private TransactionSupport transactionSupport;
    @Mock
    private OfBizDelegator ofBizDelegator;
    @Mock
    private ProjectKeyStore projectKeyStore;

    private DefaultProjectManager projectManager;

    @Before
    public void setUp()
    {
        projectManager = newProjectManager(transactionSupport, ofBizDelegator, projectKeyStore);
    }

    @Test
    public void createProjectRefreshesTheProjectKeyStoreAfterTheTransactionHasBeenCommitted()
    {
        Transaction transaction = mock(Transaction.class);
        when(transactionSupport.begin()).thenReturn(transaction);

        createProject();
        InOrder inOrder = inOrder(transaction, projectKeyStore);

        inOrder.verify(transaction).commit();
        inOrder.verify(projectKeyStore).refresh();
    }

    @Test
    public void createProjectRefreshesTheCacheEvenIfAnExceptionIsThrownAndTheTransactionIsNotCommitted()
    {
        Transaction transaction = mock(Transaction.class);
        when(transactionSupport.begin()).thenReturn(transaction);
        when(ofBizDelegator.createValue(anyString(), anyMap())).thenThrow(new DataAccessException("any message"));

        try
        {
            createProject();
            fail("a DataAccessException was supposed to be thrown");
        }
        catch (DataAccessException e)
        {
            // do nothing, the exception is the one thrown by our mock
        }

        verify(transaction, never()).commit();
        verify(projectKeyStore).refresh();
    }

    private void createProject()
    {
        String anyName = "";
        String anyKey = "";
        String anyDescription = "";
        String anyLeadKey = "";
        String anyUrl = "";
        Long anyAssigneeType = 1L;
        Long anyAvatarId = 2L;
        projectManager.createProject(anyName, anyKey, anyDescription, anyLeadKey, anyUrl, anyAssigneeType, anyAvatarId);
    }

    private DefaultProjectManager newProjectManager(
            final TransactionSupport transactionSupport,
            final OfBizDelegator ofBizDelegator,
            final ProjectKeyStore projectKeyStore)
    {
        return new DefaultProjectManager(
                ofBizDelegator,
                mock(NodeAssociationStore.class),
                mock(ProjectFactory.class),
                mock(ProjectRoleManager.class),
                mock(IssueManager.class),
                mock(AvatarManager.class),
                mock(UserManager.class),
                mock(ProjectCategoryStore.class),
                mock(ApplicationProperties.class),
                projectKeyStore,
                transactionSupport,
                mock(PropertiesManager.class),
                mock(JsonEntityPropertyManager.class),
                mock(EventPublisher.class)
        );
    }
}
