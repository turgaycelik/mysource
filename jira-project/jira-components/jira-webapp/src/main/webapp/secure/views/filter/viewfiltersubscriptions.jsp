<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'common.concepts.managefilters'"/></title>
    <content tag="section">find_link</content>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <ol class="aui-nav aui-nav-breadcrumbs">
                <li><a href="<ww:url page="ManageFilters.jspa"/>"><ww:text name="'common.concepts.managefilters'" /></a></li>
            </ol>
            <h1><ww:property value="filterName"/></h1>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <a class="aui-button" href="<ww:url value="'IssueNavigator.jspa'"><ww:param name="'requestId'" value="filterId" /></ww:url>"><ww:text name="'managefilters.filter.view'" /></a>
            </div>
        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'id'" value="'issuenav'"/>
        <ui:param name="'extraClasses'">
            <ww:if test="/conglomerateCookieValue('jira.toggleblocks.cong.cookie','lhc-state')/contains('#issuenav') == true">lhc-collapsed</ww:if>
        </ui:param>
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

                    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
                        <ui:param name="'mainContent'">
                            <h2><ww:text name="'managefilters.subscriptions'" /></h2>
                        </ui:param>
                        <ui:param name="'actionsContent'"><div class="aui-buttons">
                            <a class="aui-button trigger-dialog" href="<ww:url value="'FilterSubscription!default.jspa'"><ww:param name="'filterId'" value="filterId" /></ww:url>">
                                <ww:text name="'subscriptions.add'"/>
                            </a>
                        </div>
                        </ui:param>
                        <ui:param name="'helpContent'">
                            <aui:component name="'issue_filters_subscribing'" template="help.jsp" theme="'aui'" />
                        </ui:param>
                    </ui:soy>
                    <ww:if test="mailConfigured == false && subscriptionCount > 0">
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">warning</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="'filters.no.mail.configured'"/></p>
                            </aui:param>
                        </aui:component>
                    </ww:if>
                    <ww:if test="subscriptions != null && subscriptions/size > 0">
                        <table class="aui aui-table-rowhover">
                            <thead>
                                <tr>
                                    <th><ww:text name="'subscriptions.subscriber'"/></th>
                                    <th><ww:text name="'subscriptions.subscribed'"/></th>
                                    <th><ww:text name="'filtersubscription.field.schedule'"/></th>
                                    <th><ww:text name="'subscriptions.lastSent'"/></th>
                                    <th><ww:text name="'subscriptions.nextSend'"/></th>
                                    <th><ww:text name="'common.words.operations'"/></th>
                                </tr>
                            </thead>
                            <tbody>
                                <ww:iterator value="subscriptions" status="'status'">
                                    <tr>
                                        <td><ww:property value="subscriber(.)"/></td>
                                        <td>
                                            <ww:if test="groupName != null && groupName/length > 0">
                                                <ww:if test="/groupValid(.) == false"><span class="warning" title="<ww:text name="'admin.projects.group.invalid'"/>"></ww:if>
                                                <ww:property value="groupName"/>
                                                <ww:if test="/groupValid(.) == false"></span></ww:if>
                                            </ww:if>
                                            <ww:else>
                                                    <ww:property value="subscriber(.)"/>
                                            </ww:else>
                                        </td>
                                        <td><span title="<ww:property value="/cronTooltip(.)"/>"><ww:property value="prettySchedule(.)" /></span></td>
                                        <td><ww:property value="lastSent(.)" /></td>
                                        <td><ww:property value="nextSend(.)" /></td>
                                        <td>
                                            <ww:if test="loggedInUserIsOwner(.) == true" >
                                                <ul class="operations-list">
                                                    <li><a class="trigger-dialog" id="edit_subscription" href="<ww:url value="'FilterSubscription!default.jspa'"><ww:param name="'subId'" value="id" /><ww:param name="'filterId'" value="filterId" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                                                    <li><a href="<ww:url value="'DeleteSubscription.jspa'"><ww:param name="'subId'" value="id" /><ww:param name="'filterId'" value="filterId" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                                    <ww:if test="mailConfigured == true">
                                                        <li><a href="<ww:url value="'RunSubscription.jspa'"><ww:param name="'subId'" value="id" /><ww:param name="'filterId'" value="filterId" /></ww:url>"><ww:text name="'common.forms.run.now'"/></a></li>
                                                    </ww:if>
                                                </ul>
                                            </ww:if>
                                        </td>
                                    </tr>
                                </ww:iterator>
                            </tbody>
                        </table>
                    </ww:if>
                    <ww:else>
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">info</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="'subscriptions.nosubs'"/></p>
                            </aui:param>
                        </aui:component>
                    </ww:else>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
