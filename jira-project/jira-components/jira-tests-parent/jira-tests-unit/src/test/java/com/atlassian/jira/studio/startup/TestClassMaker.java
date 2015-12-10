package com.atlassian.jira.studio.startup;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4.1
 */
public class TestClassMaker
{
    @Test
    public void testGoodCreateClass() throws Exception
    {
        ClassMaker<TestClass> maker = new ClassMaker<TestClass>(TestClass.class, getClass().getClassLoader());
        TestClass instance = maker.createInstance(ClassOne.class.getName());
        assertNotNull(instance);
        assertTrue(instance instanceof ClassOne);
    }

    @Test
    public void testBadClassType() throws Exception
    {
        ClassMaker<TestClass> maker = new ClassMaker<TestClass>(TestClass.class, getClass().getClassLoader());
        TestClass instance = maker.createInstance(ClassTwo.class.getName());
        assertNull(instance);
    }

    @Test
    public void testNoConstructor() throws Exception
    {
        ClassMaker<TestClass> maker = new ClassMaker<TestClass>(TestClass.class, getClass().getClassLoader());
        TestClass instance = maker.createInstance(ClassNoConstructor.class.getName());
        assertNull(instance);
    }

    @Test
    public void testHiddenConstructor() throws Exception
    {
        ClassMaker<TestClass> maker = new ClassMaker<TestClass>(TestClass.class, getClass().getClassLoader());
        TestClass instance = maker.createInstance(HiddenClass.class.getName());
        assertNull(instance);
    }

    public interface TestClass
    {
    }

    public static class ClassOne implements TestClass
    {
    }

    public static class ClassTwo
    {
    }

    public static class ClassNoConstructor implements TestClass
    {
        public ClassNoConstructor(String arg)
        {
        }
    }

    private static class HiddenClass implements TestClass
    {
    }
}
