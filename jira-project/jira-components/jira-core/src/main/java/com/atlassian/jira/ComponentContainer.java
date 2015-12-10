package com.atlassian.jira;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.config.component.PicoContainerFactory;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.parameters.ComponentParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.unmodifiableSet;

/**
 * Register Components and track which ones are available to plugins.
 */
class ComponentContainer
{

    private static final Logger LOG = LoggerFactory.getLogger(ComponentContainer.class);

    enum Scope
    {
        /**
         * Provided to Plugins2 plugins
         */
        PROVIDED
                {
                    @Override
                    Registrar get(final Registry registry)
                    {
                        return new Registrar()
                        {
                            public void register(final Class<?> klass)
                            {
                                registry.register(klass);
                            }

                            public void register(final ComponentAdapter adapter)
                            {
                                registry.register(adapter);
                            }
                        };
                    }
                },

        /**
         * Not provided to Plugins2 plugins
         */
        INTERNAL
                {
                    @Override
                    Registrar get(final Registry registry)
                    {
                        return new Registrar()
                        {
                            public void register(final Class<?> klass)
                            {
                            }

                            public void register(final ComponentAdapter adapter)
                            {
                            }
                        };
                    }
                };

        abstract Registrar get(Registry registry);

        interface Registrar
        {
            abstract void register(Class<?> klass);

            abstract void register(ComponentAdapter adapter);
        }
    }

    private final MutablePicoContainer container = PicoContainerFactory.defaultJIRAContainer();
    private final Registry registry = new Registry();

    private final boolean initializeComponentsEagerly;

    public ComponentContainer()
    {
        this(false);
    }

    /**
     *
     * @param eagerInitialization indicates whether container should initialize <b>all</b> components as a part of startup process
     */
    public ComponentContainer(boolean eagerInitialization)
    {
        this.initializeComponentsEagerly = eagerInitialization;
    }

    public static ComponentContainer withEagerInitialization()
    {
        return new ComponentContainer(true);
    }

    public static ComponentContainer withoutEagerInitialization()
    {
        return new ComponentContainer(false);
    }

    //
    // accessors
    //

    MutablePicoContainer getPicoContainer()
    {
        return container;
    }

    ComponentAdapter getComponentAdapter(final Class<?> key)
    {
        return container.getComponentAdapter(key);
    }

    <T> T getComponentInstance(final Class<T> key)
    {
        return container.getComponent(key);
    }

    HostComponentProvider getHostComponentProvider()
    {
        return new HostComponentProviderImpl(registry);
    }

    static class HostComponentProviderImpl implements HostComponentProvider
    {
        private final Registry registry;

        HostComponentProviderImpl(final ComponentContainer.Registry registry)
        {
            this.registry = registry;
        }

        public void provide(final ComponentRegistrar registrar)
        {
            final Set<Component> components = registry.getComponents();
            final Set<String> usedKeys = new HashSet<String>();
            for (final Component component : components)
            {
                final Class<?>[] interfaces = component.getInterfaces();
                for (final Class<?> iface : interfaces)
                {
                    final String name = extractSpringLikeBeanNameFromInterface(usedKeys, iface);
                    registrar.register(iface).forInstance(component.getInstance()).withName(name);
                    usedKeys.add(name);
                }
            }
        }
    }

    //
    // register instances
    //

    void instance(final Scope scope, final Object instance)
    {
        // cannot provide, non-interface keys not supported
        scope.get(registry).register(instance.getClass());
        container.addComponent(instance);
    }

    void instance(final Scope scope, final String key, final Object instance)
    {
        // cannot provide, String keys not supported
        scope.get(registry).register(instance.getClass());
        container.addComponent(key, instance);
    }

    <T, S extends T> void instance(final Scope scope, final Class<T> key, final S instance)
    {
        scope.get(registry).register(key);
        container.addComponent(key, instance);
    }

    //
    // register implementations
    //

    void implementation(final Scope scope, final Class<?> implementation)
    {
        // cannot provide, non-interface keys not supported
        scope.get(registry).register(implementation);
        container.addComponent(implementation);
    }

    <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation)
    {
        scope.get(registry).register(key);
        container.addComponent(key, implementation);
    }

    /**
     * Registers the interface with a concrete implementation class using the given variable number of arguments as the
     * keys PICO will use to look up during instantiation.  ComponentParameters are created for each parameterKeys
     * object if its NOT already a {@link Parameter}.
     *
     * @param scope the container scope
     * @param key the interface to register
     * @param implementation the concrete implementation of interfaceClass
     * @param parameterKeys the variable number of parameters
     */
    <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation, final Object... parameterKeys)
    {
        final Parameter[] componentParameters = new Parameter[parameterKeys.length];
        for (int i = 0; i < parameterKeys.length; i++)
        {
            Object parameter = parameterKeys[i];
            if (parameter instanceof Parameter)
            {
                componentParameters[i] = (Parameter) parameter;
            }
            else
            {
                componentParameters[i] = new ComponentParameter(parameter);
            }
        }
        implementation(scope, key, implementation, componentParameters);
    }

    <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation, final Parameter[] parameters)
    {
        scope.get(registry).register(key);
        container.addComponent(key, implementation, parameters);
    }

    /*
     * Special method that registers the default constructor. Workaround for http://jira.codehaus.org/browse/PICO-201
     */

    <T> void implementationUseDefaultConstructor(final Scope scope, final Class<T> key, final Class<? extends T> implementation)
    {
        scope.get(registry).register(key);
        // we need to parameterise this, otherwise it tries to load the greediest constructor
        container.addComponent(key, implementation, new Parameter[] { });
    }

    void transfer(ComponentManager from, ComponentContainer.Scope scope, Class<?> key)
    {
        // Don't ask me why these casts are needed, IDEA said it compiled fine, javac begged to differ
        instance(scope, (Class<Object>) key, (Object) key.cast(from.getContainer().getComponent(key)));
    }


    //
    // direct component registration
    //

    void component(final Scope scope, final ComponentAdapter componentAdapter)
    {
        // needs to do some smarts to work out if available
        scope.get(registry).register(componentAdapter);
        container.addAdapter(componentAdapter);
    }

    /**
     * Direct adapter registration without using factory defined in {@link com.atlassian.jira.config.component.PicoContainerFactory#defaultJIRAContainer()}
     * <b>Note:</b> This method should not be used commonly - it is prepared for specialized adapters managing component lifecycle on their own
     * @param scope the component's scope
     * @param componentAdapter adapter providing component
     */
    void componentWithoutDefaultBehaviour(final Scope scope, final ComponentAdapter componentAdapter)
    {
        scope.get(registry).register(componentAdapter);
        container.as(Characteristics.NONE).addAdapter(componentAdapter);
    }

    void initializeEagerComponents()
    {
        if(initializeComponentsEagerly){
            container.getComponents();
        }
    }

    //
    // utility
    //

    /**
     * Determines unique spring like bean name from a set of interfaces.  Bean name generated by taking the
     * alphabetically first class name and lower-casing it.  Any duplicates will have numeric postfixes, starting with 1.
     *
     * @param usedKeys A set of bean names already used
     * @param clazz An interface to use to generate the bean name
     * @return A unique bean name that will never be null
     */
    static String extractSpringLikeBeanNameFromInterface(final Set<String> usedKeys, final Class<?> clazz)
    {
        notNull("clazz", clazz);
        // Allow for hard-coded aliases for known duplicate names.
        String hardCodedKey = mapDuplicates(clazz);
        if (hardCodedKey != null)
        {
            return hardCodedKey;
        }

        final String className = clazz.getSimpleName();
        final String calculatedKey = className.substring(0, 1).toLowerCase(Locale.ENGLISH) + className.substring(1);
        if (usedKeys.contains(calculatedKey))
        {
            // Duplicate keys lead to very hard to debug problems - lets know up front if we get a duplicate and use
            // mapDuplicates() to hard-code the key for legacy Components if we really must have them.
            throw new IllegalStateException("Duplicate component key found for '" + calculatedKey + "'.");

        }

        return calculatedKey;
    }

    private static String mapDuplicates(Class<?> clazz)
    {
        // The SAL ApplicationProperties is added in the BootstrapContainerRegistrar.
        // You will only see this in Setup Step 1.
        if (clazz.equals(com.atlassian.sal.api.ApplicationProperties.class))
        {
            return "salApplicationProperties";
        }
        if (clazz.equals(com.atlassian.jira.util.I18nHelper.class))
        {
            // avoid conflict with existing Crowd I18nHelper
            return "contextI18nHelper";
        }

        return null;
    }

    /**
     * maintain the provided Components.
     */
    private class Registry
    {
        private final Set<Class<?>> availableComponents = new HashSet<Class<?>>();

        void register(final Class<?> componentKey)
        {
            if (!componentKey.isInterface())
            {
                throw new IllegalArgumentException(componentKey + " must be an interface to provide to plugins.");
            }
            if (availableComponents.contains(componentKey))
            {
                throw new IllegalArgumentException(componentKey + " has already been provided.");
            }
            availableComponents.add(componentKey);
        }

        void register(final ComponentAdapter componentAdapter)
        {
            final Object key = componentAdapter.getComponentKey();
            if (key instanceof Class<?>)
            {
                register((Class<?>) key);
            }
        }

        Set<Component> getComponents()
        {
            final MultiMap<Object, Class<?>, Set<Class<?>>> instances = MultiMaps.create(new Supplier<Set<Class<?>>>()
            {
                public Set<Class<?>> get()
                {
                    return new HashSet<Class<?>>();
                }
            });
            for (final Class<?> exposedInterface : availableComponents)
            {
                final Object instance = getComponentInstance(exposedInterface);
                if (instance != null)
                {
                    instances.putSingle(instance, exposedInterface);
                }
            }
            final Set<Component> result = new HashSet<Component>();
            for (final Map.Entry<Object, Set<Class<?>>> entry : instances.entrySet())
            {
                result.add(new Component(entry.getKey(), entry.getValue()));
            }
            return Collections.unmodifiableSet(result);
        }
    }

    private static final class Component
    {
        private final Object instance;
        private final Set<Class<?>> interfaces;

        Component(final Object instance, final Set<Class<?>> interfaces)
        {
            this.instance = notNull("instance", instance);
            this.interfaces = unmodifiableSet(new HashSet<Class<?>>(notNull("interfaces", interfaces)));
        }

        /**
         * The actual component instance.
         * @return the instance
         */
        public Object getInstance()
        {
            return instance;
        }

        /**
         * The interfaces exported by this component.
         * @return the interfaces
         */
        public Class<?>[] getInterfaces()
        {
            return interfaces.toArray(new Class[interfaces.size()]);
        }

        @Override
        public int hashCode()
        {
            return instance.hashCode();
        }

        @SuppressWarnings ({ "SimplifiableIfStatement" })
        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Component)
            {
                return instance == ((Component) obj).instance;
            }
            else
            {
                return false;
            }
        }
    }
}
