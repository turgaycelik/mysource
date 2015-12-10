package com.atlassian.jira.functest.matcher;

import com.atlassian.jira.util.DomFactory;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Document;

import java.io.InputStream;
import javax.annotation.Nullable;

/**
 * Template class for matchers that work on DOM documents.
 *
 * @since v5.0
 */
public abstract class DocumentMatcher extends TypeSafeMatcher<InputStream>
{
    @Override
    final public boolean matchesSafely(InputStream inputStream)
    {
        try
        {
            if (inputStream == null)
            {
                return matchesDocument(null);
            }

            Document doc = DomFactory.createDocumentBuilder().parse(inputStream);
            return matchesDocument(doc);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    abstract protected boolean matchesDocument(@Nullable Document document) throws Exception;
}
