package com.atlassian.jira.config;

/**
 * TWENTY-318: Moved this from multitenant
 *
 * @since v5.2
 */
public interface CustomConfigHandler<B>   {

    java.lang.Class<B> getBeanClass();

    B parse(org.dom4j.Element element);

    void writeTo(org.dom4j.Element element, B b);
}