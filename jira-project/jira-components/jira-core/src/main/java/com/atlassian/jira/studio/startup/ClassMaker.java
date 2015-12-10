package com.atlassian.jira.studio.startup;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;

/**
 * Simple class creator.
 *
 * @param <T> The type of object this class returns. All objects created by this class must be a subclass of this type.
 * @since v4.4.1
 */
class ClassMaker<T>
{
    private static final Logger log = Logger.getLogger(ClassMaker.class);

    private final Class<T> instanceType;
    private final ClassLoader loader;

    /**
     * @param superClass the type of object this class returns.
     * @param loader the class loader used to locate class objects.
     */
    ClassMaker(Class<T> superClass, ClassLoader loader)
    {
        this.instanceType = superClass;
        this.loader = loader;
    }

    /**
     * Tries to create an instance of the passed class name. A null value will be returned on error. A null value will also
     * be returned if the passed class is not a subclass of {@link T}.
     *
     * @param name the name of the class to try and create.
     * @return a new instance of the passed name. Will return null if any errors occurs while trying to load the class.
     */
    T createInstance(String name)
    {
        try
        {
            Class<?> loadedClass = loader.loadClass(name);
            if (instanceType.isAssignableFrom(loadedClass))
            {
                //Safe because of the above check.
                @SuppressWarnings ( { "unchecked" })
                Class<? extends T> c = (Class<? extends T>) loadedClass;

                Constructor<? extends T> constructor = c.getConstructor();
                return constructor.newInstance();
            }
            else
            {
                log.info(format("Unable to create an instance of '%s' as it is not a subclass of '%s'.", loadedClass.getName(), instanceType.getName()));
            }
        }
        catch (ClassNotFoundException e)
        {
            log.info(format("Unable to find class '%s'.", name), e);
        }
        catch (NoSuchMethodException e)
        {
            log.info(format("Unable to find no-arg constructor for class '%s'.", name), e);
        }
        catch (InvocationTargetException e)
        {
            log.info(format("Exception thrown while creating '%s'.", name), e);
        }
        catch (InstantiationException e)
        {
            log.info(format("Exception thrown while creating '%s'.", name), e);
        }
        catch (IllegalAccessException e)
        {
            log.info(format("Exception thrown while creating '%s'.", name), e);
        }
        return null;
    }
}
