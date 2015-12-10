package com.atlassian.jira.functest.framework.matchers;

import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Matchers for DOM4J documents.
 *
 * @since 6.0
 */
public final class DocumentMatchers
{

    private DocumentMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }

    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Don't clone me");
    }

    public static Matcher<Element> elementWithName(final String name)
    {
        return new TypeSafeDiagnosingMatcher<Element>()
        {
            @Override
            protected boolean matchesSafely(final Element item, final Description mismatchDescription)
            {
                if (!name.equals(item.getName()))
                {
                    mismatchDescription.appendText("Expected ").appendValue(name).appendText(" but was ")
                            .appendValue(item.getName());
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("An element with name ").appendValue(name);
            }
        };
    }

    public static Matcher<Comment> commentWithText(final String text)
    {
        notNull(text);
        return new TypeSafeDiagnosingMatcher<Comment>()
        {
            @Override
            protected boolean matchesSafely(final Comment comment, final Description description)
            {
                final boolean result = text.equals(comment.getText());
                if (!result)
                {
                    description.appendText("Expected ").appendValue(text).appendText(" but was ").appendValue(comment.getText());
                }
                return result;
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("A Comment with text ").appendText(text);
            }
        };
    }

    public static Matcher<Document> documentWithNodes(final Class<? extends Node>... nodeTypes)
    {
        return new TypeSafeDiagnosingMatcher<Document>()
        {
            @Override
            protected boolean matchesSafely(final Document item, final Description mismatchDescription)
            {
                if (item.content().size() < nodeTypes.length)
                {
                    mismatchDescription.appendText("Expected at least ").appendValue(nodeTypes.length)
                            .appendText(" nodes, but the document has only ").appendValue(item.content().size());
                }
                for (int i = 0; i<nodeTypes.length; i++)
                {
                    if (!nodeTypes[i].isInstance(item.content().get(i)))
                    {
                        mismatchDescription.appendText("Expected type ").appendValue(nodeTypes[i]).appendText(" at position")
                                .appendValue(i).appendText(" but was ").appendValue(item.content().get(i).getClass());
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("A document with the following nodes (in order): ")
                        .appendValueList("[", ",", "]", nodeTypes);
            }
        };
    }

    public static <N extends Node> Matcher<Document> hasNodeAt(final int position, final Class<N> nodeType,
            final Matcher<N> nodeMatcher)
    {
        return new TypeSafeDiagnosingMatcher<Document>()
        {
            @Override
            protected boolean matchesSafely(final Document item, final Description mismatchDescription)
            {
                if (item.content().size() <= position)
                {
                    mismatchDescription.appendText("Expected at least ").appendValue(position)
                            .appendText(" nodes, but the document has only ").appendValue(item.content().size());
                    return false;
                }
                final Object node = item.content().get(position);
                if (!nodeType.isInstance(node))
                {
                    mismatchDescription.appendText("Expected type ").appendValue(nodeType).appendText(" at position ")
                            .appendValue(position).appendText(" but was ").appendValue(node.getClass());
                    return false;
                }
                final boolean result = nodeMatcher.matches(node);
                if (!result)
                {
                    nodeMatcher.describeMismatch(node, mismatchDescription);
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("A document with the following node at position ")
                        .appendValue(position).appendText(": ").appendDescriptionOf(nodeMatcher);
            }
        };
    }

}
