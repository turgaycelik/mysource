package com.atlassian.jira.rest.v2.search;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* Represetation of user's default share scope.
*
* @since v6.0
*/
@XmlRootElement(name = "defaultShareScope")
public class DefaultShareScopeBean
{
    @XmlElement
    private Scope scope;

    public DefaultShareScopeBean()
    {
    }

    public DefaultShareScopeBean(Scope scope)
    {
        this.scope = scope;
    }

    public Scope getScope()
    {
        return scope;
    }

    public static enum Scope
    {
        GLOBAL,
        PRIVATE
    }

    // Documentation
    public static final DefaultShareScopeBean DOC_EXAMPLE = new DefaultShareScopeBean(Scope.GLOBAL);
}
