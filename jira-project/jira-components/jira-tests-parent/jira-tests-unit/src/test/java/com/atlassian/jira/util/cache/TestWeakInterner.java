package com.atlassian.jira.util.cache;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.util.cache.WeakInterner.InternReference;

import com.google.common.base.Objects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static com.atlassian.jira.util.cache.WeakInterner.newWeakInterner;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @since v6.3
 */
@SuppressWarnings ({ "RedundantStringConstructorCall", "QuestionableName", "ConstantConditions", "unchecked" })
public class TestWeakInterner
{
    private static final int ATTEMPTS = 1000;

    private WeakInterner<String> interner = newWeakInterner();

    @After
    public void tearDown()
    {
        interner = null;
    }

    @Test
    public void testSimpleInterning()
    {
        final String hello1 = new String("hello");
        final String hello2 = new String("hello");
        final String hello3 = new String("hello");
        final String world1 = new String("world");
        final String world2 = new String("world");
        final String world3 = new String("world");

        assertInternerState(interner);
        assertThat(interner.intern(hello1), sameInstance(hello1));
        assertInternerState(interner, live(hello1));
        assertThat(interner.internOrNull(hello2), sameInstance(hello1));
        assertThat(interner.intern(hello1), sameInstance(hello1));
        assertInternerState(interner, live(hello1));
        assertThat(interner.internOrNull(world2), sameInstance(world2));
        assertInternerState(interner, live(hello1), live(world2));
        assertThat(interner.intern(world1), sameInstance(world2));
        System.gc();  // Should not change anything
        assertThat(interner.intern(world2), sameInstance(world2));
        assertThat(interner.internOrNull(world3), sameInstance(world2));
        assertThat(interner.intern(hello3), sameInstance(hello1));
        assertInternerState(interner, live(hello1), live(world2));
    }


    // This is here to verify that weak references are processed in the expected way, meaning that
    // weak references are enqueued by the GC and cleared when interning a new value.  However, this
    // is an inherent race because we can see a null value before the interned reference is enqueued
    // for us to handle in cleanUp() and there are no strong guarantees about exactly when the GC
    // will do this, just that it will eventually get around to it.
    @Test
    public void testWeakness()
    {
        for (int i=1; i<=ATTEMPTS; ++i)
        {
            if (checkWeakness())
            {
                System.out.println("Saw full weak GC cycle on loop #" + i);
                return;
            }

            // Reset for the next attempt
            this.interner = newWeakInterner();
        }

        fail("Did not see automatic cleanUp() for dead keys even in " + ATTEMPTS + " attempts, which is suspicious.");
    }

    private boolean checkWeakness()
    {
        String s1 = new String("xyzzy");
        String s2 = new String("xyzzy");
        String s3 = new String("xyzzy");
        String s4 = new String("xyzzy");
        String world = new String("world");
        final WeakReference<String> ref = new WeakReference<String>(s1);

        assertInternerState(interner);
        assertThat(interner.intern(s1), sameInstance(s1));
        assertInternerState(interner, live(s1));
        assertThat(interner.intern(s4), sameInstance(s1));
        assertInternerState(interner, live(s1));
        assertThat(interner.intern(world), sameInstance(world));
        assertInternerState(interner, live(s1), live(world));

        //noinspection UnusedAssignment
        s4 = null;
        System.gc();  // Should not change anything

        assertThat(interner.internOrNull(s3), sameInstance(s1));
        assertInternerState(interner, live(s1), live(world));

        //noinspection UnusedAssignment
        s1 = null;
        gc(ref);

        // We should still have a key with the cleared weak reference
        assertInternerState(interner, dead(s3), live(world));

        assertThat("Interning after the original is cleared should intern the new value", interner.internOrNull(s2), sameInstance(s2));
        assertThat(interner.intern(s3), sameInstance(s2));

        if (interner.store.keySet().size() == 3)
        {
            // We were unfortunate enough to outrun the GC's enqueuing of the dead reference, so we should still see it
            assertInternerState(interner, live(s2), dead(s2), live(world));
            return false;
        }

        // The ideal case, where the dead reference was enqueued by the GC before we called intern, so cleanUp() was
        // able to remove it
        assertInternerState(interner, live(s2), live(world));
        return true;
    }

    @Test
    public void testInternOrNullWithNullArg()
    {
        assertThat(interner.internOrNull(null), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInternWithNullArg()
    {
        interner.intern(null);
    }

    private static void gc(WeakReference<String> ref)
    {
        for (int i=1; i<=10; ++i)
        {
            System.gc();
            if (ref.get() == null)
            {
                System.gc();  // Kick it one last time
                return;
            }
        }
        fail("Could not convince the GC to reclaim the weak reference.  Maybe somebody is unexpectedly still holding onto it?");
    }

    private static <T> void assertInternerState(WeakInterner<T> interner, Matcher<InternReference<T>>... keyMatchers)
    {
        assertThat(interner.store.keySet(), Matchers.<InternReference<T>>containsInAnyOrder(keyMatchers));
    }

    // Matcher that expects the instance given to be the interned value
    private static <T> RefMatcher<T> live(@Nonnull T expectedValue)
    {
        return new RefMatcher<T>(expectedValue, expectedValue.hashCode());
    }

    // Matcher that expects an interned entry with the same hash as the prototype value, but with its ref cleared
    private static <T> RefMatcher<T> dead(@Nonnull T prototypeValue)
    {
        return new RefMatcher<T>(null, prototypeValue.hashCode());
    }

    static class RefMatcher<T> extends TypeSafeMatcher<InternReference<T>>
    {
        final T expectedValue;
        final int expectedHashCode;

        RefMatcher(@Nullable final T expectedValue, final int expectedHashCode)
        {
            this.expectedValue = expectedValue;
            this.expectedHashCode = expectedHashCode;
        }

        @Override
        @SuppressWarnings("ObjectEquality")  // identity test is intentional
        protected boolean matchesSafely(final InternReference<T> item)
        {
            return item != null && item.hashCode() == expectedHashCode && expectedValue == item.get();
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("InternReference@(any)[hash=" + expectedHashCode + ",referent=" + expectedValue);
        }
    }
}
