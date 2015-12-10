package com.atlassian.jira.mock.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.easymock.ArgumentsMatcher;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An builder to create an EasyMock {@link com.atlassian.jira.mock.matcher.ArgumentMatchers} that allows individual
 * arguments to be compared by different {@link com.atlassian.jira.mock.matcher.ArgumentMatcher} objects.
 * <p/>
 * Each builder has a default ArgumentMatcher. This matcher will be used for any arguments that do no have an associated
 * ArgumentMatcher.
 * <p/>
 * Consider: <tt> ArgumentsMatcherBuilder.newAlwaysBuilder().addDefaultMatcher().addComparableMatcher(String.class).addNaturalMatcher(Integer.class).asArgumentsMatcher()
 * </tt>
 * <p/>
 * This creates an {@link org.easymock.ArgumentsMatcher} that: <ul> <li>Validates the second argument to the method
 * using the String's {@link String#compareTo(String)} method.</li> <li>Validates the third argument to the method using
 * the Integer's {@link Integer#equals(Object)} method.</li> <li>Validates all other arguments using the default
 * matcher. In this case this will always be true, essentially ignoring all other arguments.</li>
 * <p/>
 * NOTE: The builder itself is not thread-safe, however, the ArgumentsMatcher returned from {@link
 * #asArgumentsMatcher()} is thread-safe.
 *
 * @deprecated This is not longer relevant under EasyMock 2.4.  
 *
 * @since v4.0
 */
@NotThreadSafe
@Deprecated
public final class ArgumentsMatcherBuilder
{
    private final List<Registration<?>> registrations = new ArrayList<Registration<?>>();
    private Registration<?> defaultRegistration = new Registration<Object>(Object.class, NaturalMatcher.naturalMatcher());

    /**
     * Create a new builder where the default matcher is a {@link ArgumentMatchers#naturalMatcher() natrual matcher}.
     *
     * @return the new builder.
     */
    public static ArgumentsMatcherBuilder newNaturalBuilder()
    {
        return new ArgumentsMatcherBuilder(Object.class, ArgumentMatchers.naturalMatcher());
    }

    /**
     * Create a new builder where the default matcher is a {@link ArgumentMatchers#alwaysMatcher() always matcher}.
     *
     * @return the new builder.
     */
    public static ArgumentsMatcherBuilder newAlwaysBuilder()
    {
        return new ArgumentsMatcherBuilder(Object.class, ArgumentMatchers.alwaysMatcher());
    }

    /**
     * Create a new builder with the passed {@link com.atlassian.jira.mock.matcher.ArgumentMatcher} as the default.
     *
     * @param type the class the default argument is expected to be. Normally only make sense if it is {@link Object}.
     * @param defaultMatcher the default matcher to use.
     * @param <T> the type that the default matcher will match.
     * @return the new builder.
     */
    public static <T> ArgumentsMatcherBuilder newBuilder(Class<T> type, ArgumentMatcher<? super T> defaultMatcher)
    {
        return new ArgumentsMatcherBuilder(type, defaultMatcher);
    }

    private <T> ArgumentsMatcherBuilder(Class<T> type, ArgumentMatcher<? super T> defaultMatcher)
    {
        defaultRegistration = new Registration<T>(notNull("type", type), notNull("defaultMatcher", defaultMatcher));
    }

    /**
     * Make the passed matcher handle the current argument.
     *
     * @param type the class of the argument.
     * @param matcher the matcher to handle the
     * @param <T> the type that the matcher should work with.
     * @return this object so that calls may be chained.
     */
    public <T> ArgumentsMatcherBuilder addArgumentMatcher(Class<T> type, ArgumentMatcher<? super T> matcher)
    {
        registrations.add(new Registration<T>(type, matcher));
        return this;
    }

    /**
     * Make the default matcher handle the current argument.
     *
     * @return this builder so that calls may be chained.
     */
    public ArgumentsMatcherBuilder addDefaultMatcher()
    {
        registrations.add(null);
        return this;
    }

    /**
     * Make the current argument match no matter its value.
     *
     * @param type the class of the argument.
     * @param <T> the type of argument.
     * @return this builder so that calls may be chained.
     */
    public <T> ArgumentsMatcherBuilder addAlwaysMatcher(Class<T> type)
    {
        return addArgumentMatcher(type, ArgumentMatchers.<T>alwaysMatcher());
    }

    /**
     * Make the current argument match no matter its value. Same as calling <tt>addAlwaysMatcher(Object.class)</tt>.
     *
     * @return this builder so that calls may be chained.
     */
    public ArgumentsMatcherBuilder addAlwaysMatcher()
    {
        return addAlwaysMatcher(Object.class);
    }

    /**
     * Make the current argument match using its natural equality, that is, its {@link Object#equals(Object) equals}
     * method.
     *
     * @param type the class of the argument.
     * @param <T> the type of argument.
     * @return this builder so that calls may be chained.
     */
    public <T> ArgumentsMatcherBuilder addNaturalMatcher(Class<T> type)
    {
        return addArgumentMatcher(type, ArgumentMatchers.<T>naturalMatcher());
    }

    /**
     * Make the current argument match using its natural equality, that is, its {@link Object#equals(Object) equals}
     * method. Equilavent to calling <tt>addNaturalMatcher(Object.class)</tt>.
     *
     * @return this builder so that calls may be chained.
     */
    public ArgumentsMatcherBuilder addNaturalMatcher()
    {
        return addNaturalMatcher(Object.class);
    }

    /**
     * Make the current {@link Comparable} argument match using the {@link Comparable#compareTo(Object)} method.
     *
     * @param type the class of the argument.
     * @param <T> the type of argument.
     * @return this builder so that calls may be chained.
     */
    public <T extends Comparable<? super T>> ArgumentsMatcherBuilder addComparableMatcher(Class<T> type)
    {
        return addArgumentMatcher(type, ArgumentMatchers.<T>comparableMatcher());
    }

    /**
     * Make the current argument match using the passed {@link java.util.Comparator}.
     *
     * @param type the class of the argument.
     * @param comparator the comparator that will be used to form the comparasion.
     * @param <T> the type of argument.
     * @return this builder so that calls may be chained.
     */
    public <T> ArgumentsMatcherBuilder addComparatorMatcher(Class<T> type, final Comparator<? super T> comparator)
    {
        return addArgumentMatcher(type, ArgumentMatchers.comparatorMatcher(comparator));
    }

    /**
     * Make the current argument match when it is not null.
     *
     * @param type the class of the argument.
     * @param <T> the type of the argument.
     * @return this builder so that calls may be chanined.
     */
    public <T> ArgumentsMatcherBuilder addNotNullMatcher(Class<T> type)
    {
        return addArgumentMatcher(type, ArgumentMatchers.notNullMatcher());
    }

    /**
     * Make the current argument match when it is not null. Equilavent to calling <tt>addNotNullMatcher(Object.class)</tt>.
     *
     * @return this builder so that calls may be chanined.
     */
    public ArgumentsMatcherBuilder addNotNullMatcher()
    {
        return addNotNullMatcher(Object.class);
    }

    /**
     * Make the passed matcher the default. The default matcher will be used for arguments that either have no
     * associated matcher, or for arguments that have been explicitly configured to use the default matcher through the
     * {@link #addDefaultMatcher()}  method.
     *
     * @param type the class of the argument.
     * @param matcher the default matcher.
     * @param <T> the type of the argument.
     * @return this builder so that it may be chained.
     */
    public <T> ArgumentsMatcherBuilder setDefaultMatcher(Class<T> type, ArgumentMatcher<? super T> matcher)
    {
        defaultRegistration = new Registration<T>(type, matcher);
        return this;
    }

    /**
     * Return the default matcher configured for this builder. This can be changed using the {@link #setDefaultMatcher(Class, ArgumentMatcher)}
     * method.
     *
     * @return the default matcher within this builder.
     */
    public ArgumentMatcher<?> getDefaultMatcher()
    {
        return defaultRegistration.getMatcher();
    }

    /**
     * Get the {@link com.atlassian.jira.mock.matcher.ArgumentMatcher} configured for the passed argument position.
     * The default matcher will be returned if passed position has no associated matcher.
     *
     * @param position the argument position.
     * @return the {@link com.atlassian.jira.mock.matcher.ArgumentMatcher} for that position.
     */
    public ArgumentMatcher<?> getMatcherForArgument(int position)
    {
        Registration<?> registration;
        if (position < registrations.size())
        {
            registration = registrations.get(position);
            if (registration == null)
            {
                registration = defaultRegistration;
            }
        }
        else
        {
            registration = defaultRegistration;
        }
        return registration.getMatcher();
    }

    /**
     * Create the arguments matcher built using this object. The matcher returned from this class is thread safe.
     *
     * @return the built arguments matcher.
     */
    public ArgumentsMatcher asArgumentsMatcher()
    {
        return new CompositeArgumentsMatcher(registrations, defaultRegistration);
    }

    @Override
    public String toString()
    {
        return String.format("Arguments Builder [Default Matcher: %s, Registrations: %s]", defaultRegistration, registrations);
    }

    /**
     * Implementation of the {@link ArgumentsMatcher} that matches each argument using a {@link
     * com.atlassian.jira.mock.matcher.ArgumentMatcher}. This is the ArgumentsMatcher that this builder returns.
     */
    @ThreadSafe
    private static class CompositeArgumentsMatcher implements ArgumentsMatcher
    {
        private final List<Registration<?>> registrations;
        private final Registration<?> defaultRegistration;

        private CompositeArgumentsMatcher(final List<Registration<?>> registrations, final Registration<?> defRegistration)
        {
            this.registrations = Collections.unmodifiableList(new ArrayList<Registration<?>>(registrations));
            this.defaultRegistration = defRegistration;
        }

        public boolean matches(final Object[] expected, final Object[] actual)
        {
            if (expected == actual)
            {
                return true;
            }
            else if (expected == null || actual == null)
            {
                return false;
            }
            else if (expected.length != actual.length)
            {
                return false;
            }

            boolean result = true;
            for (int currentPosition = 0; result && currentPosition < expected.length; currentPosition++)
            {
                Registration<?> registration = getRegistration(currentPosition);
                result = registration.match(expected[currentPosition], actual[currentPosition]);
            }

            return result;
        }

        public String toString(final Object[] objects)
        {
            StringBuilder builder = new StringBuilder("(");
            if (objects != null)
            {
                for (int i = 0; i < objects.length; i++)
                {
                    if (i > 0)
                    {
                        builder.append(", ");
                    }

                    builder.append(getRegistration(i).toString(objects[i]));
                }
            }
            return builder.append(")").toString();
        }

        @Override
        public String toString()
        {
            return String.format("Composite Arguments Matcher [Default Matcher: %s, Registrations: %s]", defaultRegistration, registrations);
        }

        private Registration<?> getRegistration(final int currentPosition)
        {
            Registration<?> registration;
            if (currentPosition < registrations.size())
            {
                registration = registrations.get(currentPosition);
                if (registration == null)
                {
                    registration = defaultRegistration;
                }
            }
            else
            {
                registration = defaultRegistration;
            }
            return registration;
        }
    }

    /**
     * Holds an assoication between class and matcher. The class can be used to make things type-safe.
     */
    private static class Registration<T>
    {
        private final Class<T> type;
        private final ArgumentMatcher<? super T> matcher;

        private Registration(final Class<T> type, final ArgumentMatcher<? super T> matcher)
        {
            this.type = notNull("type", type);
            this.matcher = notNull("matcher", matcher);
        }

        public Class<T> getType()
        {
            return type;
        }

        public ArgumentMatcher<? super T> getMatcher()
        {
            return matcher;
        }

        public boolean canHandle(Object object)
        {
            return object == null || type.isAssignableFrom(object.getClass());
        }

        public boolean match(Object expected, Object actual)
        {

            if (canHandle(expected) && canHandle(actual))
            {
                //we now know that expected instanceof T && actual instanceof T == true.
                @SuppressWarnings ({ "unchecked" }) final T tExpected = (T) expected;
                @SuppressWarnings ({ "unchecked" }) final T tActual = (T) actual;
                return matcher.match(tExpected, tActual);
            }
            else
            {
                return false;
            }
        }

        public String toString(Object object)
        {
            if (canHandle(object))
            {
                //we now know that object instanceof T == true.
                @SuppressWarnings ({ "unchecked" }) final T t = (T) object;
                return matcher.toString(t);
            }
            else
            {
                return String.valueOf(object);
            }
        }

        @Override
        public String toString()
        {
            return String.format("{Type %s: %s}", type.getName(), matcher);
        }
    }
}
