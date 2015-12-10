package com.atlassian.jira.local;

import java.util.Comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.easymock.MockFactory;
import com.atlassian.jira.easymock.MockType;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.controller.MockController;

import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.easymock.LogicalOperator;
import org.junit.After;
import org.junit.Before;

/**
 * This base class was used to provide support for JIRA unit tests written using EasyMock
 * by making use of a {@link com.atlassian.jira.mock.controller.MockController}, it performs
 * automatic mock verification if it has not already been done.
 * <p/>
 * It will also guard against tests that don't call verify() and replay() on the MockController.
 *
 * <p><strong> This class should not be used / extended from anymore as the current approach to unit testing in JIRA is
 * to use {@link org.mockito.Mockito}.</strong></p>
 *
 * @since v3.13
 * @deprecated since 5.0
 */
@Deprecated
public abstract class MockControllerTestCase
{
    protected MockController mockController;
    protected MockComponentWorker componentAccessorWorker;

    public MockControllerTestCase()
    {
        this.mockController = new MockController();
    }

    @Before
    public final void setUpMockAnnotations() throws Exception
    {
        EasyMockAnnotations.initMocks(this, new MockControllerFactory());
        componentAccessorWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    @After
    public final void onTestEnd() throws Throwable
    {
        ComponentAccessor.initialiseWorker(null);
        // so did they come out of the test without replaying or verifying
        mockController.onTestEnd();
        mockController = null;
    }

    /**
     * A simple wrapper function for mockController.replay()
     *
     * @param easyMockedCreatedObjects any EasyMocks created via {@link org.easymock.EasyMock#createMock(Class)} that you also want
     *                                 to replay
     */
    final protected void replay(Object... easyMockedCreatedObjects)
    {
        mockController.replay(easyMockedCreatedObjects);
    }

    /**
     * A simple wrapper function for mockController.verify()
     *
     * @param easyMockedCreatedObjects any EasyMocks created via {@link org.easymock.EasyMock#createMock(Class)} that you also want
     *                                 to verify
     */
    final protected void verify(Object... easyMockedCreatedObjects)
    {
        mockController.verify(easyMockedCreatedObjects);
    }

    /**
     * A simple wrapper function for mockController.reset()
     *
     * @param easyMockedCreatedObjects any EasyMocks created via {@link org.easymock.EasyMock#createMock(Class)} that you also want
     *                                 to verify
     */
    final protected void reset(Object... easyMockedCreatedObjects)
    {
        mockController.reset(easyMockedCreatedObjects);
    }


    /**
     * A synonym for {@link com.atlassian.jira.mock.controller.MockController#instantiate(Class)}
     *
     * @param implementationClass the class under test
     *
     * @return an instance of the class under test
     */
    final protected <T> T instantiate(Class<T> implementationClass)
    {
        return mockController.instantiate(implementationClass);
    }

    /**
     * A synonym for {@link com.atlassian.jira.mock.controller.MockController#getMock(Class)}
     *
     * @param aClassToMock a class to mock out
     *
     * @return a mock instance of the class
     */
    final protected <T> T getMock(Class<T> aClassToMock)
    {
        return mockController.getMock(aClassToMock);
    }

    /**
     * A synonym for {@link com.atlassian.jira.mock.controller.MockController#getMock(Class)} that aligns with the name {@link org.easymock.EasyMock#createMock(Class)}
     *
     * @param aClassToMock a class to mock out
     *
     * @return a mock instance of the class
     */
    final protected <T> T createMock(Class<T> aClassToMock)
    {
        return mockController.getMock(aClassToMock);
    }

    /**
     * A synonym for {@link com.atlassian.jira.mock.controller.MockController#addObjectInstance(Object)}
     *
     * @param objectInstance the object instance to add into the mix
     *
     * @return that same objectInstance
     */
    @SuppressWarnings ({ "unchecked" })
    final protected <T> T addObjectInstance(final T objectInstance)
    {
        return mockController.addObjectInstance(objectInstance);
    }
    /*
     =========================================================
     Proxy calls to common EasyMock methods
     =========================================================
    */

    /**
     * Returns the expectation setter for the last expected invocation in the current thread.
     *
     * @param value the parameter is used to transport the type to the ExpectationSetter. It allows writing the expected
     *              call as argument, i.e. <code>expect(mock.getName()).andReturn("John Doe")<code>.
     *
     * @return the expectation setter.
     */
    final protected <T> IExpectationSetters<T> expect(T value)
    {
        return EasyMock.expect(value);
    }

    /**
     * Returns the expectation setter for the last expected invocation in the current thread. This method is used for
     * expected invocations on void methods.
     *
     * @return the expectation setter.
     */
    final protected <T> IExpectationSetters<T> expectLastCall()
    {
        return EasyMock.expectLastCall();
    }

    /**
     * Expects any boolean argument. For details, see the EasyMock documentation.
     *
     * @return <code>false</code>.
     */
    final protected boolean anyBoolean()
    {
        return EasyMock.anyBoolean();
    }

    /**
     * Expects any byte argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected byte anyByte()
    {
        return EasyMock.anyByte();
    }

    /**
     * Expects any char argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected char anyChar()
    {
        return EasyMock.anyChar();
    }

    /**
     * Expects any int argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected int anyInt()
    {
        return EasyMock.anyInt();
    }

    /**
     * Expects any long argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected long anyLong()
    {
        return EasyMock.anyLong();
    }

    /**
     * Expects any float argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected float anyFloat()
    {
        return EasyMock.anyFloat();
    }

    /**
     * Expects any double argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected double anyDouble()
    {
        return EasyMock.anyDouble();
    }

    /**
     * Expects any short argument. For details, see the EasyMock documentation.
     *
     * @return <code>0</code>.
     */
    final protected short anyShort()
    {
        return EasyMock.anyShort();
    }

    /**
     * Expects any String argument.
     *
     * @return <code>null</code>.
     */
    final protected String anyString()
    {
        return EasyMock.<String>anyObject();
    }

    /**
     * Expects any Object argument. For details, see the EasyMock documentation.
     *
     * @return <code>null</code>.
     */
    final protected <T> T anyObject()
    {
        return EasyMock.<T>anyObject();
    }

    /**
     * Expects a comparable argument greater than or equal the given value. For details, see the EasyMock
     * documentation.
     *
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T extends Comparable<T>> T geq(Comparable<T> value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects a byte argument greater than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected byte geq(byte value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects a double argument greater than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected double geq(double value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects a float argument greater than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected float geq(float value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects an int argument greater than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected int geq(int value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects a long argument greater than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected long geq(long value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects a short argument greater than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected short geq(short value)
    {
        return EasyMock.geq(value);
    }

    /**
     * Expects a comparable argument less than or equal the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T extends Comparable<T>> T leq(Comparable<T> value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects a byte argument less than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected byte leq(byte value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects a double argument less than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected double leq(double value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects a float argument less than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected float leq(float value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects an int argument less than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected int leq(int value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects a long argument less than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected long leq(long value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects a short argument less than or equal to the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected short leq(short value)
    {
        return EasyMock.leq(value);
    }

    /**
     * Expects a comparable argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T extends Comparable<T>> T gt(Comparable<T> value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects a byte argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected byte gt(byte value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects a double argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected double gt(double value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects a float argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected float gt(float value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects an int argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected int gt(int value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects a long argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected long gt(long value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects a short argument greater than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected short gt(short value)
    {
        return EasyMock.gt(value);
    }

    /**
     * Expects a comparable argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T extends Comparable<T>> T lt(Comparable<T> value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects a byte argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected byte lt(byte value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects a double argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected double lt(double value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects a float argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected float lt(float value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects an int argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected int lt(int value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects a long argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected long lt(long value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects a short argument less than the given value. For details, see the EasyMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected short lt(short value)
    {
        return EasyMock.lt(value);
    }

    /**
     * Expects an object implementing the given class. For details, see the EasyMock documentation.
     *
     * @param <T>   the accepted type.
     * @param clazz the class of the accepted type.
     *
     * @return <code>null</code>.
     */
    final protected <T> T isA(Class<T> clazz)
    {
        return EasyMock.isA(clazz);
    }

    /**
     * Expects a string that contains the given substring. For details, see the EasyMock documentation.
     *
     * @param substring the substring.
     *
     * @return <code>null</code>.
     */
    final protected String contains(String substring)
    {
        return EasyMock.contains(substring);
    }

    /**
     * Expects a boolean that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>false</code>.
     */
    final protected boolean and(boolean first, boolean second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a byte that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected byte and(byte first, byte second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a char that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected char and(char first, char second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a double that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected double and(double first, double second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a float that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected float and(float first, float second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects an int that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected int and(int first, int second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a long that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected long and(long first, long second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a short that matches both given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected short and(short first, short second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects an Object that matches both given expectations.
     *
     * @param <T>    the type of the object, it is passed through to prevent casts.
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>null</code>.
     */
    final protected <T> T and(T first, T second)
    {
        return EasyMock.and(first, second);
    }

    /**
     * Expects a boolean that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>false</code>.
     */
    final protected boolean or(boolean first, boolean second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a byte that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected byte or(byte first, byte second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a char that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected char or(char first, char second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a double that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected double or(double first, double second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a float that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected float or(float first, float second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects an int that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected int or(int first, int second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a long that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected long or(long first, long second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a short that matches one of the given expectations.
     *
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>0</code>.
     */
    final protected short or(short first, short second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects an Object that matches one of the given expectations.
     *
     * @param <T>    the type of the object, it is passed through to prevent casts.
     * @param first  placeholder for the first expectation.
     * @param second placeholder for the second expectation.
     *
     * @return <code>null</code>.
     */
    final protected <T> T or(T first, T second)
    {
        return EasyMock.or(first, second);
    }

    /**
     * Expects a boolean that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>false</code>.
     */
    final protected boolean not(boolean first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a byte that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected byte not(byte first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a char that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected char not(char first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a double that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected double not(double first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a float that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected float not(float first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects an int that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected int not(int first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a long that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected long not(long first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a short that does not match the given expectation.
     *
     * @param first placeholder for the expectation.
     *
     * @return <code>0</code>.
     */
    final protected short not(short first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects an Object that does not match the given expectation.
     *
     * @param <T>   the type of the object, it is passed through to prevent casts.
     * @param first placeholder for the expectation.
     *
     * @return <code>null</code>.
     */
    final protected <T> T not(T first)
    {
        return EasyMock.not(first);
    }

    /**
     * Expects a boolean that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected boolean eq(boolean value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a byte that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected byte eq(byte value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a char that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected char eq(char value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a double that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected double eq(double value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a float that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected float eq(float value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects an int that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected int eq(int value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a long that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected long eq(long value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a short that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>0</code>.
     */
    final protected short eq(short value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects an Object that is equal to the given value.
     *
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T> T eq(T value)
    {
        return EasyMock.eq(value);
    }

    /**
     * Expects a boolean array that is equal to the given array, i.e. it has to have the same length, and each element
     * has to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected boolean[] aryEq(boolean[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects a byte array that is equal to the given array, i.e. it has to have the same length, and each element has
     * to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected byte[] aryEq(byte[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects a char array that is equal to the given array, i.e. it has to have the same length, and each element has
     * to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected char[] aryEq(char[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects a double array that is equal to the given array, i.e. it has to have the same length, and each element
     * has to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected double[] aryEq(double[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects a float array that is equal to the given array, i.e. it has to have the same length, and each element has
     * to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected float[] aryEq(float[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects an int array that is equal to the given array, i.e. it has to have the same length, and each element has
     * to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected int[] aryEq(int[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects a long array that is equal to the given array, i.e. it has to have the same length, and each element has
     * to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected long[] aryEq(long[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects a short array that is equal to the given array, i.e. it has to have the same length, and each element has
     * to be equal.
     *
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected short[] aryEq(short[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects an Object array that is equal to the given array, i.e. it has to have the same type, length, and each
     * element has to be equal.
     *
     * @param <T>   the type of the array, it is passed through to prevent casts.
     * @param value the given arry.
     *
     * @return <code>null</code>.
     */
    final protected <T> T[] aryEq(T[] value)
    {
        return EasyMock.aryEq(value);
    }

    /**
     * Expects null.
     *
     * @return <code>null</code>.
     */
    final protected <T> T isNull()
    {
        return EasyMock.<T>isNull();
    }

    /**
     * Expects not null.
     *
     * @return <code>null</code>.
     */
    final protected <T> T notNull()
    {
        return EasyMock.<T>notNull();
    }

    /**
     * Expects a string that contains a substring that matches the given regular expression. For details, see the
     * EasyMock documentation.
     *
     * @param regex the regular expression.
     *
     * @return <code>null</code>.
     */
    final protected String find(String regex)
    {
        return EasyMock.find(regex);
    }

    /**
     * Expects a string that matches the given regular expression. For details, see the EasyMock documentation.
     *
     * @param regex the regular expression.
     *
     * @return <code>null</code>.
     */
    final protected String matches(String regex)
    {
        return EasyMock.matches(regex);
    }

    /**
     * Expects a string that starts with the given prefix. For details, see the EasyMock documentation.
     *
     * @param prefix the prefix.
     *
     * @return <code>null</code>.
     */
    final protected String startsWith(String prefix)
    {
        return EasyMock.startsWith(prefix);
    }

    /**
     * Expects a string that ends with the given suffix. For details, see the EasyMock documentation.
     *
     * @param suffix the suffix.
     *
     * @return <code>null</code>.
     */
    final protected String endsWith(String suffix)
    {
        return EasyMock.endsWith(suffix);
    }

    /**
     * Expects a double that has an absolute difference to the given value that is less than the given delta. For
     * details, see the EasyMock documentation.
     *
     * @param value the given value.
     * @param delta the given delta.
     *
     * @return <code>0</code>.
     */
    final protected double eq(double value, double delta)
    {
        return EasyMock.eq(value, delta);
    }

    /**
     * Expects a float that has an absolute difference to the given value that is less than the given delta. For
     * details, see the EasyMock documentation.
     *
     * @param value the given value.
     * @param delta the given delta.
     *
     * @return <code>0</code>.
     */
    final protected float eq(float value, float delta)
    {
        return EasyMock.eq(value, delta);
    }

    /**
     * Expects an Object that is the same as the given value. For details, see the EasyMock documentation.
     *
     * @param <T>   the type of the object, it is passed through to prevent casts.
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T> T same(T value)
    {
        return EasyMock.same(value);
    }

    /**
     * Expects a comparable argument equals to the given value according to their compareTo method. For details, see the
     * EasMock documentation.
     *
     * @param value the given value.
     *
     * @return <code>null</code>.
     */
    final protected <T extends Comparable<T>> T cmpEq(Comparable<T> value)
    {
        return EasyMock.cmpEq(value);
    }

    /**
     * Expects an argument that will be compared using the provided comparator. The following comparison will take
     * place: <p> <code>comparator.compare(actual, expected) operator 0</code> </p> For details, see the EasyMock
     * documentation.
     *
     * @param value      the given value.
     * @param comparator Comparator used to compare the actual with expected value.
     * @param operator   The comparison operator.
     *
     * @return <code>null</code>
     */
    final protected <T> T cmp(T value, Comparator<? super T> comparator, LogicalOperator operator)
    {
        return EasyMock.cmp(value, comparator, operator);
    }


    /**
     * Switches order checking of the given mock object (more exactly: the control of the mock object) the on and off.
     * For details, see the EasyMock documentation.
     *
     * @param mock  the mock object.
     * @param state <code>true</code> switches order checking on, <code>false</code> switches it off.
     */
    final protected void checkOrder(Object mock, boolean state)
    {
        EasyMock.checkOrder(mock, state);
    }

    /**
     * Factory for MockControllerTestCase mocks.
     */
    private class MockControllerFactory implements MockFactory
    {
        @Override
        public Object createMock(Mock mock, Class<?> mockClass)
        {
            if (mock.value() == MockType.STRICT)
            {
                return mockController.createStrictMock(mockClass);
            }

            if (mock.value() == MockType.NICE)
            {
                return mockController.createNiceMock(mockClass);
            }

            return mockController.createMock(mockClass);
        }
    }
}
