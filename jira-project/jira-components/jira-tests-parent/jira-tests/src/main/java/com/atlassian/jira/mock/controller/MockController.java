package com.atlassian.jira.mock.controller;

import com.atlassian.jira.util.collect.ClassMap;
import com.atlassian.jira.util.collect.CollectionBuilder;
import junit.framework.AssertionFailedError;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.objenesis.ObjenesisHelper;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.injectors.AbstractInjectionFactory;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ConstructorInjector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class can be used to better "formalise" the use of EasyMock {@link org.easymock.MockControl} objects to mock out
 * interfaces in unit tests.
 * <p/>
 * It provides automatic MockControl creation and invocation, as well as  consistent replay and verification of all mock
 * objects.
 * <p/>
 * It assumes a certain pattern of unit test. <ol> <li>The TestCase will create a new MockController in its setup()</li>
 * <li>Each interface that is to be mocked out will be created via calls to controller.getMock()</li> <li>Before the
 * object under test is made, the controller.replay() will be called</li> <li>After the testing is done, the
 * controller.verify() is called</li> </ol>
 * <p/>
 * Every time you interaction with a mocked out class, (via getMock() or invoke a mocked out method with in record mode,
 * then its peer MockControl becomes the <i>current</i> one and hence calls to methods like setReturnValue() will be
 * delegated to that MockControl under the covers.
 *
 * <p><strong> This class should not be used / extended from anymore as the current approach to unit testing in JIRA is
 * to use {@link org.mockito.Mockito}.</strong></p>
 * @deprecated since 5.0
 * @since 4.0
 */
@Deprecated
public class MockController
{
    private final Map<Class<?>, MockControl> mapOfControls = new LinkedHashMap<Class<?>, MockControl>();
    private final ClassMap mapOfMockedObjects = ClassMap.Factory.create(new LinkedHashMap<Class<?>, Object>());
    private final ClassMap mapOfInstanceObjects = ClassMap.Factory.create(new LinkedHashMap<Class<?>, Object>());
    private final MockControlFactory defaultMockControlFacory;
    private final DefaultPicoContainer picoContainer;

    private ControllerState state;
    private MockControl currentMockControl;
    private Method lastMethodInvoked;

    private interface MockControlFactory
    {
        <T> MockControl createMockControl(Class<T> interfaceClass);

        <T> MockControl createClassMockControl(Class<T> klass);
    }

    private static final MockControlFactory strictFactory = new MockControlFactory()
    {
        public <T> MockControl createMockControl(final Class<T> interfaceClass)
        {
            return ToStringedMockControl.<T> createStrictControl(interfaceClass);
        }

        public <T> MockControl createClassMockControl(final Class<T> klass)
        {
            return MockClassControl.createStrictControl(klass);
        }
    };

    private static final MockControlFactory niceFactory = new MockControlFactory()
    {
        public <T> MockControl createMockControl(final Class<T> interfaceClass)
        {
            return ToStringedMockControl.<T> createNiceControl(interfaceClass);
        }

        public <T> MockControl createClassMockControl(final Class<T> klass)
        {
            return MockClassControl.createNiceControl(klass);
        }
    };

    /**
     * An enumeration to show the different states of the MockController
     */
    public static enum ControllerState
    {
        START,
        RECORD,
        REPLAYED,
        VERIFIED
    }

    /**
     * @return a MockController that uses <b>strict</b> {@link org.easymock.MockControl}'s by default when {@link
     *         #getMock(Class)} is called
     */
    public static MockController createStrictContoller()
    {
        return new MockController(strictFactory);
    }

    /**
     * @return a MockController that uses <b>nice</b> {@link org.easymock.MockControl}'s by default when {@link
     *         #getMock(Class)} is called
     */
    public static MockController createNiceContoller()
    {
        return new MockController(niceFactory);
    }

    /**
     * Instantiates a MockController that produces <b>strict</b> MockControl objects by default
     */
    public MockController()
    {
        this(strictFactory);
    }

    private MockController(final MockControlFactory mockControlFactory)
    {
        picoContainer = new DefaultPicoContainer();
        defaultMockControlFacory = mockControlFactory;
        currentMockControl = null;
        lastMethodInvoked = null;
        state = ControllerState.START;
    }

    /**
     * Returns the current state of the MockController
     *
     * @return the current state of the MockController
     */
    public ControllerState getState()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return new StringBuilder("state : '").append(state).append("' lastMethod :  ").append(
            lastMethodInvoked == null ? "null" : lastMethodInvoked.getName()).append(" currentMockControl :  ").append(currentMockControl).append(
            " : ").append(mapOfControls).toString();
    }

    /**
     * This returns a mocked out implementation of aClass AND creates a {@link MockControl} under to covers for this
     * interface.  The type of MockControl (strict or nice) depends on the default behaviour for this MockController.
     * The created MockControl then becomes the current MockControl in play
     * <p/>
     * If you have already asked for an instance of interfaceClass, then the same mock object is returned to the caller
     * and now extra objects will be created.
     *
     * @param aClass the class or interface class to mock out
     *
     * @return a mocked out instance of interfaceClass
     */
    public <T> T getMock(final Class<T> aClass)
    {
        return getMockObjectImpl(aClass, defaultMockControlFacory);
    }

    /**
     * Synonym for {@link #getMock(Class)} to be more EasyMock like
     *
     * @param aClass the class or interface class to mock out
     *
     * @return a mocked out instance of interfaceClass
     */
    public <T> T createMock(final Class<T> aClass)
    {
        return getMock(aClass);
    }

    /**
     * This returns a mocked out implementation of aClass AND creates a strict {@link MockControl} under to covers for
     * this interface.  This MockControl then becomes the current MockControl in play
     * <p/>
     * If you have already asked for an instance of interfaceClass, then the same mock object is returned to the caller
     * and now extra objects will be created.
     *
     * @param aClass the class or interface class to mock out
     *
     * @return a mocked out instance of interfaceClass
     */
    public <T> T getStrictMock(final Class<T> aClass)
    {
        return getMockObjectImpl(aClass, strictFactory);
    }

    /**
     * Synonym for {@link #getStrictMock(Class)} to be more EasyMock like
     *
     * @param aClass the class or interface class to mock out
     *
     * @return a mocked out instance of interfaceClass
     */
    public <T> T createStrictMock(final Class<T> aClass)
    {
        return getStrictMock(aClass);
    }

    /**
     * This returns a mocked out implementation of aClass AND creates a nice {@link MockControl} under to covers for
     * this interface.  This MockControl then becomes the current MockControl in play
     * <p/>
     * If you have already asked for an instance of interfaceClass, then the same mock object is returned to the caller
     * and now extra objects will be created.
     *
     * @param aClass the class or interface class to mock out
     *
     * @return a mocked out instance of interfaceClass
     */
    public <T> T getNiceMock(final Class<T> aClass)
    {
        return getMockObjectImpl(aClass, niceFactory);
    }

    /**
     * Synonym for {@link #getNiceMock(Class)} to be more EasyMock like
     *
     * @param aClass the class or interface class to mock out
     *
     * @return a mocked out instance of interfaceClass
     */
    public <T> T createNiceMock(final Class<T> aClass)
    {
        return getNiceMock(aClass);
    }

    /**
     * This method allow you to add an actual object instance to be added into the mix.  For example if a class you are
     * mocking out needs an actual class type and not a interface type, then you can use this method to supply the
     * object
     *
     * @param objectInstance a non null object instance
     *
     * @return objectInstance
     */
    public <T> T addObjectInstance(final T objectInstance)
    {
        if (objectInstance == null)
        {
            throw new IllegalArgumentException("You must provide non null object instances");
        }
        beginRecording();
        mapOfInstanceObjects.put(objectInstance);
        // tell pico about it
        picoContainer.addComponent(objectInstance);
        return objectInstance;
    }

    private <T> T getMockObjectImpl(final Class<T> aClass, final MockControlFactory mockControlFactory)
    {
        if (aClass == null)
        {
            throw new IllegalArgumentException("You must provide a non null interface class");
        }
        else if (!aClass.isInterface() && !isNonFinalClass(aClass))
        {
            throw new IllegalArgumentException("You can only mock out interfaces and non final classes.");
        }

        final T mockObject = aClass.cast(mapOfMockedObjects.get(aClass));
        if (mockObject != null)
        {
            currentMockControl = mapOfControls.get(aClass);
            return mockObject;
        }

        if (aClass.isInterface())
        {
            return getMockInterfaceObjectImpl(aClass, mockControlFactory);
        }
        else
        {
            return getMockClassObjectImpl(aClass, mockControlFactory);
        }
    }

    private <T> T getMockInterfaceObjectImpl(final Class<T> interfaceClass, final MockControlFactory mockControlFactory)
    {
        beginRecording();
        final MockControl mockControl = getMockControlImpl(interfaceClass, mockControlFactory);
        final Object baseMockObj = mockControl.getMock();

        // wrap the base mock object into a state checking invocation handler
        final StateCheckingInvocationHandler invocationHandler = new StateCheckingInvocationHandler(baseMockObj, mockControl);
        final T mockObject = interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
            invocationHandler));

        //
        // tell PICO about this interface
        picoContainer.addComponent(interfaceClass, mockObject);
        //
        // put in our map of objects
        mapOfMockedObjects.put(interfaceClass, mockObject);

        //
        // it is now the current mock object because they access it
        currentMockControl = mockControl;

        return mockObject;
    }

    private <T> T getMockClassObjectImpl(final Class<T> klass, final MockControlFactory mockControlFactory)
    {
        beginRecording();

        final MockControl mockControl = getClassMockControlImpl(klass, mockControlFactory);
        final Object baseMockObj = mockControl.getMock();
        final StateCheckingInvocationHandler invocationHandler = new StateCheckingInvocationHandler(baseMockObj, mockControl);

        //This code is shamefully borrowed from EasyMock class extension.

        final Enhancer enchancer = new Enhancer();
        enchancer.setSuperclass(klass);
        enchancer.setCallbackType(net.sf.cglib.proxy.InvocationHandler.class);
        final Class newKlass = enchancer.createClass();
        Enhancer.registerCallbacks(newKlass, new Callback[] { invocationHandler });

        //Create the object using the Voodoo that you do.
        final Factory mock = (Factory) ObjenesisHelper.newInstance(newKlass);

        //This needs to be called to ensure that the callback is registered with the enhanced class.
        //Normally the callback is registered when the constructor runs, but in this case Objenesis may
        //not actually call the constructor.
        mock.getCallback(0);

        final T mockObject = klass.cast(mock);

        //
        // tell PICO about this interface
        picoContainer.addComponent(klass, mockObject);
        //
        // put in our map of objects
        mapOfMockedObjects.put(klass, mockObject);

        currentMockControl = mockControl;

        return mockObject;
    }

    private void beginRecording()
    {
        if (state == ControllerState.START)
        {
            state = ControllerState.RECORD;
        }
        if (state != ControllerState.RECORD)
        {
            throw new IllegalStateException("You must be in record state");
        }
    }

    private <T> MockControl getMockControlImpl(final Class<T> interfaceClass, final MockControlFactory mockControlFactory)
    {
        MockControl mockControl = mapOfControls.get(interfaceClass);
        if (mockControl == null)
        {
            mockControl = mockControlFactory.createMockControl(interfaceClass);
            mapOfControls.put(interfaceClass, mockControl);
        }
        return mockControl;
    }

    private <T> MockControl getClassMockControlImpl(final Class<T> klass, final MockControlFactory mockControlFactory)
    {
        MockControl mockControl = mapOfControls.get(klass);
        if (mockControl == null)
        {
            mockControl = mockControlFactory.createClassMockControl(klass);
            mapOfControls.put(klass, mockControl);
        }
        return mockControl;
    }

    /**
     * This method allows you access to the underlying MockControl objects.  Ordinarily you should need to use this
     * method unless doing something special.
     *
     * @param interfaceClass the interface class that is being mocked out
     *
     * @return MockControl or null if not call to @link #getMock} for this interfaceClass has not been made
     */
    public MockControl getMockControl(final Class<?> interfaceClass)
    {
        return mapOfControls.get(interfaceClass);
    }

    /**
     * This method allows you access to the current MockControl.  Ordinarily you would not need access to this method as
     * the MockControl methods are delegated to the currentMockControl when called.
     *
     * @return the current MockControl or null if there isn't one
     */
    public MockControl getCurrentMockControl()
    {
        return currentMockControl;
    }

    /**
     * This method allows you access to the interfaces and classes that have been mocked out by this MockController.
     * Ordinarily you should not have to call this method but its here for completeness.
     *
     * @return a non null List or interface {@link Class}es
     */
    public List<Class<?>> getMockedTypes()
    {
        return new ArrayList<Class<?>>(mapOfMockedObjects.keySet());
    }

    /**
     * This method allows you access to the mocked object instances that have been mocked out by this MockController.
     * Ordinarily you should not have to call this method but its here for completeness.
     *
     * @return a non null List of mocked {@link Object}s
     */
    public List<Object> getMockedObjects()
    {
        return new ArrayList<Object>(mapOfMockedObjects.values());
    }

    /**
     * This method allows you access to the MockControl instances that have been created by this MockController.
     * Ordinarily you should not have to call this method but its here for completeness.
     *
     * @return a non null List of {@link MockControl}s
     */
    public List<MockControl> getMockControls()
    {
        return new ArrayList<MockControl>(mapOfControls.values());
    }

    /**
     * This method allows you access to the object instances that have been placed in the mix by called to {@link
     * #addObjectInstance(Object)} .  Ordinarily you should not have to call this method but its here for completeness.
     *
     * @return a non null List of {@link Object}s
     */
    public List<Object> getObjectInstances()
    {
        return new ArrayList<Object>(mapOfInstanceObjects.values());
    }

    /**
     * This will try to use the mock interfaces that have been previously been added to the MockController to
     * instantiate a new instance of implementationClass.  It uses a <b>greediest</b> constructor based algorithm to do
     * this.
     * <p/>
     * It will then replay the MockControl objects in play, ready for the instantiated object to be tested.
     *
     * @param classUnderTest the class to instantiate.  It must be non null and it must not be an Interface.
     *
     * @return an instance of implementationClass using the mocked out interfaces already in the MockController
     *
     * @throws IllegalStateException if an instance cannot be instantiated because of a lack of mocked interfaces.
     */
    public <T> T instantiateAndReplay(final Class<T> classUnderTest)
    {
        return instantiateAndReplayImpl(classUnderTest, false);
    }

    private <T> T instantiateAndReplayImpl(final Class<T> classUnderTest, final boolean allowNonPublicMethods)
    {
        beginRecording();
        checkInterface(classUnderTest);

        final DefaultPicoContainer childContainer;
        if (allowNonPublicMethods)
        {
            childContainer = new DefaultPicoContainer(new AccessibleComponentAdapterFactory(), picoContainer);
        }
        else
        {
            childContainer = new DefaultPicoContainer(picoContainer);
        }
        childContainer.addComponent(classUnderTest);

        // try to use PICO to instantiate an instance of implementationClass
        try
        {
            replayInternal();
            return classUnderTest.cast(childContainer.getComponent(classUnderTest));
        }
        catch (final AbstractInjector.UnsatisfiableDependenciesException ude)
        {
            final IllegalStateException stateException = new IllegalStateException(
                "You don't have all the dependent interfaces needed to create an instance of " + classUnderTest.getName());
            stateException.initCause(ude);
            throw stateException;
        }
    }

    /**
     * Makes non public contructors accessible
     */
    static class AccessibleComponentAdapterFactory extends AbstractInjectionFactory
    {


        @Override
        public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class<T> componentImplementation, final Parameter... parameters)
                throws PicoCompositionException
        {
            boolean useNames = AbstractBehaviorFactory.arePropertiesPresent(componentProperties, Characteristics.USE_NAMES, true);
            final boolean rememberChosenCtor = true;
            ConstructorInjector injector = new ConstructorInjector(componentKey, componentImplementation, parameters, componentMonitor, useNames, rememberChosenCtor).withNonPublicConstructors();
            injector.enableEmjection(AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.EMJECTION_ENABLED));
            return wrapLifeCycle(componentMonitor.newInjector(injector), lifecycleStrategy);
        }
    }

    /**
     * Try an create an instance of the passed class for testing. It tries to create the object by calling the passed
     * constructor. The arguments to the constructor will be made from mocks previously registered with the controller
     * or new mocks if they have not been registered. All the mocks will be placed in the replay state.
     *
     * @param classUnderTest the class to attempt to create.
     * @param constructor    the constructor to call.
     *
     * @return the newly created object.
     *
     * @throws IllegalStateException if the controller is unable to create the object for any reason.
     */
    public <T> T instantiateAndReplayNice(final Class<T> classUnderTest, final Constructor<T> constructor)
    {
        beginRecording();
        checkInterface(classUnderTest);

        if ((constructor == null) || (constructor.getDeclaringClass() != classUnderTest))
        {
            throw new IllegalArgumentException(
                "Passed constructor belongs to '" + classUnderTest.getName() + "'. I must belong to '" + classUnderTest.getName() + "'.");
        }
        final Class<?>[] classes = constructor.getParameterTypes();
        final List<Class<?>> needMocks = new ArrayList<Class<?>>(classes.length);
        for (final Class<?> aClass : classes)
        {
            if (picoContainer.getComponent(aClass) == null)
            {
                //lets try an add one.
                if (aClass.isInterface() || isNonFinalClass(aClass))
                {
                    needMocks.add(aClass);
                }
                else
                {
                    throw new IllegalStateException(
                        "Unable to automatically mock out '" + aClass.getName() + "' to create '" + classUnderTest.getName() + "'.");
                }
            }
        }

        for (final Class<?> class1 : needMocks)
        {
            getMock(class1);
        }

        return instantiateAndReplayImpl(classUnderTest, true);
    }

    /**
     * This will create an instance of the passed class for testing. It tries to create the object by calling each of
     * the objects constructors in turn. The arguments to the constructor will be made from mocks previously registered
     * with the controller or new mocks if they have not been registered. All the mocks will be placed in the replay
     * state.
     * <p/>
     * This is a synonym for {@link #instantiate(Class)} with the added bonus of being less characters to
     * read and write ;)
     * <p/>
     * This is the method you will most likely call on the MockController to instatiate the class under test
     *
     * @param classUnderTest the class to attempt to create.
     *
     * @return the newly created object.
     *
     * @throws IllegalStateException if the controller is unable to create the object for any reason.
     */
    public <T> T instantiate(final Class<T> classUnderTest)
    {
        beginRecording();
        checkInterface(classUnderTest);

        final Constructor<?>[] declaredConstructors = classUnderTest.getDeclaredConstructors();
        @SuppressWarnings("unchecked")
        final Constructor<T>[] constructors = (Constructor<T>[]) declaredConstructors;
        Arrays.sort(constructors, new ContructorArgsLengthComparator<T>());
        RuntimeException firstRuntime = null;

        for (final Constructor<T> constructor : constructors)
        {
            try
            {
                return instantiateAndReplayNice(classUnderTest, constructor);
            }
            catch (final RuntimeException e)
            {
                //oops an error occurred. Try another.
                if (firstRuntime == null)
                {
                    firstRuntime = e;
                }
            }
        }

        final IllegalStateException stateException = new IllegalStateException(
            "You don't have all the dependent interfaces needed to create an instance of " + classUnderTest.getName());
        if (firstRuntime != null)
        {
            stateException.initCause(firstRuntime);
        }
        throw stateException;
    }

    private <T> void checkInterface(final Class<T> classUnderTest)
    {
        if (classUnderTest == null)
        {
            throw new IllegalArgumentException("You must provide a non null implementationClass");
        }
        if (classUnderTest.isInterface())
        {
            throw new IllegalArgumentException("You can only instantiate implementation classes, not interfaces");
        }
    }

    private boolean isNonFinalClass(final Class<?> aClass)
    {
        return !(aClass.isInterface() || aClass.isAnnotation() || Modifier.isFinal(aClass.getModifiers()));
    }

    /**
     * This method can be called at the end of a TestCase to check that the MockController is in a good state.  It will
     * check that you have called replay().  It will also call verify() if its hasn't already been called.
     *
     * @throws IllegalStateException if you haven't called replay() (eg you are in RECORD state.
     */
    public void onTestEnd()
    {
        if (state == ControllerState.RECORD)
        {
            throw new IllegalStateException("You have not called replay() on the MockController");
        }
        else if (state == ControllerState.REPLAYED)
        {
            // we can call verify for them if they haven't already don't it
            verifyInternal();
        }
    }

    /**
     * This can traverse a list of mocked objects not created by the mockController and then do something against them
     */
    private abstract class MocksNotCreatedByUsVisitor
    {
        private MocksNotCreatedByUsVisitor(final Object... easyMockedCreatedObjects)
        {
            // don't replay stuff that is inside the mockController.  Only stuff that may have been created by EasyMock directly.
            if ((easyMockedCreatedObjects != null) && (easyMockedCreatedObjects.length > 0))
            {
                final List<Object> mockControllerMockedObjects = getMockedObjects();
                for (final Object easyMock : easyMockedCreatedObjects)
                {
                    if (!mockControllerMockedObjects.contains(easyMock))
                    {
                        doMockOperation(easyMock);
                    }
                }
            }
        }

        abstract void doMockOperation(final Object easyMockedCreatedObject);
    }

    /**
     * Called to replay ALL of the MockControl objects inside this MockController as well as replay any EasyMock created
     * mocks that where created outside the mock controller.
     *
     * @param easyMockedCreatedObjects any EasyMocks created via {@link org.easymock.EasyMock#createMock(Class)} that
     *                                 you also want to replay
     */
    public void replay(final Object... easyMockedCreatedObjects)
    {
        replayInternal();
        new MocksNotCreatedByUsVisitor(easyMockedCreatedObjects)
        {
            @Override
            void doMockOperation(final Object easyMockedCreatedObject)
            {
                EasyMock.replay(easyMockedCreatedObject);
            }
        };
    }

    /**
     * Called to verify ALL of the MockControl objects inside this MockController as well as verify any EasyMock created
     * mocks that where created outside the mock controller.
     *
     * @param easyMockedCreatedObjects any EasyMocks created via {@link EasyMock#createMock(Class)} that you also want
     *                                 to verify
     */
    final public void verify(final Object... easyMockedCreatedObjects)
    {
        verifyInternal();
        new MocksNotCreatedByUsVisitor(easyMockedCreatedObjects)
        {
            @Override
            void doMockOperation(final Object easyMockedCreatedObject)
            {
                EasyMock.verify(easyMockedCreatedObject);
            }
        };
    }

    /**
     * Called to reset ALL of the MockControl objects inside this MockController as well as reset any EasyMock created
     * mocks that where created outside the mock controller.
     *
     * @param easyMockedCreatedObjects any EasyMocks created via {@link EasyMock#createMock(Class)} that you also want
     *                                 to verify
     */
    final public void reset(final Object... easyMockedCreatedObjects)
    {
        resetInternal();
        new MocksNotCreatedByUsVisitor(easyMockedCreatedObjects)
        {
            @Override
            void doMockOperation(final Object easyMockedCreatedObject)
            {
                EasyMock.reset(easyMockedCreatedObject);
            }
        };
    }

    /**
     * Called to reset ALL of the MockControl objects inside this MockController.
     *
     * @see org.easymock.MockControl#reset()
     */
    private void resetInternal()
    {
        for (MockControl mockControl : mapOfControls.values())
        {
            if (mockControl instanceof ToStringedMockControl<?>)
            {
                mockControl = ((ToStringedMockControl<?>) mockControl).getDelegateMockControl();
            }
            mockControl.reset();
        }

        currentMockControl = null;
        lastMethodInvoked = null;
        state = ControllerState.RECORD;
    }

    /**
     * Called to replay ALL of the MockControl objects inside this MockController.
     *
     * @throws IllegalStateException if you have already replayed the MockControl's
     * @see org.easymock.MockControl#replay()
     */
    private void replayInternal()
    {
        if (state == ControllerState.REPLAYED)
        {
            throw new IllegalStateException("Already in replay state");
        }
        CollectionUtils.forAllDo(mapOfControls.values(), new Closure()
        {
            public void execute(final Object o)
            {
                ((MockControl) o).replay();
            }
        });
        state = ControllerState.REPLAYED;
    }

    /**
     * Called to verify ALL of the MockControl objects inside this MockController.
     *
     * @throws IllegalStateException if you have not already replayed the MockControl's
     * @see org.easymock.MockControl#verify()
     */
    private void verifyInternal()
    {
        if (state != ControllerState.REPLAYED)
        {
            throw new IllegalStateException("Not in replay state");
        }
        CollectionUtils.forAllDo(mapOfControls.values(), new Closure()
        {
            public void execute(final Object o)
            {
                ((MockControl) o).verify();
            }
        });
        state = ControllerState.VERIFIED;
    }

    private static final Set<String> exemptMethodNames;

    static
    {
        exemptMethodNames = CollectionBuilder.newBuilder("toString", "equals", "hashCode").asSet();
    }

    /**
     * This {@link java.lang.reflect.InvocationHandler}  is used to wrap the MockControl provided mock object and to do
     * a state check on whether the invocation is valid or not given that state of the MockController.
     */
    private class StateCheckingInvocationHandler implements InvocationHandler, net.sf.cglib.proxy.InvocationHandler
    {
        private final Object delegateObject;
        private final MockControl delegateControl;

        private StateCheckingInvocationHandler(final Object delegateObject, final MockControl delegateControl)
        {
            this.delegateObject = delegateObject;
            this.delegateControl = delegateControl;
        }

        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
        {
            validateInvocation(method);
            try
            {
                // in case of equals/toString/hashCode, we probably want to work against our MockController proxy of
                // the mocked class instead of EasyMock's proxy, because that is what is available to client code.
                // e.g. when passing a MockController proxy as a parameter expectation to a method on another
                // MockController proxy
                if (method.getName().equals("equals"))
                {
                    return proxy == args[0] ? Boolean.TRUE : Boolean.FALSE;
                }
                if (method.getName().equals("toString"))
                {
                    return "MockController for " + delegateObject.toString();
                }
                if (method.getName().equals("hashCode"))
                {
                    return System.identityHashCode(proxy);
                }

                method.setAccessible(true);
                return method.invoke(delegateObject, args);
            }
            catch (final Throwable t)
            {
                // it could be that the easy mock framework is having a hissy fit and
                // we want them to bubble up as having been thrown by us, on its behalf
                final Throwable cause = t.getCause();
                if (cause instanceof AssertionFailedError)
                {
                    //noinspection ThrowableInstanceNeverThrown
                    throw new AssertionFailedError("invoking " + delegateObject + " " + cause.getMessage()).initCause(cause);
                }
                else if ((t instanceof InvocationTargetException) && (cause != null))
                {
                    throw cause;
                }
                else
                {
                    throw t;
                }
            }
        }

        private void validateInvocation(final Method method)
        {
            // EasyMock does not mock out toString/hashCode/equals
            if (exemptMethodNames.contains(method.getName()))
            {
                return;
            }
            if (state == ControllerState.REPLAYED)
            {
                return;
            }
            currentMockControl = delegateControl;
            lastMethodInvoked = method;
        }
    }

    /**
     * Comparator that order's {@link java.lang.reflect.Constructor} objects by the number of arguments.
     */
    private static class ContructorArgsLengthComparator<T> implements Comparator<Constructor<T>>
    {
        public int compare(final Constructor<T> constructor1, final Constructor<T> constructor2)
        {
            // public first/ then protected / then package / then private
            int rc = getVisibility(constructor2) - getVisibility(constructor2);
            if (rc == 0)
            {
                //look for the longest arguments.
                rc = constructor2.getParameterTypes().length - constructor1.getParameterTypes().length;
            }
            return rc;
        }

        private int getVisibility(final Member member)
        {
            final int modifiers = member.getModifiers();
            if (Modifier.isPublic(modifiers))
            {
                return 4;
            }
            else if (Modifier.isProtected(modifiers))
            {
                return 3;
            }
            else if (Modifier.isPrivate(modifiers))
            {
                return 0;
            }
            else
            {
                return 2;
            }
        }
    }

    /*
    * ===================================================================
    * MockControl DELEGATE METHODS
    * ===================================================================
    */
    private void checkMockControlDelegateState(final String methodName)
    {
        if (currentMockControl == null)
        {
            throw new IllegalStateException(
                "You invoked a MockControl method " + methodName + "() but you have not called any mocked out methods before hand");
        }
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setThrowable(final Throwable throwable)
    {
        checkMockControlDelegateState("setThrowable");
        currentMockControl.setThrowable(throwable);
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setReturnValue(final boolean b)
    {
        checkMockControlDelegateState("setReturnValue");
        currentMockControl.setReturnValue(b);
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setReturnValue(final long l)
    {
        checkMockControlDelegateState("setReturnValue");
        currentMockControl.setReturnValue(l);
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setReturnValue(final Object o)
    {
        checkMockControlDelegateState("setReturnValue");
        currentMockControl.setReturnValue(o);
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setReturnValue(final Object o, final int i)
    {
        checkMockControlDelegateState("setReturnValue");
        currentMockControl.setReturnValue(o, i);
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setDefaultReturnValue(final boolean b)
    {
        checkMockControlDelegateState("setDefaultReturnValue");
        currentMockControl.setDefaultReturnValue(b);
    }

    /**
     * @deprecated since we moved to {@link org.easymock.EasyMock}.  Use EasyMock expectations instead
     */
    @Deprecated
    public void setDefaultReturnValue(final Object o)
    {
        checkMockControlDelegateState("setDefaultReturnValue");
        currentMockControl.setDefaultReturnValue(o);
    }
}
