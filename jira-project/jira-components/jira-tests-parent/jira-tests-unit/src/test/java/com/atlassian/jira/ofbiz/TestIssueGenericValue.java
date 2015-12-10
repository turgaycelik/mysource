package com.atlassian.jira.ofbiz;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.entity.model.ModelFieldType;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssueGenericValue
{

    public static final long PROJECT_ID = 10000L;
    public static final String PROJECT_KEY = "TEST";
    @Mock
    private ComponentAccessor.Worker worker;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private Project project;
    @Mock
    private GenericDelegator delegator;
    @Mock
    private ModelFieldType modelFieldType;

    @Test (expected = IllegalArgumentException.class)
    public void testGetNullKey()
    {
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        IssueGenericValue sut = new IssueGenericValue(issue);

        sut.getString(null);
    }

    @Test
    public void testGetKey() throws Exception
    {
        when(worker.getComponent(ProjectManager.class)).thenReturn(projectManager);
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(project);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        ComponentAccessor.initialiseWorker(worker);
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        issue.set("project", PROJECT_ID);
        issue.set("number", 1L);
        IssueGenericValue sut = new IssueGenericValue(issue);

        assertThat(sut.getString("key"), equalTo("TEST-1"));
    }

    @Test
    public void testGetSomethingElseThanKey() throws Exception
    {
        ComponentAccessor.initialiseWorker(worker);
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        issue.set("number", 1L);
        IssueGenericValue sut = new IssueGenericValue(issue);

        assertThat(sut.getLong("number"), equalTo(1L));
    }

    @Test
    public void testGetKeyWithNullProjectId() throws Exception
    {
        when(worker.getComponent(ProjectManager.class)).thenReturn(projectManager);
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(project);
        ComponentAccessor.initialiseWorker(worker);
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        ModelField field = new ModelField();
        field.setName("project");
        issue.modelEntity.addField(field);
        issue.set("number", 1L);
        issue.set("id", 1L);
        IssueGenericValue sut = new IssueGenericValue(issue);

        assertThat(sut.getString("key"), nullValue());
    }

    @Test
    public void testGetKeyWithNullProject() throws Exception
    {
        when(worker.getComponent(ProjectManager.class)).thenReturn(projectManager);
        ComponentAccessor.initialiseWorker(worker);
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        issue.set("project", PROJECT_ID);
        issue.set("number", 1L);
        issue.set("id", 1L);
        IssueGenericValue sut = new IssueGenericValue(issue);

        assertThat(sut.getString("key"), nullValue());
    }

    @Test
    public void testGetKeyWithNullIssueNum() throws Exception
    {
        when(worker.getComponent(ProjectManager.class)).thenReturn(projectManager);
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(project);
        ComponentAccessor.initialiseWorker(worker);
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        issue.set("project", PROJECT_ID);
        issue.set("id", 1L);
        ModelField field = new ModelField();
        field.setName("number");
        issue.modelEntity.addField(field);
        ModelField key = new ModelField();
        key.setName("key");
        issue.modelEntity.addField(key);
        IssueGenericValue sut = new IssueGenericValue(issue);

        assertThat(sut.getString("key"), nullValue());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSetNullKey()
    {
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        IssueGenericValue sut = new IssueGenericValue(issue);

        sut.setString(null, null);
    }

    @Test
    public void testSetKey() throws Exception
    {
        when(delegator.getEntityFieldType(any(ModelEntity.class), anyString())).thenReturn(modelFieldType);
        when(modelFieldType.getJavaType()).thenReturn("String");
        MockGenericValue issue = new MockGenericValue("Issue", 10001L);
        issue.modelEntity.setEntityName("Issue");
        issue.setDelegator(delegator);
        ModelField field = new ModelField();
        field.setName("number");
        issue.modelEntity.addField(field);
        field = new ModelField();
        field.setName("key");
        issue.modelEntity.addField(field);
        IssueGenericValue sut = new IssueGenericValue(issue);
        sut.internalDelegator = delegator;

        sut.setString("key", "TEST-43");

        assertThat(sut.getLong("number"), equalTo(43L));
    }

    @Test
    public void testSetSomethingElse() throws Exception
    {
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        ModelField field = new ModelField();
        field.setName("number");
        issue.modelEntity.addField(field);
        IssueGenericValue sut = new IssueGenericValue(issue);
        sut.internalDelegator = delegator;

        sut.set("number", 43L);

        assertThat(sut.getLong("number"), equalTo(43L));
    }

    @Test (expected = NullPointerException.class)
    public void testSetNullValueForKey() throws Exception
    {
        when(delegator.getEntityFieldType(any(ModelEntity.class), anyString())).thenReturn(modelFieldType);
        when(modelFieldType.getJavaType()).thenReturn("String");
        MockGenericValue issue = new MockGenericValue("Issue");
        issue.modelEntity.setEntityName("Issue");
        issue.setDelegator(delegator);
        ModelField field = new ModelField();
        field.setName("number");
        issue.modelEntity.addField(field);
        field = new ModelField();
        field.setName("key");
        issue.modelEntity.addField(field);
        IssueGenericValue sut = new IssueGenericValue(issue);
        sut.internalDelegator = delegator;

        sut.setString("key", null);
    }
}