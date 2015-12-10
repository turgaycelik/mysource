<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean name="'com.atlassian.jira.util.JiraDateUtils'" id="dateUtils" />
<html>
<head>
    <title><ww:text name="'bulkedit.title'"/></title>
    <ww:if test="/ourTask/finished == false">
        <meta http-equiv="refresh" content="5">
    </ww:if>
</head>
<body>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                <ui:param name="'content'">
                    <h1><ww:text name="'bulkedit.title'"/></h1>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'id'" value="'stepped-process'" />
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

                    <page:applyDecorator name="jiraform">
                        <page:param name="formName">bulkoperationprogressform</page:param>
                        <page:param name="columns">1</page:param>
                        <page:param name="action">BulkDelete.jspa</page:param>
                        <page:param name="method">get</page:param>
                        <page:param name="submitId">refresh_submit</page:param>
                        <page:param name="submitName"><ww:text name="'common.words.refresh'"/></page:param>
                        <page:param name="width">100%</page:param>
                        <page:param name="title"><ww:text name="'bulk.operation.progress.title'"/></page:param>
                        <page:param name="instructions">
                            <ww:if test="/thereAnyTransitionError == true">
                                <ww:if test="/transitionErrorsLimited == true">
                                    <aui:component template="auimessage.jsp" theme="'aui'">
                                        <aui:param name="'messageType'">warning</aui:param>
                                        <aui:param name="'messageHtml'">
                                            <ww:text name="'bulkworkflowtransition.error.limit'" />
                                        </aui:param>
                                    </aui:component>
                                </ww:if>

                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">error</aui:param>
                                    <aui:param name="'messageHtml'">
                                        <ww:property value="/transitionErrors" >
                                            <ww:if test="./empty == false">
                                                <ww:iterator value=".">
                                                    <strong><ww:property value="./key"/></strong>
                                                    <ul>
                                                        <ww:iterator value="./value">
                                                            <li><ww:property value="."/></li>
                                                        </ww:iterator>
                                                    </ul>
                                                </ww:iterator>
                                            </ww:if>
                                        </ww:property>
                                    </aui:param>
                                </aui:component>
                            </ww:if>

                            <ww:if test="/ourTask/finished == true && /ourTask/userWhoStartedTask == false">
                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">info</aui:param>
                                    <aui:param name="'messageHtml'">
                                        <p>
                                            <ww:text name="'common.tasks.cant.acknowledge.task.you.didnt.start'">
                                                <ww:param name="'value0'"><a href="<ww:property value="/ourTask/userURL"/>"><ww:property value="/ourTask/user/name"/></a></ww:param>
                                            </ww:text>
                                        </p>
                                    </aui:param>
                                </aui:component>
                            </ww:if>
                        </page:param>

                        <tr bgcolor="#ffffff">
                        <td>
                            <ui:component template="taskdescriptor.jsp" name="'/ourTask'"/>
                            <ww:if test="/ourTask/finished == true">
                                <page:param name="action">BulkOperationFinish.jspa</page:param>
                                <ui:component name="'errorActionOutput'" template="hidden.jsp"/>
                                <ww:if test="/ourTask/userWhoStartedTask == true">
                                    <page:param name="submitId">acknowledge_submit</page:param>
                                    <page:param name="submitName"><ww:text name="'common.words.acknowledge'"/></page:param>
                                    <ui:component name="'taskId'" template="hidden.jsp"/>
                                </ww:if>
                                <ww:else>
                                    <page:param name="submitId">done_submit</page:param>
                                    <page:param name="submitName"><ww:text name="'common.words.done'"/></page:param>
                                </ww:else>
                            </ww:if>
                            <ww:else>
                                <page:param name="action">BulkOperationProgress.jspa</page:param>
                                <page:param name="submitId">refresh_submit</page:param>
                                <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
                                <ui:component name="'taskId'" template="hidden.jsp"/>
                                <ui:component name="'errorActionOutput'" template="hidden.jsp"/>
                                <ww:if test="/ourTask/cancellable == true">
                                    <page:param name="buttons">
                                        <input class="aui-button" type="button" id="cancel_bulkedit_submit" name ="<ww:text name="'bulk.operation.progress.cancel'"/>" value="<ww:text name="'bulk.operation.progress.cancel'"/>"
                                               onclick="location.href='BulkDeleteCancel.jspa?taskId=<ww:property value="taskId" />'"/>
                                    </page:param>
                                </ww:if>
                            </ww:else>
                            <ww:if test="/ourTask/cancelled == true">
                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">info</aui:param>
                                    <aui:param name="'messageHtml'">
                                        <p id="bulkedit-progress-message">
                                            <ww:if test="/ourTask/finished == true">
                                                <ww:text name="'bulk.operation.progress.cancelled'"/><br/>
                                            </ww:if>
                                            <ww:else>
                                                <ww:text name="'bulk.operation.progress.cancelling'"/><br/>
                                            </ww:else>
                                        </p>
                                    </aui:param>
                                </aui:component>
                            </ww:if>

                        </td>
                        </tr>

                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
