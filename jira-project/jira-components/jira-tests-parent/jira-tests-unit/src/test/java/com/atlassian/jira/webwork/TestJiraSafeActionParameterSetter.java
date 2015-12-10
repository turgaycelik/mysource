package com.atlassian.jira.webwork;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.action.SafeAction;

import org.junit.Test;

import webwork.action.Action;
import webwork.action.IllegalArgumentAware;
import webwork.util.editor.PropertyEditorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test for JiraSafeActionParameterSetter
 *
 * @since v3.13.2
 */
public class TestJiraSafeActionParameterSetter
{
    private class AssertingAction implements Action
    {
        AtomicInteger goodSetterCount = new AtomicInteger(0);

        public String execute() throws Exception
        {
            return null;
        }
    }

    private class NoValidSetters extends AssertingAction
    {
        /** @noinspection UnusedDeclaration*/
        public void setSomething(final Map someMap)
        {
            fail("this should never be invoked");
        }
    }

    private class MixedSetters extends NoValidSetters
    {
        String name;
        Long id;
        String[] counsins;

        public void setName(final String name)
        {
            this.name = name;
            goodSetterCount.incrementAndGet();
        }

        public void setId(final Long id)
        {
            this.id = id;
            goodSetterCount.incrementAndGet();
        }

        public void setCousins(final String[] counsins)
        {
            this.counsins = counsins;
            goodSetterCount.incrementAndGet();
        }
    }

    @Test
    public void testNullParameters()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        try
        {
            // we dont accept null actions
            setter.setSafeParameters(null, new HashMap());
        }
        catch (final IllegalArgumentException ignored)
        {}

        class SafeActionimpl implements Action, SafeAction
        {
            public String execute() throws Exception
            {
                return null;
            }
        }
        try
        {
            // we dont accept SafeAction to this setter
            setter.setSafeParameters(new SafeActionimpl(), new HashMap());
        }
        catch (final IllegalArgumentException ignored)
        {}

        final MixedSetters action = new MixedSetters();
        setter.setSafeParameters(action, null);
        assertEquals(0, action.goodSetterCount.get());
    }

    @Test
    public void testNoParameters()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();

        final MixedSetters action = new MixedSetters();
        setter.setSafeParameters(action, new HashMap());
        assertEquals(0, action.goodSetterCount.get());
    }

    @Test
    public void testNoSetters()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        final Map parameters = EasyMap.build("name", "brad");

        final NoValidSetters action = new NoValidSetters();
        setter.setSafeParameters(action, parameters);
        assertEquals(0, action.goodSetterCount.get());
    }

    @Test
    public void testNoMatchingSetter()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        final Map parameters = new ParameterMapBuilder("wontmatch", "anything").toMap();

        final MixedSetters action = new MixedSetters();
        setter.setSafeParameters(action, parameters);
        assertEquals(0, action.goodSetterCount.get());
    }

    @Test
    public void testNullInput()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        final Map parameters = EasyMap.build("name", null);

        final MixedSetters action = new MixedSetters();
        setter.setSafeParameters(action, parameters);
        assertEquals(0, action.goodSetterCount.get());
        assertNull(action.name);
        assertNull(action.id);
    }

    @Test
    public void testSomeMatchingSomeNot()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        final Map parameters = new ParameterMapBuilder().add("wont", "match").add("name", "brad").toMap();

        final MixedSetters action = new MixedSetters();
        setter.setSafeParameters(action, parameters);
        assertEquals(1, action.goodSetterCount.get());
        assertEquals("brad", action.name);
        assertNull(action.id);
    }

    @Test
    public void testSomeBasicTypes()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        final Map parameters = new ParameterMapBuilder().add("name", "brad").add("id", "100").add("cousins", "anton").add("cousins", "shaun").add(
            "cousins", "damon").add("cousins", "trent").add("cousins", "jarrod").toMap();

        final MixedSetters action = new MixedSetters();
        setter.setSafeParameters(action, parameters);
        assertEquals(3, action.goodSetterCount.get());
        assertEquals("brad", action.name);
        assertEquals(new Long(100), action.id);
        assertTrue(Arrays.equals(new String[] { "anton", "shaun", "damon", "trent", "jarrod" }, action.counsins));
    }

    @Test
    public void testIllegalArgumentExceptions()
    {
        final JiraSafeActionParameterSetter setter = new JiraSafeActionParameterSetter();
        Map parameters = new ParameterMapBuilder().add("name", "brad").add("id", "100xyz").toMap();

        final MixedSetters action = new MixedSetters();
        try
        {
            setter.setSafeParameters(action, parameters);
            fail("Should have barfed");
        }
        catch (final PropertyEditorException ignored)
        {}

        class AwareAction extends MixedSetters implements IllegalArgumentAware
        {
            AtomicInteger argumentsAdded = new AtomicInteger(0);
            private long age;

            public void addIllegalArgumentException(final String fieldName, final IllegalArgumentException e)
            {
                argumentsAdded.incrementAndGet();
            }

            public void setAge(final long age)
            {
                this.age = age;
            }
        }

        parameters = new ParameterMapBuilder().add("name", "brad").add("id", "100xyz").add("age", "").toMap();

        final AwareAction awareAction = new AwareAction();
        setter.setSafeParameters(awareAction, parameters);
        assertEquals(2, awareAction.argumentsAdded.get());

    }

}
