package com.atlassian.jira.web.pagebuilder;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.web.action.util.FieldsResourceIncluder;
import com.atlassian.webresource.api.assembler.WebResourceAssembler;

import java.io.Writer;

/**
 * Decorator representing general-*.jsp
 * @since v6.1
 */
public class GeneralJspDecorator extends AbstractJspDecorator implements Decorator
{
    public GeneralJspDecorator(WebResourceAssembler webResourceAssembler)
    {
        super(webResourceAssembler,
                "/decorators/general-head-pre.jsp",
                "/decorators/general-head-post.jsp",
                "/decorators/general-body-pre.jsp",
                "/decorators/general-body-post.jsp");
    }

    @Override
    public void writePreHead(final Writer writer)
    {
        webResourceAssembler.resources()
                .requireContext("atl.general")
                .requireContext("jira.general");

        final FieldsResourceIncluder headFieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
        headFieldResourceIncluder.includeFieldResourcesForCurrentUser();

        super.writePreHead(writer);
    }
}
