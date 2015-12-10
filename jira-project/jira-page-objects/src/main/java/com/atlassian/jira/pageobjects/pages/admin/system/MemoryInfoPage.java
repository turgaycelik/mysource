package com.atlassian.jira.pageobjects.pages.admin.system;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.inject.Inject;

import org.openqa.selenium.By;

public class MemoryInfoPage extends AbstractJiraPage
{

    public final static String URI = "/secure/admin/ViewMemoryInfo.jspa";

    @Inject
    protected PageElementFinder finder;

    /**
     * Timed condition checking if we're at given page.
     *
     * @return timed condition checking, if the test is at given page
     */
    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("memory-info")).timed().isPresent();
    }

    /**
     * @return The URI, including query string, relative to the base url
     */
    @Override
    public String getUrl()
    {
        return URI;
    }

    public MemoryInfo getJvmInfoForRowTitle(final String jvmInfoKey)
    {
        for (final PageElement row : jvmInfoElements())
        {
            boolean found = false;
            for (final PageElement cell : row.findAll(By.tagName("td")))
            {
                if (!found)
                {
                    if (cell.getText().contains(jvmInfoKey))
                    {
                        found = true;
                    }
                }
                else
                {
                    return MemoryInfo.createFromStringLabel(cell.getText(), cell);
                }
            }
        }
        return null;
    }

    private List<PageElement> jvmInfoElements()
    {
        return finder.find(By.id("jvm-info")).findAll(By.cssSelector("tr"));
    }

    private List<PageElement> memoryInfoElements()
    {
        return finder.find(By.id("memory-info")).findAll(By.cssSelector("tr"));
    }

    public static class MemoryInfo
    {
        private static final Pattern PATTERN = Pattern.compile("(\\d+)% Free \\(Used: (\\d+) MB Total: (\\d+) MB\\).*");
        private static final Pattern NON_GRAPH_PATTER = Pattern.compile(".*Used: (\\d+) MB");

        private final long percentage;
        private final long used;
        private final long total;

        private final PageElement pageElement;

        private MemoryInfo(final long percentage, final long used, final long total, final PageElement pageElement)
        {
            this.percentage = percentage;
            this.used = used;
            this.total = total;
            this.pageElement = pageElement;
        }

        protected static MemoryInfo createFromStringLabel(final String label, final PageElement pageElement)
        {
            final Matcher matcher = PATTERN.matcher(label);
            final Matcher secondMatcher = NON_GRAPH_PATTER.matcher(label);

            if(matcher.find())
            {
                return new MemoryInfo(Long.parseLong(matcher.group(1)),
                                      Long.parseLong(matcher.group(2)),
                                      Long.parseLong(matcher.group(3)),
                                      pageElement);
            }

            if(secondMatcher.find())
            {
                return new MemoryInfo(0L, Long.parseLong(secondMatcher.group(1)), 0L, pageElement);
            }

            return null;
        }

        public long getPercentage()
        {
            return percentage;
        }

        public long getUsed()
        {
            return used;
        }

        public long getTotal()
        {
            return total;
        }

        public PageElement getPageElement()
        {
            return pageElement;
        }

        public boolean isChartVisible() {
            return pageElement.findAll(By.tagName("table")).size() > 0;
        }
    }
}
