package com.atlassian.jira.web.util;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.SearchAndReplacer;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

/**
 * Web resource transformer to retrieve localized date format symbols.
 * <p/>
 * This transforms the content by pattern matching on the text JIRA.getDateFormatSymbol("key") where key can
 * be any string. It replaces this syntax with the literal string translation before serving the resource.
 *
 * @since v6.0
 */
public class DateFormatTransformer implements WebResourceTransformer
{
    private static final Pattern PATTERN = Pattern.compile(
            "JIRA\\.getDateFormatSymbol" +
                    "\\(\\s*" + // start paren
                    "['\"]([^'\"]+)['\"]" + // single or double quoted word
                    "\\s*\\)" // end paren
    );

    private final SearchAndReplacer grep;

    public DateFormatTransformer(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.grep = SearchAndReplacer.create(PATTERN, new Function<Matcher, CharSequence>()
        {
            public String apply(Matcher matcher)
            {
                String key = matcher.group(1);
                DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance(jiraAuthenticationContext.getLocale());

                List<String> values;
                if (key.equals("amPmStrings"))
                {
                    values = Arrays.asList(dateFormatSymbols.getAmPmStrings());
                }
                else if (key.equals("eras"))
                {
                    values = Arrays.asList(dateFormatSymbols.getEras());
                }
                else if (key.equals("months"))
                {
                    values = Arrays.asList(dateFormatSymbols.getMonths()).subList(0, 12);
                }
                else if (key.equals("shortMonths"))
                {
                    values = Arrays.asList(dateFormatSymbols.getShortMonths()).subList(0, 12);
                }
                else if (key.equals("shortWeekdays"))
                {
                    values = Arrays.asList(dateFormatSymbols.getShortWeekdays()).subList(1,8);
                }
                else if (key.equals("weekdays"))
                {
                    values = Arrays.asList(dateFormatSymbols.getWeekdays()).subList(1,8);
                }
                else
                {
                    values = Collections.emptyList();
                }

                return '[' + Joiner.on(',').join(Lists.transform(values, new Function<String, String>()
                {
                    @Override
                    public String apply(String value)
                    {
                        // JRA-36079: do ecma-script escaping of value so that we don't end up with javascript
                        // objects like this -> [ ... ,'uh'ohhhh', ... ]
                        return StringUtils.quote(StringEscapeUtils.escapeEcmaScript(value));
                    }
                })) + ']';
            }
        });
    }

    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new CharSequenceDownloadableResource(nextResource)
        {
            @Override
            public CharSequence transform(CharSequence originalContent)
            {
                return grep.replaceAll(originalContent);
            }
        };
    }
}
