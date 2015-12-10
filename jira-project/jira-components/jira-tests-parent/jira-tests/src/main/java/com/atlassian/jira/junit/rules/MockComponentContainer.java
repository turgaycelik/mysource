package com.atlassian.jira.junit.rules;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.stateNotNull;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;
import static java.lang.String.format;

/**
 * JUnit {@code @Rule} that allows for providing mock JIRA components accessed in
 * production code via {@link com.atlassian.jira.component.ComponentAccessor}
 * static methods.
 *
 * <p/>
 * Example usage (in your test classes):
 *
 * <ul>
 *     <li>using annotations:</li>
 * <code><pre>
 *     &#64;Rule public MockComponentContainer container = new MockComponentContainer(this);
 *
 *     &#64;Mock
 *     &#64;AvailableInContainer
 *     private ServiceOne mockServiceOne;
 *
 *     &#64;Mock
 *     &#64;AvailableInContainer(interfaceClass=ServiceTwo.class) // useful if mockServiceTwo would implement more interfaces
 *     private ServiceTwo mockServiceTwo;
 *
 *     &#64;Test
 *     public void testCodeThatUsesComponentAccessor()
 *     {
 *         assertSame(mockServiceOne, ComponentAccessor.getComponent(ServiceOne.class));
 *         assertSame(mockServiceTwo, ComponentAccessor.getComponent(ServiceTwo.class));
 *         // etc.
 *     }
 * </pre></code>
 *
 * <li>or adding to the container manually:</li>
 * <code><pre>
 *     &#64;Rule public MockComponentContainer container = new MockComponentContainer(this);
 *
 *     &#64;Before
 *     public void addMocks()
 *     {
 *         container.addMock(ServiceOne.class, mockServiceOne)
 *                  .addMock(ServiceTwo.class, mockServiceTwo);
 *     }
 *
 *     &#64;Test
 *     public void testCodeThatUsesComponentAccessor()
 *     {
 *         assertSame(mockServiceOne, ComponentAccessor.getComponent(ServiceOne.class));
 *         // etc.
 *     }
 * </pre></code>
 *
 * </ul>
 *
 * @since 5.1
 */
public class MockComponentContainer extends TestWatcher
{
    private final MockComponentWorker mockWorker = new MockComponentWorker();

    private final Object test;

    public MockComponentContainer(Object test)
    {
        this.test = test;
    }

    public <I, C extends I> MockComponentContainer addMockComponent(Class<I> componentInterface, C mockComponentImplementation)
    {
        mockWorker.addMock(componentInterface, mockComponentImplementation);
        return this;
    }

    public <I, C extends I> MockComponentContainer addMock(Class<I> componentInterface, C mockComponentImplementation)
    {
        return addMockComponent(componentInterface, mockComponentImplementation);
    }

    @Override
    protected void starting(Description description)
    {
        addAnnotatedMocks();
        ComponentAccessor.initialiseWorker(mockWorker);
    }

    @Override
    protected void finished(Description description)
    {
        ComponentAccessor.initialiseWorker(null); // reset
    }

    private void addAnnotatedMocks()
    {
        try
        {
            for (Field field : getAllDeclaredFields(test.getClass()))
            {
                field.setAccessible(true);
                if (field.isAnnotationPresent(AvailableInContainer.class))
                {
                    final AvailableInContainer annotation = field.getAnnotation(AvailableInContainer.class);
                    final Object mock;
                    if(annotation.instantiateMe()){
                        mock = instantiateAndSet(field);
                    } else {
                        mock = field.get(test);
                    }
                    stateNotNull(String.format("%s is annotated with @AvailableInContainer but this field is null. Please instantiate implementation.", field.getName()), mock);
                    addToContainer(mock, annotation, field.getName());
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("Exception while processing @AvailableInContainer test fields", e);
        }

    }

    private Object instantiateAndSet(final Field field)
    {
        try
        {
            Constructor<?> constructor = field.getType().getConstructor();
            Object mock = constructor.newInstance();
            field.set(test, mock);
            return mock;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot instantiate field", e);
        }
    }

    private void addToContainer(Object mock, AvailableInContainer annotation, String fieldName)
    {
        Class<?> componentKey;
        if (annotation.interfaceClass().equals(Object.class))
        {
            // figure out the interface from the mock
            Iterable<Class<?>> interfaceClass = getInterfacesExcludingCrap(mock);
            if (Iterables.size(interfaceClass) == 0)
            {
                componentKey = mock.getClass(); // just use the concrete class as key
            }
            else if (Iterables.size(interfaceClass) != 1)
            {
                throw new IllegalStateException(format("%s is annotated with @AvailableInContainer without an "
                        + "interface key specified, but class %s does not implement exactly one interface. You must "
                        + "specify an interface key for this mock to avoid ambiguity", fieldName, mock.getClass()));
            }
            else
            {
                componentKey = Iterables.get(interfaceClass, 0);
            }
        }
        else
        {
            componentKey = annotation.interfaceClass();
        }
        stateTrue(format("Error while adding mock stored in field %s to the container. "
                + "Class %s does not inherit from interface %s. Please fix the interfaceClass field on the "
                + "@AvailableInContainer annotation of that field", fieldName, mock.getClass(), componentKey), componentKey.isInstance(mock));
        addMock((Class)componentKey, mock); // yes compiler, I know what I'm doing :P
    }

    /**
     * Mockito mocks are awesome, except implemeting some crappy marker interface :P
     *
     * @param mock a mock
     * @return a list of interfaces that this would actually implement, was it not (maybe) a Mockito mock
     */
    private Iterable<Class<?>> getInterfacesExcludingCrap(Object mock)
    {
        final Iterable<Class<?>> result = ImmutableList.copyOf(mock.getClass().getInterfaces());
        return Iterables.filter(result, new Predicate<Class<?>>()
        {
            @Override
            public boolean apply(Class<?> input)
            {
                return !input.getPackage().getName().startsWith("org.mockito");
            }
        });
    }

    /**
     * {@link Class#getDeclaredFields()} does not walk the class hierarchy.
     *
     * @param clazz the child class
     * @return all declared fields of the child class and its parent classes
     */
    private static List<Field> getAllDeclaredFields(Class clazz)
    {
        List<Field> result = new ArrayList<Field>();
        if (clazz != null)
        {
            result.addAll(getAllDeclaredFields(clazz.getSuperclass()));
            result.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return result;
    }

    public MockComponentWorker getMockWorker()
    {
        return mockWorker;
    }
}
