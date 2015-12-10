package com.atlassian.jira.matchers.johnson;

import com.atlassian.johnson.event.Event;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Matchers for {@link com.atlassian.johnson.event.Event}.
 *
 * @since v5.1.6
 */
public final class JohnsonEventMatchers
{

    private JohnsonEventMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }


    public static Matcher<Event> hasKey(final String key)
    {
        notNull("key", key);
        return new TypeSafeDiagnosingMatcher<Event>()
        {
            @Override
            protected boolean matchesSafely(Event item, Description mismatchDescription)
            {
                return key.equals(item.getKey().getType());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("A Johnson Event with a key ").appendValue(key);
            }
        };
    }

    public static Matcher<Event> hasDescription(final String eventDescription)
    {
        notNull("description", eventDescription);
        return new TypeSafeDiagnosingMatcher<Event>()
        {
            @Override
            protected boolean matchesSafely(Event item, Description mismatchDescription)
            {
                if (!eventDescription.equals(item.getDesc()))
                {
                    mismatchDescription.appendValue("Expected description ").appendValue(eventDescription)
                            .appendText(" but was ").appendValue(item.getDesc());
                    return false;
                }
                else
                {
                    return true;
                }

            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("A Johnson Event with a description ").appendValue(eventDescription);
            }
        };
    }

    public static Matcher<Event> containsDescription(final String eventDescription)
       {
           notNull("description", eventDescription);
           return new TypeSafeDiagnosingMatcher<Event>()
           {
               @Override
               protected boolean matchesSafely(Event item, Description mismatchDescription)
               {
                   if (item.getDesc() == null || !item.getDesc().contains(eventDescription))
                   {
                       mismatchDescription.appendValue("Expected to contain description ").appendValue(eventDescription)
                               .appendText(" but was ").appendValue(item.getDesc());
                       return false;
                   }
                   else
                   {
                       return true;
                   }

               }

               @Override
               public void describeTo(Description description)
               {
                   description.appendText("A Johnson Event containing a description ").appendValue(eventDescription);
               }
           };
       }

    public static Matcher<Event> hasLevel(final String level)
    {
        notNull("level", level);
        return new TypeSafeDiagnosingMatcher<Event>()
        {
            @Override
            protected boolean matchesSafely(Event item, Description mismatchDescription)
            {
                return level.equals(item.getLevel().getLevel());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("A Johnson Event with a level ").appendValue(level);
            }
        };
    }
}
