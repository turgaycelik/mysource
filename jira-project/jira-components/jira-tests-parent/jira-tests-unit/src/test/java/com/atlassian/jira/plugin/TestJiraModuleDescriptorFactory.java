package com.atlassian.jira.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Ensure that every Descriptor in JIRA has
 *
 * @since v4.0
 */
public class TestJiraModuleDescriptorFactory
{

    @Test
    public void testModulesAccountedFor() throws Exception
    {
        final TestModuleDescriptorFactory moduleDescriptorFactory = new TestModuleDescriptorFactory();
        final Collection<Class<AbstractModuleDescriptor>> descriptors = getAllBy(AbstractModuleDescriptor.class, "com.atlassian.jira.plugin");
        for (Class<AbstractModuleDescriptor> cls : descriptors)
        {
            assertTrue(cls.getName() + " is not registered with the JiraModuleDescriptorFactory", moduleDescriptorFactory.isClassRegistered(cls));
        }
    }

    private class TestModuleDescriptorFactory extends JiraModuleDescriptorFactory
    {
        public TestModuleDescriptorFactory()
        {
            super(null);
        }

        public boolean isClassRegistered(Class<AbstractModuleDescriptor> cls)
        {
            return getDescriptorClassesMap().containsValue(cls);
        }
    }

    private static <T> Collection<Class<T>> getAllBy(final Class<T> searchClass, final String searchPackage)
            throws Exception
    {
        final Collection<Class<T>> allModuleDescriptors = new ArrayList<Class<T>>();
        for (Class clazz : getClassesinPackage(searchPackage))
        {
            // Check if this class is a public concrete instance of TestCase:
            if (searchClass.isAssignableFrom(clazz)
                    && !Modifier.isAbstract(clazz.getModifiers())
                    && Modifier.isPublic(clazz.getModifiers())
                    )
            {
                allModuleDescriptors.add(clazz);
            }
        }
        return allModuleDescriptors;
    }

    /**
     * list Classes inside a given package
     *
     * @param packageName String name of a Package, EG "java.lang"
     * @return Class[] classes inside the root of the given package
     * @throws ClassNotFoundException if the Package is invalid
     */
    private static List<Class> getClassesinPackage(String packageName) throws ClassNotFoundException
    {
        ArrayList<Class> classes = new ArrayList<Class>();
        // Get a File object for the package
        File directory = null;
        try
        {
            String folderName = packageName.replace('.', '/');
            final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(folderName);
            while (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                directory = new File(url.getFile());
                if (directory.exists())
                {
                    getClassesRecursively(packageName, directory, classes);
                }
            }
            return classes;
        }
        catch (NullPointerException x)
        {
            throw x;
            //throw new ClassNotFoundException(pckgname+" does not appear to be a valid package");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void getClassesRecursively(String packageName, File directory, List<Class> classList)
            throws ClassNotFoundException
    {
        // Find the contents of this directory:
        File[] childFiles = directory.listFiles();
        for (File childFile : childFiles)
        {
            if (childFile.isFile())
            {
                // we are only interested in .class files
                String fileName = childFile.getName();
                if (fileName.endsWith(".class"))
                {
                    // remove the .class extension
                    String className = fileName.substring(0, fileName.length() - 6);
                    try
                    {
                        classList.add(Class.forName(packageName + '.' + className));
                    }
                    catch (Throwable ignored)
                    {}
                }
            }
            else if (childFile.isDirectory())
            {
                getClassesRecursively(packageName + '.' + childFile.getName(), childFile, classList);
            }
        }
    }

}
