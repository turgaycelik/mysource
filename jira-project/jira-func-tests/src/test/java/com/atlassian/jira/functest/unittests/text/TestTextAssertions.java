package com.atlassian.jira.functest.unittests.text;

import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * A test for TextAssertions
 * <p/>
 *
 * TODO add more tests since this deserves special attention.
 * Mark L are you listening?
 * Yes Brad, I am listening.
 *
 * @since v3.13
 */
public class TestTextAssertions extends TestCase
{
    static final String QBF = "the quick brown fox jumped over the lazy dog";

    public void testAssertTextPresentNumOccurences() throws Exception
    {
        final TextAssertions textAssertions = new TextAssertionsImpl();
        textAssertions.assertTextPresentNumOccurences(QBF, "brown", 1);
        textAssertions.assertTextPresentNumOccurences(QBF, "the", 2);
        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextPresentNumOccurences(QBF, "the", 3);
            }
        });
        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextPresentNumOccurences(QBF, "the", 1);
            }
        });
        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextPresentNumOccurences(QBF, "brown", 2);
            }
        });
    }

    public void testBasicStringOperations() throws Exception
    {
        final TextAssertions textAssertions = new TextAssertionsImpl();

        ensureNoAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextPresent(QBF, "lazy");
            }
        });
        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextPresent(QBF, "lazi");
            }
        });

        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextPresent(QBF, "doggy");
            }
        });

        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextNotPresent(QBF, "dog");
            }
        });

        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextSequence(QBF, "quick", "brown", "FAX");
            }
        });

        ensureNoAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertTextSequence(QBF, "quick", "brown", "fox jumped o", "ver the");
            }
        });

    }

    public void testBasicRegexOperations() throws Exception
    {
        final TextAssertions textAssertions = new TextAssertionsImpl();

        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertRegexMatch(QBF, "ly");
            }
        });
        ensureNoAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertRegexMatch(QBF, "l.*y");
            }
        });
        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertRegexMatch(QBF, "lazi");
            }
        });

        ensureAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertRegexNoMatch(QBF, "lazy");
            }
        });
        ensureNoAssertionFailed(new Runnable()
        {
            public void run()
            {
                textAssertions.assertRegexNoMatch(QBF, "tim");
            }
        });
    }

    private void ensureAssertionFailed(Runnable assertionRunnable)
    {
        try
        {
            assertionRunnable.run();
            // If we get here, then we need to fail, but not let the AssertionFailedError get caught by our catch block.
        }
        catch (AssertionFailedError e)
        {
            // Expected
            return;
        }
        // Wait until we are out of the try..catch block before we fail.
        fail("Did not throw AssertionFailedError as expected");
    }

    private void ensureNoAssertionFailed(Runnable assertionRunnable)
    {
        try
        {
            assertionRunnable.run();
        }
        catch (AssertionFailedError e)
        {
            fail("Unexpected AssertionFailedError");
        }
    }
}
