package com.atlassian.sal.jira.project;

import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.easymock.EasyMockAnnotations.replayMocks;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.verify;

public class TestJiraProjectManager extends TestCase
{
    @Mock
    ProjectManager mockProjectManager;

    @Mock
    ProjectFactory mockProjectFactory;

    JiraProjectManager jiraProjectManager;

    public void setUp()
    {
        EasyMockAnnotations.initMocks(this);
        jiraProjectManager = new JiraProjectManager(mockProjectManager, mockProjectFactory);
    }

    public void testGetAllProjectKeys()
    {
        Collection projectGVs = new ArrayList();
        // What to do .....
        class MockProject extends ProjectImpl
        {
            private String key;

            public MockProject(String key)
            {
                super(null);
                this.key = key;
            }

            public String getKey()
            {
                return key;
            }

        }
        // .... that's simple enough
        Project p1 = new MockProject("p1");
        Project p2 = new MockProject("p2");
        List<Project> projects = Arrays.asList(p1, p2);

        expect(mockProjectManager.getProjects()).andReturn(projectGVs);
        expect(mockProjectFactory.getProjects(projectGVs)).andReturn(projects);
        replayMocks(this);
        
        Collection<String> keys = jiraProjectManager.getAllProjectKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("p1"));
        assertTrue(keys.contains("p2"));
        verify(mockProjectFactory, mockProjectManager);
    }

}
