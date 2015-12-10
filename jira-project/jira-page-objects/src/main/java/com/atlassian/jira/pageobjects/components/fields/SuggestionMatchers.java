package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.framework.fields.HasId;
import com.atlassian.jira.util.dbc.Assertions;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

/**
 * Matchers for suggestions.
 *
 * @since v4.4
 */
public class SuggestionMatchers
{

    public static Matcher<MultiSelectSuggestion> hasText(final String text)
    {
        return new BaseMatcher<MultiSelectSuggestion>()
        {
            @Override
            public boolean matches(Object obj)
            {
                MultiSelectSuggestion suggestion = (MultiSelectSuggestion) obj;
                return suggestion.getText().now().equals(text);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion with text ").appendValue(text).toString();
            }
        };
    }

    public static Matcher<MultiSelectSuggestion> hasMainLabel(final String mainLabel)
    {
        return new BaseMatcher<MultiSelectSuggestion>()
        {
            @Override
            public boolean matches(Object obj)
            {
                MultiSelectSuggestion suggestion = (MultiSelectSuggestion) obj;
                return suggestion.getMainLabel().equals(mainLabel);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion with main label ").appendValue(mainLabel).toString();
            }
        };
    }

    public static Matcher<MultiSelectSuggestion> hasLabels(final String mainLabel, final String alias)
    {
        return new BaseMatcher<MultiSelectSuggestion>()
        {
            @Override
            public boolean matches(Object obj)
            {
                MultiSelectSuggestion suggestion = (MultiSelectSuggestion) obj;
                return suggestion.getMainLabel().equals(mainLabel) && suggestion.getAliasLabel().equals(alias);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion with main label ").appendValue(mainLabel)
                        .appendText(" and alias ").appendValue(alias).toString();
            }
        };
    }

    public static Matcher<Iterable<MultiSelectSuggestion>> hasNumberSuggestions(final Integer numberSuggestions)
    {
        return new BaseMatcher<Iterable<MultiSelectSuggestion>>()
        {

            @Override
            public boolean matches(final Object obj)
            {
                @SuppressWarnings ( { "unchecked" }) Iterable<MultiSelectSuggestion> suggestions = (Iterable) obj;
                int count = 0;
                for (final MultiSelectSuggestion suggestion : suggestions)
                {
                    ++count;
                }
                return count == numberSuggestions;
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Suggestion list contains ").appendValue(numberSuggestions).appendText(" suggestions").toString();
            }
        };
    }



    public static Matcher<List<MultiSelectSuggestion>> hasNoMatches()
    {
        return new BaseMatcher<List<MultiSelectSuggestion>>()
        {
            @Override
            public boolean matches(Object obj)
            {
                @SuppressWarnings ( { "unchecked" }) List<MultiSelectSuggestion> suggestions = (List) obj;
                return suggestions.size() == 1 && suggestions.get(0).getText().now().equals("No Matches");
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion list with no matches ").toString();
            }
        };
    }

    public static Matcher<Iterable<MultiSelectSuggestion>> containsSuggestion(final String mainLabel)
    {
        return new BaseMatcher<Iterable<MultiSelectSuggestion>>()
        {
            @Override
            public boolean matches(Object obj)
            {
                @SuppressWarnings ( { "unchecked" }) Iterable<MultiSelectSuggestion> suggestions = (Iterable) obj;
                for (MultiSelectSuggestion suggestion : suggestions)
                {
                    if (hasMainLabel(mainLabel).matches(suggestion))
                    {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion list contains suggestion with main label ").appendValue(mainLabel)
                        .toString();
            }
        };
    }

    public static Matcher<Iterable<MultiSelectSuggestion>> containsSuggestion(final String mainLabel, final String alias)
    {
        return new BaseMatcher<Iterable<MultiSelectSuggestion>>()
        {
            @Override
            public boolean matches(Object obj)
            {
                @SuppressWarnings ( { "unchecked" }) Iterable<MultiSelectSuggestion> suggestions = (Iterable) obj;
                for (MultiSelectSuggestion suggestion : suggestions)
                {
                    if (hasLabels(mainLabel, alias).matches(suggestion))
                    {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion list contains suggestion with main label ").appendValue(mainLabel)
                        .appendText(" and alias ").appendValue(alias).toString();
            }
        };
    }

    public static Matcher<Suggestion> containsSubstring(final String substring)
    {
        return new TypeSafeMatcher<Suggestion>()
        {
            @Override
            public boolean matchesSafely(Suggestion item)
            {
                final String text = item.getText().now();
                return text != null && text.contains(substring);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a suggestion with substring ").appendValue(substring);
            }
        };
    }

    public static Matcher<Suggestion> isActive()
    {
        return isActive(Suggestion.class);
    }

    public static <S extends Suggestion> Matcher<S> isActive(final Class<S> targetType)
    {
        Assertions.notNull("targetType", targetType);
        return new TypeSafeMatcher<S>()
        {
            @Override
            public boolean matchesSafely(S item)
            {
                return item.isActive().now();
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("an active suggestion of type ");
            }
        };
    }

    public static <E extends HasId> Matcher<E> idContainsSubstring(final String idSubstring, Class<E> targetType)
    {
        Assertions.notNull("idSubstring", idSubstring);
        return new TypeSafeMatcher<E>()
        {
            @Override
            public boolean matchesSafely(E item)
            {
                String id = item.getId().now();
                return id != null && id.contains(idSubstring);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("An element that contains ID substring ").appendValue(idSubstring);
            }
        };
    }

    public static <E extends HasId> Matcher<E> hasId(final String id, Class<E> targetType)
    {
        Assertions.notNull("id", id);
        return new TypeSafeMatcher<E>()
        {
            @Override
            public boolean matchesSafely(E item)
            {
                return id.equals(item.getId().now());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("An element that has ID ").appendValue(id);
            }
        };
    }

    public static Matcher<HasId> hasId(final String id)
    {
        Assertions.notNull("id", id);
        return new TypeSafeMatcher<HasId>()
        {
            @Override
            public boolean matchesSafely(HasId item)
            {
                return id.equals(item.getId().now());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("An element that has ID ").appendValue(id);
            }
        };
    }


}
