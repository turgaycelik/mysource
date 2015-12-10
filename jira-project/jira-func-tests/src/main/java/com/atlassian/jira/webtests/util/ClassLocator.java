package com.atlassian.jira.webtests.util;

import com.atlassian.jira.util.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.hamcrest.StringDescription;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that can be used to find other classes in a particular package.
 *
 * @since v4.0
 */
public final class ClassLocator<T>
{
    @SuppressWarnings("unchecked")
    public static ClassLocator<Object> forAnyClass()
    {
        return new ClassLocator(Object.class);
    }

    private final Class<T> klazz;

    private int level = Integer.MAX_VALUE;
    private boolean allowInner = true;

    private Predicate<Class<? extends T>> predicate = null;
    private FileFilter filenameFilter = null;
    private String pkg = null;



    public ClassLocator(Class<T> klazz)
    {
        this.klazz = klazz;
    }

    public int getLevel()
    {
        return level;
    }

    public ClassLocator<T> setLevel(final int level)
    {
        this.level = level;
        return this;
    }

    public boolean isAllowInner()
    {
        return allowInner;
    }

    public ClassLocator<T> setAllowInner(final boolean allowInner)
    {
        this.allowInner = allowInner;
        return this;
    }

    public Predicate<Class<? extends T>> getPredicate()
    {
        return predicate;
    }

    public ClassLocator<T> setPredicate(final Predicate<Class<? extends T>> predicate)
    {
        this.predicate = predicate;
        return this;
    }

    public FileFilter getFilenameFilter()
    {
        return filenameFilter;
    }

    public ClassLocator<T> setFilenameFilter(final FileFilter filenameFilter)
    {
        this.filenameFilter = filenameFilter;
        return this;
    }

    public ClassLocator<T> setPackage(String pkg)
    {
        this.pkg = pkg;
        return this;
    }

    public String getPackage()
    {
        return pkg;
    }

    /**
     * List all the classes inside a package
     *
     * @return the list of classes in the package.
     */
    public List<Class<? extends T>> findClasses()
    {
        if (pkg == null)
        {
            throw new IllegalStateException("Package must be specified.");
        }

        // Get a File object for the package
        File directory;
        final String folderName = pkg.replace('.', '/');
        final URL url = Thread.currentThread().getContextClassLoader().getResource(folderName);
        if (url == null)
        {
            throw new IllegalStateException(new StringDescription().appendText("Could not find package ")
                    .appendValue(pkg).appendText(" on the class path").toString());
        }
        try
        {
            directory = new File(url.toURI());
        }
        catch (URISyntaxException e)
        {
            directory = new File(url.getFile());
        }

        if (directory.exists() && directory.isDirectory())
        {
            final ArrayList<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
            final ClassPredicate<T> classPredicate = new ClassPredicate<T>(predicate, klazz, allowInner);
            final ClassFileFilter classFileFilter = new ClassFileFilter(allowInner, filenameFilter);

            getClassesRecursively(pkg, directory, classes, classPredicate, classFileFilter, level);
            return classes;
        }
        else
        {
            throw new RuntimeException(pkg + " does not appear to be a valid package");
        }
    }

    private static <T>void getClassesRecursively(String packageName, File directory, List<Class<? extends T>> classList,
            ClassPredicate<T> predicate, FileFilter filter, int currentLevel)
    {
        // Find the contents of this directory:
        final File[] childFiles = directory.listFiles(filter);
        for (File childFile : childFiles)
        {
            if (childFile.isFile())
            {
                final String fileName = childFile.getName();
                // remove the .class extension. The FileFilter has made sure we have this extension.
                final String className = FilenameUtils.getBaseName(fileName);                
                try
                {
                    final Class<?> clazz = Class.forName(packageName + '.' + className);
                    Class<? extends T> subClass = predicate.evaluate(clazz);
                    if (subClass != null)
                    {
                        classList.add(subClass);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("Unexpected class not found exception for: " + className, e);
                }
            }
            else if (childFile.isDirectory() && currentLevel > 0)
            {
                getClassesRecursively(packageName + '.' + childFile.getName(), childFile, classList, predicate, filter, currentLevel - 1);
            }
        }
    }

    private static class ClassPredicate<T>
    {
        private final Predicate<Class<? extends T>> delegate;
        private final Class<T> klazz;
        private final boolean inner;

        public ClassPredicate(final Predicate<Class<? extends T>> delegate, final Class<T> klazz, final boolean inner)
        {
            this.delegate = delegate;
            this.klazz = klazz;
            this.inner = inner;
        }

        public Class<? extends T> evaluate(final Class<?> input)
        {
            if (klazz.isAssignableFrom(input) && (inner || input.getEnclosingClass() == null))
            {
                //We know this is safe from the above check.
                @SuppressWarnings ({ "unchecked" }) Class<? extends T> subKlazz = (Class<? extends T>) input;
                if (delegate == null || delegate.evaluate(subKlazz))
                {
                    return subKlazz;
                }
            }
            return null;
        }
    }

    private static class ClassFileFilter implements FileFilter
    {
        private static final String EXTENSION_CLASS = "class";

        private final boolean inner;
        private final FileFilter delegate;

        private ClassFileFilter(final boolean inner, final FileFilter delegate)
        {
            this.inner = inner;
            this.delegate = delegate;
        }

        public boolean accept(final File pathname)
        {
            if (!pathname.exists() || !pathname.canRead())
            {
                return false;
            }

            if (pathname.isFile())
            {
                if (!isValidClassFile(pathname))
                {
                    return false;
                }
            }

            return delegate == null || delegate.accept(pathname);
        }

        private boolean isValidClassFile(File file)
        {
            final String name = file.getName();
            final String extension = FilenameUtils.getExtension(name);
            return EXTENSION_CLASS.equalsIgnoreCase(extension) && (inner || name.indexOf('$') < 0);
        }
    }
}
