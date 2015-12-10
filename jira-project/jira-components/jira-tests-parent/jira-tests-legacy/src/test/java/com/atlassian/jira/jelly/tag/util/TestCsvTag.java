/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.util;

import electric.xml.Document;

import electric.xml.matching.Compare;

import com.atlassian.jira.jelly.AbstractJellyTestCase;

public class TestCsvTag extends AbstractJellyTestCase
{
    public TestCsvTag(String s)
    {
        super(s);
    }

    public void testTheTag() throws Exception
    {
        final String scriptFilename = "csv-tag.test.the-tag.jelly";
        Document document = runScript(scriptFilename);

        String expectedXml = "<JiraJelly xmlns:jira='jelly:com.atlassian.jira.jelly.JiraTagLib' xmlns:j='jelly:core' xmlns:test='jelly:junit'>" + "<row><column>foo</column><column>bar</column></row>" + "<row><column>baz</column><column>bat</column></row>" + "</JiraJelly>";

        Document expectedDocument = new Document(expectedXml);
        assertTrue(Compare.equals(expectedDocument, document));
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "util" + FS;
    }
}
