package com.atlassian.jira.matchers;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers for java.io.File
 *
 * @since v6.1
 */
public class FileMatchers
{
    private FileMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Matcher<File> exists() {
        return new TypeSafeMatcher<File>()
        {
            public File testedFile;

            @Override
            protected boolean matchesSafely(final File file)
            {
                testedFile = file;
                return file.exists();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("that file ");
                description.appendValue(testedFile);
                description.appendText(" exists");
            }
        };
    }

    public static Matcher<File> isFile()
    {
        return new TypeSafeMatcher<File>()
        {
            public File testedFile;

            @Override
            protected boolean matchesSafely(final File file)
            {
                testedFile = file;
                return file.isFile();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("that file ");
                description.appendValue(testedFile);
                description.appendText(" is a regular file");
            }
        };
    }

    public static Matcher<File> isDirectory()
    {
        return new TypeSafeMatcher<File>()
        {
            public File testedFile;

            @Override
            protected boolean matchesSafely(final File file)
            {
                testedFile = file;
                return file.isDirectory();
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("that file ");
                description.appendValue(testedFile);
                description.appendText(" is a directory");
            }
        };
    }

    public static Matcher<File> named(final String name)
    {
        return new TypeSafeMatcher<File>()
        {
            @Override
            public boolean matchesSafely(File item)
            {
                return item.getName().equals(name);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a file named ")
                        .appendValue(name);
            }

            public void describeMismatchSafely(Description description, File item)
            {
                description.appendText("a file named ")
                        .appendValue(name)
                        .appendText(" in ")
                        .appendValue(item.getParentFile());
            }
        };
    }
}
