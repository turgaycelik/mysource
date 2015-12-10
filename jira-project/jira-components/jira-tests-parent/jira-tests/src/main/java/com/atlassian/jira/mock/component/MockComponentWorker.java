package com.atlassian.jira.mock.component;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.preferences.MockUserPreferencesManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * This component worker can be used with the {@link ComponentAccessor} to return mock
 * instances of components for unit testing.
 * <p/>
 * When a class needs to access another JIRA component, the preferred mechanism to resolve
 * this dependency is by injection in the constructor.  Among other things, this makes it
 * easier to see what the dependencies of the class are and also makes it easy for unit
 * tests to provide mocks for those components.  However, there are times when dependency
 * injection is impossible or impractical.  Examples include:
 * <ul>
 * <li>Components with circular dependencies between them, as one of them must be resolved
 *      first without the other dependency available for injection yet</li>
 * <li>Classes that are not injectable components but are instead explicitly constructed
 *      in a long chain of classes that would not otherwise need the target component</li>
 * <li>Static-only utility classes, which are never constructed at all</li>
 * </ul>
 * <p/>
 * In these cases, the class can use {@link ComponentAccessor#getComponentOfType(Class)}
 * to resolve the dependency, instead.  The drawback is that {@link ComponentAccessor}
 * uses a global, static reference to a {@link ComponentAccessor.Worker} implementation
 * to accomplish this, and if nothing has initialised that reference, then an
 * {@code IllegalStateException} is thrown.
 * <p/>
 * Unit tests must be responsible for ensuring that everything they require, directly or
 * indirectly, is arranged during the test's setup, so a unit test that encounters this
 * problem should explicitly initialise the component accessor.  In most cases, all they
 * need to do is create and install an instance of this class.  For example:
 * <p/>
 * <code><pre>
 *     &#64;Before
 *     public void setUp()
 *     {
 *         new {@link #MockComponentWorker()}.{@link #init() init()};
 *     }
 * </pre></code>
 * <p/>
 * The {@code MockComponentWorker} comes with a few mocks by default, including
 * implementations for common problem areas, such as the {@code UserKeyService} and
 * the {@code UserPreferencesManager}, so for many tests this will be enough as-is.
 * If you need additional mocked components to be resolved in this way, then you can
 * add them as well.  An example might look something like this:
 * <p/>
 * <code><pre>
 *     &#64;Before
 *     public void setUp()
 *     {
 *         final ApplicationUser fred = new MockApplicationUser("Fred");
 *         final JiraAuthenticationContext jiraAuthenticationContext = Mockito.mock(JiraAuthenticationContext.class);
 *         Mockito.when(jiraAuthenticationContext.getUser()).thenReturn(fred);
 *         Mockito.when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(fred.getDirectoryUser());
 *         new {@link #MockComponentWorker()}
 *                 .{@link #addMock(Class,Object) addMock}(ConstantsManager.class, new MockStatusConstantsManager())
 *                 .{@link #addMock(Class,Object) addMock}(JiraAuthenticationContext.class, jiraAuthenticationContext)
 *                 .{@link #init()};
 *     }
 * </pre></code>
 * <p/>
 * JUnit 4 annotations can also be used to initialise the {@code MockComponentWorker}.
 * See the
 * {@link com.atlassian.jira.junit.rules.MockComponentContainer MockComponentContainer}
 * and
 * {@link com.atlassian.jira.junit.rules.MockitoContainer MockitoContainer}
 * {@code @Rule}s for examples.
 *
 * @since v4.4
 */
public class MockComponentWorker implements ComponentAccessor.Worker
{
    private static final Logger LOG = Logger.getLogger(MockComponentWorker.class);

    final Map<Class<?>, Object> implementations = newHashMap();
    final Map<Class<?>, LazyReference<?>> lazyImplementationReferences = newHashMap();
    private MockUserPreferencesManager mockUserPreferences;
    private MockApplicationProperties mockApplicationProperties;
    private MockUserKeyService mockUserKeyService;

    public MockComponentWorker()
    {
        initDefaultMocks();
    }

    private void initDefaultMocks()
    {
        // Add some default mocks
        mockUserPreferences = new MockUserPreferencesManager();
        mockApplicationProperties = new MockApplicationProperties();
        mockUserKeyService = new MockUserKeyService();

        // The keys should be defined in jira-api so that consumers don't have to depend on jira-core.
        registerMock(UserKeyService.class, mockUserKeyService);
        registerMock(UserPreferencesManager.class, mockUserPreferences);
        registerMock(ApplicationProperties.class, mockApplicationProperties);

        // Using implementation class introduces a dependency on jira-core.
        // Putting it into a lazy reference makes this dependency optional for plugins.
        addMock(JqlClauseBuilderFactory.class, new LazyReference<JqlClauseBuilderFactory>()
        {
            @Override
            protected JqlClauseBuilderFactory create()
            {
                return new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(null));
            }
        });
    }

    /**
     * Registers a mock component to be returned by this component worker.
     * <p/>
     * Since {@link #addMock(Class, Object)} is identical but also returns {@code this}
     * for call chaining, it may be more convenient to use.
     *
     * @param componentInterface the interface that the mock component must implement
     * @param componentMock the component that implements the interface
     * @param <T> the {@code componentInterface}
     * @param <U> the {@code componentMock}'s class, which must implement {@code <T>}
     * @see #addMock(Class, Object)
     */
    public <T, U extends T> void registerMock(Class<T> componentInterface, U componentMock)
    {
        implementations.put(componentInterface, componentMock);
    }

    /**
     * Registers a mock component to be returned by this component worker.
     * <p/>
     * This method is exactly equivalent to {@link #registerMock(Class, Object)}, except that
     * it also returns {@code this}, making it possible to use it in the convenient call chaining
     * style illustrated in the documentation for this class.
     *
     * @param componentInterface the interface that the mock component must implement
     * @param componentMock the component that implements the interface
     * @param <T> the {@code componentInterface}
     * @param <U> the {@code componentMock}'s class, which must implement {@code <T>}
     * @return {@code this}, for convenience
     */
    public <T, U extends T> MockComponentWorker addMock(Class<T> componentInterface, U componentMock)
    {
        registerMock(componentInterface, componentMock);
        return this;
    }

    /**
     * Registers a mock component to be returned by this component worker.
     * <p/>
     * This method accepts a lazy reference to the implementation in order to allow lazy initialisation on first access.
     *
     * @param componentInterface the interface that the mock component must implement
     * @param componentMockRef the lazy reference to the component that implements the interface
     * @param <T> the {@code componentInterface}
     * @param <U> the {@code componentMock}'s class, which must implement {@code <T>}
     * @return {@code this}, for convenience
     */
    public <T, U extends T> MockComponentWorker addMock(Class<T> componentInterface, LazyReference<U> componentMockRef)
    {
        lazyImplementationReferences.put(componentInterface, componentMockRef);
        return this;
    }

    /**
     * Obtains the registered mock of the specified interface type.  Test code would normally only
     * need to call this to retrieve access to mocks that were provided automatically (such as the
     * {@link UserKeyService}) or that the test registered earlier and is now ready to stub.
     * <p/>
     * JIRA itself will also sometimes use this method (or {@link #getComponentOfType(Class)}) to
     * resolve dependencies.  Arguably, this method should throw an exception rather than return
     * {@code null} when a component is requested without a mock provided for it; however, some
     * unit tests will resolve the component during construction without the test code path
     * actually needing to use it, so this method just generates a warning message in the log
     * for this case.
     *
     * @param componentClass the interface for which an implementation is desired
     * @param <T> the return type inferred from the specified {@code componentClass}.
     * @return the requested component, or {@code null} if no mock implementation has been
     *      provided for it.
     */
    @SuppressWarnings ( { "unchecked" })
    @Override
    public <T> T getComponent(Class<T> componentClass)
    {
        Object component = implementations.get(componentClass);
        if (component == null)
        {
            LazyReference<?> lazyReference = lazyImplementationReferences.get(componentClass);
            if (lazyReference != null)
            {
                component = lazyReference.get();
            }
        }

        if (component == null)
        {
            LOG.warn("No mock implementation was provided for component '" + componentClass.getName() + '\'');
        }
        return (T)component;
    }

    /**
     * Although the {@link ComponentAccessor} specifies different semantics for this method,
     * in this mock implementation it behaves identically to {@link #getComponent(Class)}.
     *
     * @param componentClass as for {@link #getComponent(Class)}
     * @param <T> as for {@link #getComponent(Class)}
     * @return as for {@link #getComponent(Class)}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getComponentOfType(Class<T> componentClass)
    {
        return getComponent(componentClass);
    }

    /**
     * Although the {@link ComponentAccessor} specifies different semantics for this method,
     * in this mock implementation it behaves identically to {@link #getComponent(Class)}.
     *
     * @param componentClass as for {@link #getComponent(Class)}
     * @param <T> as for {@link #getComponent(Class)}
     * @return as for {@link #getComponent(Class)}
     */
    @Override
    public <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
    {
        return getComponent(componentClass);
    }

    /**
     * Convenience method that just calls
     * {@link ComponentAccessor}.{@link ComponentAccessor#initialiseWorker(ComponentAccessor.Worker) initialiseWorker(this)}.
     * If you are developing a plugin that must support JIRA versions prior to v6.0, then that &#64;Internal
     * method must be used directly, instead.
     *
     * @since v6.0
     */
    public MockComponentWorker init()
    {
        ComponentAccessor.initialiseWorker(this);
        return this;
    }

    public MockUserPreferencesManager getMockUserPreferences()
    {
        return mockUserPreferences;
    }

    public MockApplicationProperties getMockApplicationProperties()
    {
        return mockApplicationProperties;
    }

    public MockUserKeyService getMockUserKeyService()
    {
        return mockUserKeyService;
    }
}
