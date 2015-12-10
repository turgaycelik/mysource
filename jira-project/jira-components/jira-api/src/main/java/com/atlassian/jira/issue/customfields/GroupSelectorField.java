package com.atlassian.jira.issue.customfields;

import org.apache.lucene.search.Query;

/**
 * An interface that can be implemented by a custom field type. By implementing this interface a custom field
 * type lets JIRA 'know' that it can potentially be used as a 'Group Selector'. JIRA then uses this field in places where
 * a group selector can be shown. For example:
 * <ul>
 *  <li>The {@link com.atlassian.jira.security.type.GroupCF} Security type uses a custom field of type 'Group Selector'
 * to grant permissions.</li>
 *  <li>The {@link com.atlassian.jira.workflow.condition.InGroupCFCondition} uses a custom field of type 'Group
 * Selector' to determine if a user is allowed to execute a workflow transition.
 * </ul>
 *
 * A Group Selector is any field that can return a Group or a group name (String) as its value.
 *
 *<p>
 * If a {@link CustomFieldType} implements this interface it should return values as objects of one of the following
 * types:
 * <ul>
 *  <li>{@link com.atlassian.crowd.embedded.api.Group}</li>
 *  <li>{@link java.util.Collection} of {@link com.atlassian.crowd.embedded.api.Group}s</li>
 *  <li>{@link String}</li>
 *  <li>{@link java.util.Collection} of {@link String}s</li>
 * </ul>
 * </p>
 *
 * <p>
 * For example, a Multi Select custom field is a Group Selector as the values it returns are a Collection of Strings. These
 * strings <b>must</b> represent group names. It is up to JIRA Administrator to ensure that the when the Multi Select
 * custom field's options are defined, that all of them map exactly to a group name.
 */
public interface GroupSelectorField
{

    /**
     * This method should be implemented in your custom type to return a Query. Generally you should return a TermQuery
     * in the form of  <code>fieldName:groupName</code>.  However some custom fields, such as (@Link SelectCFType)
     * manipulate the field identifiers and values before storing in the index.  In cases like this you will have to
     * implement the method such that it searches the correct fields.
     *
     * @param fieldID   the id of the custom field
     * @param groupName the name of the group to filter on
     * @return the (@Link Query) to pass to the searcher
     */
    Query getQueryForGroup(final String fieldID, String groupName);
}
