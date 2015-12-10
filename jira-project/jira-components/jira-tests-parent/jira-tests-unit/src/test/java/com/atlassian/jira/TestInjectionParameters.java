package com.atlassian.jira;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.event.internal.EventExecutorFactoryImpl;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizApplicationDao;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizDirectoryDao;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizGroupDao;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizInternalMembershipDao;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizUserDao;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.junit.Test;
import org.mockito.Mockito;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.quartz.impl.StdScheduler;

import junit.framework.Assert;
import webwork.action.ActionSupport;

/**
 * A test that makes sure that classes that we inject do not have concrete classes in their constructors
 */
public class TestInjectionParameters
{
    private static final String CLASS_EXT = ".class";
    private static final Set<Class> EXCLUSIONS = new HashSet<Class>();

    static
    {
        EXCLUSIONS.add(OfBizApplicationDao.class);
        EXCLUSIONS.add(EventExecutorFactoryImpl.class);
        EXCLUSIONS.add(ExternalLinkUtilImpl.class);
        EXCLUSIONS.add(DefaultPluginEventManager.class);
        // StdScheduler comes from Quartz Scheduler library.
        EXCLUSIONS.add(StdScheduler.class);
        // Ignore these temporarily until we update Crowd Embedded
        EXCLUSIONS.add(OfBizDirectoryDao.class);
        EXCLUSIONS.add(OfBizUserDao.class);
        EXCLUSIONS.add(OfBizGroupDao.class);
        EXCLUSIONS.add(OfBizInternalMembershipDao.class);
    }

    @Test
    public void testConstructorUsage() throws ClassNotFoundException
    {

        final ContainerRegistrar registrar = new ContainerRegistrar();
        final MyComponentContainer container = new MyComponentContainer();

        registrar.registerComponents(container, true);
        //add all webwork classes since they get injected with components
        final ImmutableSet.Builder<Class<?>> classesToCheck = getClassesForPackage("com.atlassian.jira.web", new Predicate<Class<?>>()
        {
            @Override
            public boolean evaluate(final Class<?> aClass)
            {
                return ActionSupport.class.isAssignableFrom(aClass) && !aClass.getName().contains("Test");
            }
        });
        //add all implementation classes from pico.  They could get dodgy stuff injected!
        classesToCheck.addAll(container.getImplementationsToCheck());

        final Set<Class> offenders = new HashSet<Class>();
        for (final Class classToCheck : classesToCheck.build())
        {
            if (EXCLUSIONS.contains(classToCheck))
            {
                continue;
            }
            final Constructor[] constructors = classToCheck.getConstructors();
            for (final Constructor constructor : constructors)
            {
                final Class[] parameterTypes = constructor.getParameterTypes();
                for (final Class parameterType : parameterTypes)
                {
                    if (isOffender(parameterType, container))
                    {
                        offenders.add(classToCheck);
                        break;
                    }
                }
            }
        }
        if (!offenders.isEmpty())
        {
            final StringBuilder out = new StringBuilder();
            out.append("Found ").append(offenders.size()).append(" classes that are injectable and take concrete classes.\n").
                    append("They may break if logging & profiling is enabled. Change these classes to have interfaces injected or add them to the exclusions:\n");
            for (final Class offender : offenders)
            {
                out.append("* ").append(offender.getCanonicalName()).append("\n");
            }
            Assert.fail(out.toString());
        }
    }

    private boolean isOffender(final Class parameter, final MyComponentContainer container)
    {
        if (parameter.isPrimitive())
        {
            return false;
        }
        if (parameter.isInterface())
        {
            return false;
        }

        //I suppose concrete parameters are allowed if there's no interface for them.
        if (parameter.getInterfaces().length == 0)
        {
            return false;
        }

        if (parameter.equals(StepDescriptor.class) || parameter.equals(ActionDescriptor.class))
        {
            return false;
        }

        final Set<Class<?>> nonProfiledClasses = container.getNonProfiledClasses();
        return !nonProfiledClasses.contains(parameter);
    }

    private static ImmutableSet.Builder<Class<?>> getClassesForPackage(final String pckgname, final Predicate<Class<?>> pred)
            throws ClassNotFoundException
    {
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        final List<File> directories = new ArrayList<File>();
        try
        {
            final ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null)
            {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            final String path = pckgname.replace('.', '/');
            // Ask for all resources for the path
            final Enumeration<URL> resources = cld.getResources(path);
            while (resources.hasMoreElements())
            {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8")));
            }
        }
        catch (final NullPointerException x)
        {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        }
        catch (final UnsupportedEncodingException encex)
        {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        }
        catch (final IOException ioex)
        {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }

        final ImmutableSet.Builder<Class<?>> classes = ImmutableSet.builder();
        // For every directory identified capture all the .class files
        for (final File directory : directories)
        {
            scanDirectories(pckgname, classes, directory, pred);
        }
        return classes;
    }

    private static void scanDirectories(final String pckgname, final ImmutableSet.Builder<Class<?>> classes, final File directory, final Predicate<Class<?>> pred)
            throws ClassNotFoundException
    {
        // Get the list of the files contained in the package
        final File[] files = directory.listFiles();
        if (files != null)
        {
            for (final File file : files)
            {
                if (file.isDirectory())
                {
                    scanDirectories(pckgname + "." + file.getName(), classes, file, pred);
                }
                // we are only interested in .class files
                final String fileName = file.getName();
                if (fileName.endsWith(CLASS_EXT))
                {
                    // removes the .class extension
                    try
                    {
                        final Class<?> aClass = Class.forName(pckgname + '.' + fileName.substring(0, fileName.length() - CLASS_EXT.length()));
                        if (pred.evaluate(aClass))
                        {
                            classes.add(aClass);
                        }
                    }
                    catch (final NoClassDefFoundError e)
                    {
                        // do nothing. this class hasn't been found by the loader, and we don't care.
                    }
                    catch (final ExceptionInInitializerError e)
                    {
                        //also do nothing.
                    }
                }
            }
        }
    }

    static class MyComponentContainer extends ComponentContainer
    {
        // Classes that are not registered against and interface will *not* be profiled.  They should
        // be excluded when checking for illegal parameters
        private final Set<Class<?>> nonProfiledClasses = Sets.newHashSet();
        // The actual classes that are components need to be checked for illegal parameters.
        private final Set<Class<?>> implementationsToCheck = Sets.newHashSet();

        @Override
        void instance(final Scope scope, final Object instance)
        {
            nonProfiledClasses.add(instance.getClass());
        }

        @Override
        void instance(final Scope scope, final String key, final Object instance)
        {
            nonProfiledClasses.add(instance.getClass());
        }

        @Override
        <T, S extends T> void instance(final Scope scope, final Class<T> key, final S instance)
        {
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(instance.getClass());
            }
        }

        @Override
        <T> T getComponentInstance(final Class<T> key)
        {
            return Mockito.mock(key);
        }

        @Override
        void implementation(final Scope scope, final Class<?> implementation)
        {
            nonProfiledClasses.add(implementation);
        }

        @Override
        void component(final Scope scope, final ComponentAdapter componentAdapter)
        {

        }

        @Override
        void componentWithoutDefaultBehaviour(final Scope scope, final ComponentAdapter componentAdapter)
        {

        }

        @Override
        void transfer(final ComponentManager from, final Scope scope, final Class<?> key)
        {

        }

        @Override
        ComponentAdapter getComponentAdapter(final Class<?> key)
        {
            return (ComponentAdapter) Mockito.mock(key, Mockito.withSettings().extraInterfaces(ComponentAdapter.class));
        }

        @Override
        <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation)
        {
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        @Override
        <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation, final Object... parameterKeys)
        {
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        @Override
        <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation, final Parameter[] parameters)
        {
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        @Override
        <T> void implementationUseDefaultConstructor(final Scope scope, final Class<T> key, final Class<? extends T> implementation)
        {
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        public Set<Class<?>> getNonProfiledClasses()
        {
            return nonProfiledClasses;
        }

        public Set<Class<?>> getImplementationsToCheck()
        {
            return implementationsToCheck;
        }
    }
}
