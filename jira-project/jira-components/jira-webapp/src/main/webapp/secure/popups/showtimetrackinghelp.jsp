<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<html>
<head>
    <title><ww:text name="'showtimetrackinghelp.title'"/></title>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'showtimetrackinghelp.heading'"/></h1>
        </ui:param>
    </ui:soy>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <p><ww:text name="'showtimetrackinghelp.preamble'"/></p>
            <ul>
                <li>
                    <strong><ww:text name="'showtimetrackinghelp.logwork.options.title'"/></strong>
                    <ul>
                        <li><strong><ww:text name="'showtimetrackinghelp.timespent.title'"/></strong> - <ww:text name="'showtimetrackinghelp.timespent.description'"/></li>
                        <li><strong><ww:text name="'showtimetrackinghelp.timestarted.title'"/></strong> - <ww:text name="'showtimetrackinghelp.timestarted.description'"/></li>
                        <li>
                            <strong><ww:text name="'showtimetrackinghelp.remainingestimate.title'"/></strong> - <ww:text name="'showtimetrackinghelp.remainingestimate.description'"/>
                            <ul>
                                <li>
                                    <strong><ww:text name="'showtimetrackinghelp.estimate.autoadjust.title'"/></strong> -
                                    <ww:text name="'showtimetrackinghelp.autoadjust.description'">
                                        <ww:param name="'value0'"><strong></ww:param>
                                        <ww:param name="'value1'"><ww:text name="'showtimetrackinghelp.remainingestimate.title'"/></ww:param>
                                        <ww:param name="'value2'"></strong></ww:param>
                                        <ww:param name="'value3'"><ww:text name="'showtimetrackinghelp.timespent.title'"/></ww:param>
                                    </ww:text>
                                </li>
                                <li><strong><ww:text name="'showtimetrackinghelp.estimate.unset.title'"/></strong> - <ww:text name="'showtimetrackinghelp.estimate.unset.description'"/></li>
                                <li>
                                    <strong><ww:text name="'showtimetrackinghelp.estimate.existing.title'"/></strong> -
                                    <ww:text name="'showtimetrackinghelp.estimate.existing.description'">
                                        <ww:param name="'value0'"><strong></ww:param>
                                        <ww:param name="'value1'"><ww:text name="'showtimetrackinghelp.remainingestimate.title'"/></ww:param>
                                        <ww:param name="'value2'"></strong></ww:param>
                                    </ww:text>
                                </li>
                                <li>
                                    <strong><ww:text name="'showtimetrackinghelp.estimate.adjust.title'"/></strong> -
                                    <ww:text name="'showtimetrackinghelp.estimate.adjust.description'">
                                        <ww:param name="'value0'"><strong></ww:param>
                                        <ww:param name="'value1'"><ww:text name="'showtimetrackinghelp.remainingestimate.title'"/></ww:param>
                                        <ww:param name="'value2'"></strong></ww:param>
                                    </ww:text>
                                </li>
                                <li>
                                    <strong><ww:text name="'showtimetrackinghelp.estimate.reduce.title'"/></strong> -
                                    <ww:text name="'showtimetrackinghelp.estimate.reduce.description'">
                                        <ww:param name="'value0'"><strong></ww:param>
                                        <ww:param name="'value1'"><ww:text name="'showtimetrackinghelp.remainingestimate.title'"/></ww:param>
                                        <ww:param name="'value2'"></strong></ww:param>
                                    </ww:text>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </li>
                <ww:if test="/commentCopiedToWorkDescription==false">
                    <li>
                        <ww:text name="'showtimetrackinghelp.workdescription.description.1'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"><ww:text name="'showtimetrackinghelp.workdescription.title'"/></ww:param>
                            <ww:param name="'value2'"></strong></ww:param>
                            <ww:param name="'value3'"><ww:text name="'showtimetrackinghelp.comment.title'"/></ww:param>
                            <%--<ww:param name="'value4'">&nbsp;</ww:param> --%>
                        </ww:text>
                    </li>
                </ww:if>
                <ww:else>
                    <li>
                        <ww:text name="'showtimetrackinghelp.workdescription.description.1'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"><ww:text name="'showtimetrackinghelp.workdescription.title'"/></ww:param>
                            <ww:param name="'value2'"></strong></ww:param>
                            <ww:param name="'value3'"><ww:text name="'showtimetrackinghelp.comment.title'"/></ww:param>
                        </ww:text>
                    </li>
                </ww:else>
                <ww:if test="/commentCopiedToWorkDescription==false">
                    <li><ww:text name="'showtimetrackinghelp.workdescription.description.3'"/></li>
                </ww:if>
                <li>
                    <strong><ww:text name="'showtimetrackinghelp.timetracking.options.title'"/></strong>
                    <ul>
                        <li><strong><ww:text name="'showtimetrackinghelp.timetracking.originalestimate.title'"/></strong> - <ww:text name="'showtimetrackinghelp.timetracking.originalestimate.description'"/></li>
                        <li><strong><ww:text name="'showtimetrackinghelp.remainingestimate.title'"/></strong> - <ww:text name="'showtimetrackinghelp.remainingestimate.description.1'"/></li>

                    </ul>
                </li>
                <li>
                    <strong><ww:text name="'showtimetrackinghelp.timetracking.values.title'"/></strong>
                    <ul>
                        <li><ww:text name="'showtimetrackinghelp.timetracking.values.description.1'"/></li>
                        <li>
                            <ww:text name="'showtimetrackinghelp.timetracking.values.description.2'">
                                <ww:param name="'value0'"><ww:property value="defaultTimeUnit"/></ww:param>
                            </ww:text>
                        </li>
                        <li>
                            <ww:text name="'showtimetrackinghelp.timetracking.values.description.3'">
                                <ww:param name="'value0'"><ww:property value="daysPerWeek"/></ww:param>
                                <ww:param name="'value1'"><ww:property value="hoursPerDay"/></ww:param>
                            </ww:text>
                        </li>
                    </ul>
                </li>
            </ul>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
