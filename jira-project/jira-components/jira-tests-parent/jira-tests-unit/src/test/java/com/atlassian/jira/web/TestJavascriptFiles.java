package com.atlassian.jira.web;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Currently this is only used to make sure JS files don't contain console.log statements but can be
 * extended for more stuff in future
 */
public class TestJavascriptFiles
{
    private final List<FileFilter> excludes = new ArrayList<FileFilter>();
    private final Set<String> badCalls = new LinkedHashSet<String>();
    private FileFilter filter;

    @Before
    public void setUp() throws Exception
    {
        //add file sthat should be excluded here!
        excludes.add(new NameFileFilter("deployJava.js", IOCase.INSENSITIVE));

        //add calls we don't want to include in our JS here
        badCalls.add("console.log(");
        badCalls.add("AJS.log(");
        badCalls.add("alert(");
        badCalls.add("debugger");

        this.filter = getFileFilter();
    }

    @Test
    public void testJSContainsNoLogging()
    {
        final File webappRoot = new File("../jira-core/src/main/webapp");
        final List<FileResult> resultList = processFiles(webappRoot);
        if(!resultList.isEmpty())
        {
            final StringBuilder res = new StringBuilder("Some files contained logging statements: \n");
            for (FileResult result : resultList)
            {
                res.append(result.toString()).append("\n");
            }
            res.append("Perhaps these statements should be surrounded by:\n/* [alert] */\n <STATEMENT>\n/* [alert] end */\n\nOR\n\n/* [logging] */\n <STATEMENT>\n/* [logging] end */");
            fail(res.toString());
        }
    }

    private FileFilter getFileFilter()
    {
        IOFileFilter filter = new OrFileFilter(new WildcardFileFilter("*.js", IOCase.INSENSITIVE), FileFilterUtils.directoryFileFilter());
        if (!excludes.isEmpty())
        {
            final OrFileFilter orFilter = new OrFileFilter(excludes);
            filter = FileFilterUtils.andFileFilter(FileFilterUtils.notFileFilter(orFilter), filter);
        }
        return filter;
    }

    private List<FileResult> processFiles(final File file)
    {
        final List<FileResult> results = new ArrayList<FileResult>();

        if (file.isFile())
        {
            final FileResult result = checkFile(file);
            if(!result.isValid())
            {
                results.add(result);
            }
        }
        else
        {
            final File[] files = file.listFiles(filter);
            if (files != null) {
                for (File sub : files)
                {
                    results.addAll(processFiles(sub));
                }
            }
        }
        return results;
    }

    private FileResult checkFile(final File file)
    {
        final LineIterator iterator;
        try
        {
            iterator = FileUtils.lineIterator(file);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        int lineCount = 1;
        final FileResult result = new FileResult(file.getPath());
        boolean inLoggingMode = false;
        boolean inAlertMode = false;

        /*
        Generally logging and alert lines should be marked with:
        //[alert]
        <STATEMENTS>
        // [alert] end

        OR

        //[logging]
        <STATEMENTS>
        // [logging] end

        if they're not then they're probably dodgy log statements!
         */

        while(iterator.hasNext())
        {
            final String line = iterator.nextLine();
            if(line.contains("[logging]"))
            {
                inLoggingMode = !inLoggingMode;
            }
            if(line.contains("[alert]"))
            {
                inAlertMode = !inAlertMode;
            }
            for (String badCall : badCalls)
            {
                if(line.contains(badCall) && !inLoggingMode && !inAlertMode)
                {
                    result.addBadCall(lineCount, badCall);
                }
            }
            lineCount ++;
        }
        return result;
    }

    static class FileResult {
        private String filePath;
        private Map<Integer, String> badCallMap = new TreeMap<Integer, String>();

        FileResult(final String filePath)
        {
            this.filePath = filePath;
        }

        public void addBadCall(int lineNum, String badCall)
        {
            badCallMap.put(lineNum, badCall);
        }

        public boolean isValid()
        {
            return badCallMap.isEmpty();
        }

        @Override
        public String toString()
        {
            final StringBuilder res = new StringBuilder(filePath);
            res.append(" contains logging calls: \n");
            for (Map.Entry<Integer, String> entry : badCallMap.entrySet())
            {
                res.append("\tLine ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return res.toString();
        }
    }
}
