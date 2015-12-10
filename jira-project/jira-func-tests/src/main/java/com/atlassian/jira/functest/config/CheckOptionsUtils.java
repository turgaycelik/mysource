package com.atlassian.jira.functest.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read and update {@link CheckOptions} instances.
 *
 * @since v4.0
 */
public final class CheckOptionsUtils
{
    private static final AllOptions ALL_OPTIONS = new AllOptions();
    private static final NoOptions NO_OPTIONS = new NoOptions();

    private static final String ALL_OPTION = "all";

    /*
     * Allows:
     *  suppresschecks: a,c
     *  suppresschecks:a,c
     *  suppresscheck:a,c
     *  suppresscheck: a,c
     *  suppresschecks a,c
     *  suppresschecks c
     *  suppresscheck a
     *  suppresscheck d
     *
     */
    private static final Pattern PATTERN_FIND_OPTION = Pattern.compile("^\\s*suppresschecks?(?::|\\s)(.*?)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /**
     * Match all the blank lines at the start of a string.
     */
    private static final Pattern PATTERN_CLEAN_LINES = Pattern.compile("\\s*$\\s+?^?", Pattern.MULTILINE);

    private CheckOptionsUtils()
    {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Can't clone me, I'm a loner.");
    }

    public static CheckOptions allOptions()
    {
        return ALL_OPTIONS;
    }

    public static CheckOptions noOptions()
    {
        return NO_OPTIONS;
    }

    public static CheckOptions disabled(String... options)
    {
        return new DisabledOptions(Arrays.asList(options));
    }

    public static CheckOptions disableIn(CheckOptions original, String... newDisabledOptions)
    {
        final Set<String> allDisabled = new HashSet<String>(original.asSuppressChecks());
        allDisabled.addAll(Lists.newArrayList(newDisabledOptions));
        return new DisabledOptions(allDisabled);
    }

    /**
     * Creates a {@link CheckOptions} object by parsing through the
     * comments at the top level of the passed document and looking for lines of the form:
     * <p/>
     * suppresscheck: check
     * <p/>
     * or
     * <p/>
     * suppresschecks: check, check2
     * <p/>
     * There a special check called "all". When specified all the checks will be suppressed.
     *
     * @param document the document to scan.
     * @return a CheckOptions object configured from the passed document.
     */
    public static CheckOptions parseOptions(Document document)
    {
        final Set<String> checkExcludes = new HashSet<String>();
        for (Comment comment : getComments(document))
        {
            final String s = comment.getText();
            final Matcher matcher = PATTERN_FIND_OPTION.matcher(s);
            int pos = 0;
            while (pos < s.length() && matcher.find(pos))
            {
                final String[] parsedOptions = matcher.group(1).split(Pattern.quote(","));
                for (String parsedOption : parsedOptions)
                {
                    parsedOption = normalizeOption(parsedOption);
                    if (isAllOption(parsedOption))
                    {
                        return noOptions();
                    }
                    else
                    {
                        checkExcludes.add(parsedOption);
                    }
                }
                pos = matcher.end() + 1;
            }
        }

        if (checkExcludes.isEmpty())
        {
            return allOptions();
        }
        else
        {
            return new DisabledOptions(checkExcludes);
        }
    }

    /**
     * Writes the passed {@link CheckOptions} object to a comment in the
     * passed document.
     *
     * @param document the document to write to.
     * @param options the options to write to the passed document.
     */
    public static void writeOptions(Document document, CheckOptions options)
    {
        final Set<String> suppressOptions = options.asSuppressChecks();
        final List<Comment> comments = getComments(document);
        final String optionStr = StringUtils.join(suppressOptions, ", ");
        boolean update = !suppressOptions.isEmpty();

        if (comments.isEmpty())
        {
            if (update)
            {
                addTopLevelComment(document, String.format("%n%n    suppresschecks: %s%n%n", optionStr));
            }
        }
        else if (hasExistingChecks(comments))
        {
            replaceExistingCheck(comments, optionStr);
        }
        else
        {
            if (update)
            {
                final Comment comment = comments.get(0);
                comment.setText(String.format("%n%n    suppresschecks: %s%n%n%s", optionStr, startLineTrim(comment.getText())));
            }
        }
    }

    private static boolean hasExistingChecks(List<Comment> comments)
    {
        for (final Comment comment : comments)
        {
            if (PATTERN_FIND_OPTION.matcher(comment.getText()).find())
            {
                return true;
            }
        }
        return false;
    }

    private static void replaceExistingCheck(final List<Comment> comments, final String optionStr)
    {
        boolean updated = false;
        for (final Comment comment : comments)
        {
            final Matcher matcher = PATTERN_FIND_OPTION.matcher(comment.getText());
            if (matcher.find())
            {
                comment.setText(matcher.replaceFirst(Matcher.quoteReplacement(replacementString(optionStr))));
                updated = true;
                break;
            }
        }
        if (!updated)
        {
            throw new AssertionError("Did not find comment to update");
        }
    }

    private static String replacementString(final String optionStr)
    {
        return StringUtils.isEmpty(optionStr) ? optionStr : "    suppresschecks: " + optionStr;
    }

    /**
     * Removes the starting blank lines from the passed string.
     *
     * @param string the string to remove blank lines from.
     * @return remove the leading blank lines from the passed string.
     */
    private static String startLineTrim(final String string)
    {
        final Matcher matcher = PATTERN_CLEAN_LINES.matcher(string);
        if (matcher.find())
        {
            if (matcher.start() == 0)
            {
                return string.substring(matcher.end());
            }
        }
        return string;
    }

    @SuppressWarnings ({ "unchecked" })
    private static List<Comment> getComments(final Document document)
    {
        return (List<Comment>) document.selectNodes("/comment()");
    }

    private static Comment addTopLevelComment(final Document document, String comment)
    {
        @SuppressWarnings ({ "unchecked" }) final List<Node> list = document.content();
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Comment commentNode = factory.createComment(comment);
        list.add(0, commentNode);
        list.add(1, factory.createText("\n"));

        return commentNode;
    }

    private static boolean isAllOption(String option)
    {
        return ALL_OPTION.equalsIgnoreCase(option);
    }

    private static String normalizeOption(String checkId)
    {
        checkId = StringUtils.trimToNull(checkId);
        if (checkId != null)
        {
            return checkId.toLowerCase(Locale.ENGLISH);
        }
        else
        {
            return null;
        }
    }

    private static class AllOptions implements CheckOptions
    {
        public boolean checkEnabled(String checkId)
        {
            return true;
        }

        public Set<String> asSuppressChecks()
        {
            return Collections.emptySet();
        }
    }

    private static class NoOptions implements CheckOptions
    {
        public boolean checkEnabled(String checkId)
        {
            return false;
        }

        public Set<String> asSuppressChecks()
        {
            return Collections.singleton(ALL_OPTION);
        }
    }

    private static class DisabledOptions implements CheckOptions
    {
        private final Set<String> disabledOptions;

        public DisabledOptions(Collection<String> disabledOptions)
        {
            final HashSet<String> strings = new HashSet<String>();
            for (String option : disabledOptions)
            {
                option = normalizeOption(option);
                if (option != null)
                {
                    strings.add(option);
                }
            }
            this.disabledOptions = Collections.unmodifiableSet(strings);
        }

        public boolean checkEnabled(String checkId)
        {
            return !disabledOptions.contains(normalizeOption(checkId));
        }

        public Set<String> asSuppressChecks()
        {
            return disabledOptions;
        }
    }
}
