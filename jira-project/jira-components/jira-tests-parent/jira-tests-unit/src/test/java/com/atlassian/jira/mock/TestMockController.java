package com.atlassian.jira.mock;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test class for {@link com.atlassian.jira.mock.controller.MockController}
 */
public class TestMockController
{
    private static final String GOOD_DOG = "Good Dog";
    private static final String DEAD_CAT = "Dead Cat";

    public interface Dog
    {
        void voidDogCall();

        String stringDogCall(int p1);
    }

    public interface Cat
    {
        void voidCatCall();

        String stringCatCall(int p1);
    }

    public interface Rat
    {
        void voidRatCall();

        String stringRatCall(int p1);
    }

    public static class Elephant
    {
        void voidElephantCall() {}

        String stringElephantCall(final int p1)
        {
            return "stringElephantCall(" + p1 + ")";
        }
    }

    public final static class Hippo
    {
        void voidHippoCall() { }

        String stringHippoCall(final int p2)
        {
            return String.format("stringHippoCall(%d)", p2);
        }
    }

    public static class MenagerieImplementation
    {
        private final Dog dog;
        private final Cat cat;
        private final Rat rat;
        private final Elephant elephant;
        private final Hippo hippo;

        public MenagerieImplementation(final Dog dog, final Cat cat)
        {
            this(dog, cat, null);
        }

        public MenagerieImplementation(final Dog dog, final Cat cat, final Rat rat)
        {
            this(dog, cat, rat, null);
        }

        public MenagerieImplementation(final Dog dog, final Cat cat, final Rat rat, final Elephant elephant)
        {
            this(dog, cat, rat, elephant, null);
        }

        public MenagerieImplementation(final Dog dog, final Cat cat, final Rat rat, final Elephant elephant, final Hippo hippo)
        {
            this.dog = dog;
            this.cat = cat;
            this.rat = rat;
            this.elephant = elephant;
            this.hippo = hippo;
        }

        public void voidDogCall()
        {
            dog.voidDogCall();
        }

        public String stringDogCall(final int p1)
        {
            return dog.stringDogCall(p1);
        }

        public void voidCatCall()
        {
            cat.voidCatCall();
        }

        public String stringCatCall(final int p1)
        {
            return cat.stringCatCall(p1);
        }

        public String stringElephantCall(final int p1)
        {
            return elephant.stringElephantCall(p1);
        }

        public String stringHippoCall(final int p2)
        {
            return hippo.stringHippoCall(p2);
        }

        public Dog getDog()
        {
            return dog;
        }

        public Cat getCat()
        {
            return cat;
        }

        public Rat getRat()
        {
            return rat;
        }

        public Elephant getElephant()
        {
            return elephant;
        }

        public Hippo getHippo()
        {
            return hippo;
        }
    }

    /**
     * This is an example of how to use MockController
     */
    @Test
    public void testUsageExample()
    {
        final MockController mockController = new MockController();
        final Dog dog = mockController.getMock(Dog.class);
        dog.stringDogCall(1);
        mockController.setReturnValue("dog");

        final Cat cat = mockController.getMock(Cat.class);
        cat.stringCatCall(2);
        mockController.setReturnValue("cat");

        final MenagerieImplementation menagery = mockController.instantiateAndReplay(MenagerieImplementation.class);
        assertEquals("dog", menagery.stringDogCall(1));
        assertEquals("cat", menagery.stringCatCall(2));

        mockController.verify();
    }

    @Test
    public void testGetMockWithInterfaces()
    {
        final MockController mockController = MockController.createStrictContoller();
        try
        {
            mockController.getMock(null);
            fail("Should have barfed");
        }
        catch (final IllegalArgumentException expected)
        {}

        Dog dog = mockController.getMock(Dog.class);
        assertNotNull(dog);
        assertTrue(dog instanceof Dog);
        final MockControl dogController = mockController.getCurrentMockControl();
        assertNotNull(dogController);


        dog.stringDogCall(1);
        assertSame(dogController, mockController.getCurrentMockControl());
        mockController.setReturnValue("dog");
        assertSame(dogController, mockController.getCurrentMockControl());

        Cat cat = mockController.getMock(Cat.class);
        assertNotNull(cat);
        final MockControl catController = mockController.getCurrentMockControl();
        assertNotNull(catController);
        assertNotSame(dogController, catController);

        cat.stringCatCall(2);
        assertSame(catController, mockController.getCurrentMockControl());
        assertNotSame(dogController, mockController.getCurrentMockControl());
        mockController.setReturnValue("cat");
        assertSame(catController, mockController.getCurrentMockControl());
        assertNotSame(dogController, mockController.getCurrentMockControl());

        dog.voidDogCall();
        assertSame(dogController, mockController.getCurrentMockControl());
        assertNotSame(catController, mockController.getCurrentMockControl());

        cat.voidCatCall();
        assertSame(catController, mockController.getCurrentMockControl());
        assertNotSame(dogController, mockController.getCurrentMockControl());

        // what are interfaces in play
        final List<Class<?>> types = mockController.getMockedTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        assertTrue(types.contains(Dog.class));
        assertTrue(types.contains(Cat.class));

        // what objects do we have in play
        final List<Object> mockedObjects = mockController.getMockedObjects();
        assertNotNull(mockedObjects);
        assertEquals(2, mockedObjects.size());

        // what mock controllers do we have in play
        final List<MockControl> mockControllers = mockController.getMockControls();
        assertNotNull(mockControllers);
        assertEquals(2, mockControllers.size());
        assertTrue(mockControllers.contains(dogController));
        assertTrue(mockControllers.contains(catController));
    }

    @Test
    public void testGetMockWithClasses()
    {
        final MockController mockController = MockController.createStrictContoller();
        try
        {
            mockController.getMock(Hippo.class);
            fail("Should have barfed for final class.");
        }
        catch (final IllegalArgumentException expected)
        {}

        Dog dog = mockController.getMock(Dog.class);
        assertNotNull(dog);
        assertTrue(dog instanceof Dog);
        final MockControl dogController = mockController.getCurrentMockControl();
        assertNotNull(dogController);

        dog.stringDogCall(1);
        assertSame(dogController, mockController.getCurrentMockControl());
        mockController.setReturnValue("dog");
        assertSame(dogController, mockController.getCurrentMockControl());

        Elephant elephant = mockController.getMock(Elephant.class);

        assertNotNull(elephant);
        final MockControl elephantController = mockController.getCurrentMockControl();
        assertNotNull(elephantController);
        assertNotSame(dogController, elephantController);

        elephant.stringElephantCall(4);
        assertSame(elephantController, mockController.getCurrentMockControl());
        assertNotSame(dogController, mockController.getCurrentMockControl());
        mockController.setReturnValue("african elefant");
        assertSame(elephantController, mockController.getCurrentMockControl());
        assertNotSame(dogController, mockController.getCurrentMockControl());

        dog.voidDogCall();
        assertSame(dogController, mockController.getCurrentMockControl());
        assertNotSame(elephantController, mockController.getCurrentMockControl());

        elephant.voidElephantCall();
        assertSame(elephantController, mockController.getCurrentMockControl());
        assertNotSame(dogController, mockController.getCurrentMockControl());

        // what are interfaces in play
        final List<Class<?>> types = mockController.getMockedTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        assertTrue(types.contains(Dog.class));
        assertTrue(types.contains(Elephant.class));

        // what objects do we have in play
        final List<Object> mockedObjects = mockController.getMockedObjects();
        assertNotNull(mockedObjects);
        assertEquals(2, mockedObjects.size());

        // what mock controllers do we have in play
        final List<MockControl> mockControllers = mockController.getMockControls();
        assertNotNull(mockControllers);
        assertEquals(2, mockControllers.size());
        assertTrue(mockControllers.contains(dogController));
        assertTrue(mockControllers.contains(elephantController));
    }


    @Test
    public void testOnTestEnd()
    {
        MockController mockController = new MockController();
        // this should work if we are in START mode.  It doesnt get finicky until
        // you have actualy done some mocking
        mockController.onTestEnd();

        try
        {
            mockController.getMock(Dog.class);
            mockController.onTestEnd();
            fail("Should have throw an IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {}

        mockController.replay();
        assertEquals(MockController.ControllerState.REPLAYED, mockController.getState());
        mockController.onTestEnd();
        assertEquals(MockController.ControllerState.VERIFIED, mockController.getState());

        mockController = new MockController();
        mockController.replay();
        mockController.verify();
        assertEquals(MockController.ControllerState.VERIFIED, mockController.getState());
        mockController.onTestEnd();
        assertEquals(MockController.ControllerState.VERIFIED, mockController.getState());
    }

    @Test
    public void testStateManagement()
    {
        final MockController mockController = new MockController();
        final Dog dog = mockController.getMock(Dog.class);
        final Cat cat = mockController.getMock(Cat.class);

        try
        {
            mockController.setReturnValue(123);
            fail("Should have thrown exception because of no last method and hence no controller in play");
        }
        catch (final IllegalStateException expected)
        {}
        dog.stringDogCall(1);
        // multiple calls to the controller is allowed since EasyMock allows it!
        mockController.setReturnValue("stringDogCall1");
        mockController.setReturnValue("stringDogCall2");

        try
        {
            mockController.verify();
            fail("Not in replay state");
        }
        catch (final Exception expected)
        {

        }

        mockController.replay();
        try
        {
            mockController.replay();
            fail("Already in replay state");
        }
        catch (final Exception expected)
        {}

        String returnValue = dog.stringDogCall(1);
        assertEquals("stringDogCall1", returnValue);

        // we can call verify all we like
        try
        {
            mockController.verify();
            fail("should have complained about the lack of a second call to method");
        }
        catch (final AssertionError expected)
        {}

        try
        {
            cat.stringCatCall(3);
            fail("Should have complained about bad parameters.");
        }
        catch (final AssertionError expected)
        {}
        returnValue = dog.stringDogCall(1);
        assertEquals("stringDogCall2", returnValue);

        mockController.verify();

        try
        {
            mockController.replay();
            fail("Just to be sure we are in already in replay state");
        }
        catch (final Exception expected)
        {}

        // we can reset as many times as we like
        mockController.reset();
        mockController.reset();

        mockController.replay();
    }

    @Test
    public void testInstantiationFailure()
    {
        final MockController mockController = new MockController();

        final Dog dog = mockController.getMock(Dog.class);
        dog.stringDogCall(1);
        mockController.setReturnValue("stringDogCall(1)");
        try
        {
            mockController.instantiateAndReplay(MenagerieImplementation.class);
            fail("Should not be able to satisfy dependencies yet since we haven't asked for a cat");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testInstantiationSucess()
    {
        final MockController mockController = new MockController();
        MenagerieImplementation menagery;

        final Dog dog = mockController.getMock(Dog.class);
        dog.stringDogCall(1);
        mockController.setReturnValue("stringDogCall(1)");

        final Cat cat = mockController.getMock(Cat.class);
        cat.stringCatCall(2);
        mockController.setReturnValue("stringCatCall(2)");

        menagery = mockController.instantiateAndReplay(MenagerieImplementation.class);
        assertEquals("stringDogCall(1)", menagery.stringDogCall(1));
        assertEquals("stringCatCall(2)", menagery.stringCatCall(2));

        mockController.verify();
    }

    /**
     * Check the nice object creation. The object should automatically create the necessary mocks.
     */
    @Test
    public void testNiceInstantiation()
    {
        final MockController mockController = new MockController();
        final MenagerieImplementation menagery = mockController.instantiate(MenagerieImplementation.class);

        assertNotNull(menagery);
        assertNotNull(menagery.getCat());
        assertNotNull(menagery.getDog());
        assertNotNull(menagery.getRat());

        mockController.verify();
    }

    /**
     * Check the nice object creation. Make sure the user can make mock assertions.
     */
    @Test
    public void testNiceInstantiationWithAssertions()
    {
        final MockController mockController = new MockController();
        final Dog expectedDog = mockController.getMock(Dog.class);

        assertNotNull(expectedDog);

        //make an assertion.
        expectedDog.stringDogCall(1);
        mockController.setReturnValue(GOOD_DOG);

        final MenagerieImplementation menagery = mockController.instantiate(MenagerieImplementation.class);

        assertNotNull(menagery);
        assertNotNull(menagery.getCat());
        assertSame(expectedDog, menagery.getDog());
        assertNotNull(menagery.getRat());

        final String actualResult = menagery.stringDogCall(1);
        assertEquals(GOOD_DOG, actualResult);

        mockController.verify();
    }

    /**
     * Check the nice object creation. The object should automatically create the necessary mocks.
     *
     * @throws Exception when a test error occurs.
     */
    @Test
    public void testNiceConstructorInstantiation() throws Exception
    {
        final Constructor<MenagerieImplementation> constructor = MenagerieImplementation.class.getConstructor(new Class[] { Dog.class, Cat.class });
        final MockController mockController = new MockController();
        final MenagerieImplementation menagery = mockController.instantiateAndReplayNice(MenagerieImplementation.class, constructor);

        assertNotNull(menagery);
        assertNotNull(menagery.getCat());
        assertNotNull(menagery.getDog());
        assertNull(menagery.getRat());

        mockController.verify();
    }

    /**
     * Check the nice object creation. Make sure we can make assertions.
     *
     * @throws Exception when a test error occurs.
     */
    @Test
    public void testNiceConstructorInstantiationWithAssertions() throws Exception
    {
        final Constructor<MenagerieImplementation> constructor = MenagerieImplementation.class.getConstructor(new Class[] { Dog.class, Cat.class });

        final MockController mockController = new MockController();
        final Cat expectedCat = mockController.getMock(Cat.class);

        assertNotNull(expectedCat);

        //make an assertion.
        expectedCat.stringCatCall(10);
        mockController.setReturnValue(DEAD_CAT);

        final MenagerieImplementation menagery = mockController.instantiateAndReplayNice(MenagerieImplementation.class, constructor);

        assertNotNull(menagery);
        assertSame(expectedCat, menagery.getCat());
        assertNotNull(menagery.getDog());
        assertNull(menagery.getRat());

        final String actualResult = menagery.stringCatCall(10);
        assertEquals(DEAD_CAT, actualResult);

        mockController.verify();
    }

    @Test
    public void testAddingObjectInstances() throws Exception
    {
        MenagerieImplementation menagery;
        MockController mockController;
        final Hippo hippo = new Hippo();
        final Constructor<MenagerieImplementation> constructorWithHippo = MenagerieImplementation.class.getConstructor(new Class[] { Dog.class, Cat.class, Rat.class, Elephant.class, Hippo.class });

        //
        // test it gives the same objects back
        final List<Object> expectedList = new ArrayList<Object>(CollectionBuilder.newBuilder("A String", 123L, new StringBuffer("a StringBuffer")).asList());

        mockController = new MockController();
        for (final Object instanceObj : expectedList)
        {
            final Object returnedObj = mockController.addObjectInstance(instanceObj);
            assertSame(instanceObj, returnedObj);
        }
        final List<Object> actualList = mockController.getObjectInstances();
        assertEquals(expectedList, actualList);

        //
        // try without making an elephant available
        try
        {
            mockController = new MockController();
            mockController.instantiateAndReplayNice(MenagerieImplementation.class, constructorWithHippo);
            fail("Should have failed because there is no hippos available");
        }
        catch (final IllegalStateException ignored)
        {}

        // now try with the elephant added to the mix
        mockController = new MockController();
        mockController.addObjectInstance(hippo);
        menagery = mockController.instantiateAndReplayNice(MenagerieImplementation.class, constructorWithHippo);
        assertNotNull(menagery);
        assertNotNull(menagery.getElephant());
        assertEquals("stringHippoCall(123)", menagery.stringHippoCall(123));
        mockController.verify();

        //
        // now try one that is instantiated without a constructor
        mockController = new MockController();
        mockController.addObjectInstance(hippo);
        menagery = mockController.instantiate(MenagerieImplementation.class);
        assertNotNull(menagery);
        assertNotNull(menagery.getElephant());
        assertEquals("stringHippoCall(456)", menagery.stringHippoCall(456));
        mockController.verify();

        //
        // now try one via the stricter version of instantiateAndReply but without all the mocks needed
        mockController = new MockController();
        mockController.addObjectInstance(hippo);
        try
        {
            mockController.instantiateAndReplay(MenagerieImplementation.class);
            fail("Should have complained about missing resources");
        }
        catch (final IllegalStateException ignored)
        {}

        //
        // now try one via the stricter version of instantiateAndReply when we do have all the classes
        mockController = new MockController();
        mockController.addObjectInstance(hippo);
        mockController.getMock(Dog.class);
        mockController.getMock(Cat.class);
        mockController.getMock(Rat.class);
        mockController.getMock(Elephant.class);
        menagery = mockController.instantiateAndReplay(MenagerieImplementation.class);
        assertNotNull(menagery);
        assertNotNull(menagery.getElephant());
        assertEquals("stringHippoCall(789)", menagery.stringHippoCall(789));
        mockController.verify();
    }

    @Test
    public void testNoMocksCreated_NoInstatiation() throws Exception
    {
        assertEquals(true,true);
    }

    @Test
    public void testNoMocksCreated_Instatiation() throws Exception
    {
        MockController mockController = new MockController();
        mockController.instantiate(MenagerieImplementation.class);
        assertEquals(true,true);
    }
}
