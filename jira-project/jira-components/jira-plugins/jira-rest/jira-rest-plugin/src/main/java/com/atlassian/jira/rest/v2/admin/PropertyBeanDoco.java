package com.atlassian.jira.rest.v2.admin;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
* @since v6.2
*/
@SuppressWarnings ("UnusedDeclaration")
class PropertyBeanDoco
{
    static final WorkflowTransitionResource.PropertyBean EXAMPLE = new WorkflowTransitionResource.PropertyBean("jira.i18n.title", "some.title");
    static final WorkflowTransitionResource.PropertyBean EXAMPLE2 = new WorkflowTransitionResource.PropertyBean("jira.permission", "createissue");
    static final WorkflowTransitionResource.PropertyBean CREATE_EXAMPLE = new WorkflowTransitionResource.PropertyBean(null, "createissue");
    static final List<WorkflowTransitionResource.PropertyBean> EXAMPLE_LIST = ImmutableList.of(EXAMPLE, EXAMPLE2);
}
