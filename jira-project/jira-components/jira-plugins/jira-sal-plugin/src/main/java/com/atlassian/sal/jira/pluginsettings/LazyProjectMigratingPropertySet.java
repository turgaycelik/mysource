package com.atlassian.sal.jira.pluginsettings;

import com.opensymphony.module.propertyset.PropertySet;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.Project;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Lazily migrates the old SAL settings by keys from project into normal property sets.  May be decommissioned at some
 * point in future, no sooner than SAL 3.
 */
public class LazyProjectMigratingPropertySet
{

    public static PropertySet create(ProjectManager projectManager, JiraPropertySetFactory jiraPropertySetFactory,
        PropertySet propertySet, String propertyKey)
    {
        return (PropertySet) Proxy.newProxyInstance(PropertySet.class.getClassLoader(),
            new Class<?>[]{PropertySet.class}, new PropertySetInvocationHandler(projectManager, jiraPropertySetFactory,
                propertySet, propertyKey));
    }

    static class PropertySetInvocationHandler implements InvocationHandler
    {
        private final ProjectManager projectManager;
        private final JiraPropertySetFactory jiraPropertySetFactory;

        private final PropertySet target;
        private final String projectKey;

        // Whether lazy initialisation of the project property set has occured
        private boolean projectPropertySetInitialised;
        // The property set is lazily initialised, only if we get a miss.
        private PropertySet projectPropertySet;

        private PropertySetInvocationHandler(ProjectManager projectManager, JiraPropertySetFactory jiraPropertySetFactory,
            PropertySet target, String projectKey)
        {
            this.projectManager = projectManager;
            this.jiraPropertySetFactory = jiraPropertySetFactory;
            this.target = target;
            this.projectKey = projectKey;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().equals("exists"))
            {
                return exists((String) args[0]);
            }
            else
            {
                return method.invoke(target, args);
            }
        }

        /**
         * JIRA plugin settings always checks if the key exists first, so we only do the migration here.
         */
        private boolean exists(String key)
        {
            if (target.exists(key))
            {
                return true;
            }
            synchronized (this)
            {
                if (!projectPropertySetInitialised)
                {
                    projectPropertySetInitialised = true;
                    // Look up the project id
                    Project project = projectManager.getProjectObjByKey(projectKey);
                    if (project != null)
                    {
                        // Lazily initialise the property set that we are falling back to
                        projectPropertySet = jiraPropertySetFactory.buildCachingPropertySet("Project", project.getId());
                    }
                }
                if (projectPropertySet != null)
                {
                    if (projectPropertySet.exists(key))
                    {
                        // Check that its a string or text, the old one only supported string and text so thats all we
                        // need to worry about, and then migrate it across, returning true.
                        switch (projectPropertySet.getType(key))
                        {
                        case PropertySet.STRING:
                            target.setString(key, projectPropertySet.getString(key));
                            projectPropertySet.remove(key);
                            return true;
                        case PropertySet.TEXT:
                            target.setText(key, projectPropertySet.getText(key));
                            projectPropertySet.remove(key);
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

}
