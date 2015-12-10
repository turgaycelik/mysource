/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.AbstractProjectManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.ofbiz.core.entity.GenericValue;

public class MockProjectManager extends AbstractProjectManager
{
    Map<Long, GenericValue> components;
    Map<Long, Project> projects;
    private OfBizDelegator delegator;
    private final AtomicInteger nextId = new AtomicInteger();

    public MockProjectManager()
    {
        super(null, null);
        components = new HashMap<Long, GenericValue>();
        projects = new HashMap<Long, Project>();
    }

//    public void addComponent(GenericValue component)
//    {
//        components.put(component.getLong("id"), component);
//    }

    public void addProject(Project project)
    {
        projects.put(project.getId(), project);
    }

    public void addProject(GenericValue gv)
    {
        final Long id = gv.getLong("id");
        projects.put(id, new MockProject(id, gv.getString("key"), gv.getString("name"), gv));
    }

    @Override
    public Project createProject(final String name, final String key, final String description, final String lead,
            final String url, final Long assigneeType, final Long avatarId)
    {
        MockProject project = new MockProject(
                nextId.addAndGet(1),
                key,
                name
        );

        project.setAssigneeType(assigneeType);

        return project;
    }

    @Override
    public Project updateProject(final Project updatedProject, final String name, final String description,
            final String lead, final String url, final Long assigneeType, final Long avatarId, final String projectKey)
    {
        MockProject newProject = new MockProject(updatedProject.getId(), updatedProject.getKey(), name);
        newProject.setDescription(description);
        newProject.setLead(new MockUser(lead));
        newProject.setUrl(url);
        newProject.setAssigneeType(assigneeType);
        // ignore Avatar
//        newProject.setAvatar(avatarId);
        // ignore Project Key

        projects.put(newProject.getId(), newProject);
        return newProject;
    }

    @Override
    public void removeProjectIssues(final Project project) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProject(final Project project)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue getProject(Long id)
    {
        Project project = projects.get(id);
        if (project == null)
        {
            return null;
        }
        return project.getGenericValue();
    }

    /**
     * Returns project object or null if project with that id doesn't exist.
     *
     * @param id project id
     * @return project object or null if project with that id doesn't exist
     */
    @Override
    public Project getProjectObj(Long id)
    {
        return projects.get(id);
    }

    @Override
    public GenericValue getProjectByName(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue getProjectByKey(String key)
    {
        for (GenericValue project : getProjects())
        {
            if (key.equals(project.getString("key")))
            {
                return project;
            }
        }
        return null;
    }

    @Override
    public Project getProjectObjByKey(String projectKey)
    {
        for (Project project : getProjectObjects())
        {
            if (projectKey.equals(project.getKey()))
            {
                return project;
            }
        }
        return null;
    }

    @Override
    public Project getProjectByCurrentKeyIgnoreCase(String projectKey)
    {
        for (GenericValue project : getProjects())
        {
            if (projectKey.equalsIgnoreCase(project.getString("key")))
            {
                return new ProjectImpl(project);
            }
        }
        return null;
    }

    @Override
    public Project getProjectObjByKeyIgnoreCase(final String projectKey)
    {
        return getProjectByCurrentKeyIgnoreCase(projectKey);
    }

    @Override
    public Set<String> getAllProjectKeys(Long projectId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Project getProjectObjByName(String projectName)
    {
        for (GenericValue project : getProjects())
        {
            if (projectName.equals(project.getString("name")))
            {
                return new ProjectImpl(project);
            }
        }
        return null;
    }

    @Override
    public Collection<GenericValue> getProjects()
    {
        Collection projectGvs = new ArrayList(projects.values());
        CollectionUtils.transform(projectGvs, new Transformer()
        {
            @Override
            public Object transform(Object object)
            {
                return ((Project) object).getGenericValue();
            }
        });
        return projectGvs;
    }

    @Override
    public List<Project> getProjectObjects()
    {
        return new ArrayList(projects.values());
    }

    @Override
    public long getProjectCount() throws DataAccessException
    {
        return projects.size();
    }

    @Override
    public GenericValue getComponent(Long id)
    {
        return components.get(id);
    }

    @Override
    public GenericValue getComponent(GenericValue project, String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<GenericValue> getComponents(GenericValue project)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<GenericValue> getProjectCategories()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ProjectCategory> getAllProjectCategories()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue getProjectCategory(Long id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProjectCategory getProjectCategoryObject(Long id) throws DataAccessException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateProjectCategory(GenericValue projectCat)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateProjectCategory(ProjectCategory projectCategory) throws DataAccessException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<GenericValue> getProjectsFromProjectCategory(GenericValue projectCategory)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Project> getProjectsFromProjectCategory(ProjectCategory projectCategory)
            throws DataAccessException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Project> getProjectObjectsFromProjectCategory(final Long projectCategoryId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<GenericValue> getProjectsWithNoCategory()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Project> getProjectObjectsWithNoCategory()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue getProjectCategoryFromProject(GenericValue project)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProjectCategory getProjectCategoryForProject(Project project)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProjectCategory(GenericValue project, GenericValue category)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProjectCategory(Project project, ProjectCategory category)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProjectCategory createProjectCategory(String name, String description)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProjectCategory(Long id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Project> getProjectsLeadBy(User leadUser)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Project> getProjectsLeadBy(ApplicationUser leadUser)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<GenericValue> getProjectsByLead(User leadUser)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getNextId(Project project) throws DataAccessException
    {
        // TODO: Implement me
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public long getCurrentCounterForProject(Long id)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentCounterForProject(Project project, long counter)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh()
    {
        throw new UnsupportedOperationException();
    }

    private OfBizDelegator getDelegator()
    {
        if (delegator == null)
        {
            delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        }
        return delegator;
    }

    /**
     * Creates a "default" MockProjectManager. <p> This will include the following projects: <table>
     * <tr><td>ID</td><td>Key</td> <td>Name</td></tr> <tr><td>1</td> <td>HSP</td> <td>Homosapien</td></tr>
     * <tr><td>2</td> <td>MNK</td> <td>Monkey</td></tr> <tr><td>3</td> <td>RAT</td> <td>Rattus</td></tr> <tr><td>4</td>
     * <td>COW</td> <td>Bovine</td></tr> <tr><td>5</td> <td>DOG</td> <td>Canine</td></tr> <tr><td>6</td> <td>PIG</td>
     * <td>Porcine</td></tr> </table> </p>
     *
     * @return a "default" MockProjectManager.
     */
    public static MockProjectManager createDefaultProjectManager()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(1, "HSP", "Homosapien"));
        mockProjectManager.addProject(new MockProject(2, "MNK", "Monkey"));
        mockProjectManager.addProject(new MockProject(3, "RAT", "Rattus"));
        mockProjectManager.addProject(new MockProject(4, "COW", "Bovine"));
        mockProjectManager.addProject(new MockProject(5, "DOG", "Canine"));
        mockProjectManager.addProject(new MockProject(6, "PIG", "Porcine"));

        return mockProjectManager;
    }
}
