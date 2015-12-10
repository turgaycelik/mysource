package com.atlassian.jira.upgrade.util;

import org.dom4j.Document;

public interface SearchRequestConverter
{
    public Document process(Document document);
}
