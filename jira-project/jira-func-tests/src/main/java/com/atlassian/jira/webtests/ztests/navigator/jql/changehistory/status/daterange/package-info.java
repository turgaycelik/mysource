/**
 * Contains the acceptance tests responsible of verifying that a user is able to query all the issues that were in
 * a particular status in a set date range.
 *
 * <p>There are three types of ranges that can be specified:
 * <dl>
 *     <dt>BEFORE <em>date_value</em>
 *     <dd>
 *         <p>Defines an implicit range of dates ending at a specified date. The range excludes the end date.</p>
 *         <p>Example JQL query: <code>WAS resolved BEFORE '2011-05-25 09:30'</code></p>
 *     </dd>
 *
 *     <dt>AFTER <em>date_value</em>
 *     <dd>
 *         <p>Defines an implicit range of dates starting at a specified date. The range excludes the start date.</p>
 *         <p>Example JQL query: <code>WAS resolved AFTER '2011-05-25 09:30'</code></p>
 *     </dd>
 *
 *     <dt>DURING <em>date_range</em>
 *     <dd>
 *         <p>
 *             Defines an explicit range of dates starting at one date and ending at another date. The range includes
 *             both the start and the end date.
 *         </p>
 *         <p>Example JQL query: <code>WAS resolved DURING ('2011-05-25 09:30', '2011-05-30 09:30)</code></p>
 *     </dd>
 * </dl>
 * </p>
 * @see <a href="https://jdog.atlassian.com/browse/JRADEV-3740">User Story [JRADEV-3740]</a>
 */
package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status.daterange;