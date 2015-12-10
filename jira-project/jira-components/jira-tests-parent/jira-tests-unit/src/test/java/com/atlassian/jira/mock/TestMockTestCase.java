package com.atlassian.jira.mock;

import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @since v4.0
 */
public class TestMockTestCase extends MockControllerTestCase
{
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
     * A pointless example of MockControllerTestCase in action.  No need for EasyMock.* to be statically imported
     * and the instatiation of the class under test is made easier. 
     */
    @Test
    public void testExample()
    {
        expect(getMock(Dog.class).stringDogCall(eq(666))).andReturn("I taught I taw a puddy tat?");
        getMock(Cat.class).voidCatCall();
        expectLastCall().asStub();

        final MenagerieImplementation menagerieImplementation = instantiate(MenagerieImplementation.class);
        assertNotNull(menagerieImplementation.stringDogCall(666));
        menagerieImplementation.voidCatCall();
    }
}
