<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.integritychecker.integrity.checker'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="integrity_checker"/>       
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.integritychecker.integrity.checker'"/></page:param>
    <page:param name="action">IntegrityChecker.jspa</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="buttons">
        <input class="aui-button" type="button" name="okbutton" value="<ww:text name="'admin.common.words.ok'"/>" onclick="location.href='IntegrityChecker!default.jspa'"/>
    </page:param>

    <page:param name="description">
        <ww:if test="/totalResults > 0">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">success</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.integritychecker.x.errors.corrected'">
                           <ww:param name="'value0'"><ww:property value="/totalResults"/></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.integritychecker.no.errors.corrected'"/></p>
                </aui:param>
            </aui:component>
        </ww:else>
    </page:param>

    <tr>
        <td>
            <ul>
            <ww:iterator value="integrityChecks" status="'status'">
                <li>
                <ww:property value="./description" />
                    <ul>
                    <ww:iterator value="./checks">
                        <li>
                            <%-- Check if the action is available--%>
                            <ww:if test="available == false">
                                <span class="secondary-text"><ww:property value="./description"/> - <ww:property value="unavailableMessage"/></span>
                            </ww:if>
                            <ww:else>
                                <%-- Work out the color for the check --%>
                                <ww:if test="/results/(.) != null && /results/(.)/empty == false">
                                    <ww:if test="/hasWarningResults(.) == true">
                                        <span class="status-correctable">
                                    </ww:if>
                                    <ww:else>
                                        <span class="status-active">
                                    </ww:else>
                                </ww:if>
                                <ww:else>
                                    <span>
                                </ww:else>

                                <ww:property value="./description"/>
                                </span>
                                <ww:if test="/results/(.) != null && /results/(.)/empty == false">
                                        <ww:iterator value="/results/(.)">
                                        <div class="integrity-checks">
                                            <p>
                                            <jira:linkbugkeys>
                                                <%-- determine the color of the message --%>
                                                <ww:if test="./warning == true">
                                                    <span class="status-unfixable"><ww:text name="'admin.integritychecker.UNFIXABLE.ERROR'"/></span>:
                                                </ww:if>
                                                <ww:else>
                                                    <span class="status-active"><ww:text name="'admin.integritychecker.FIXED'"/></span>:
                                                </ww:else>
                                                <ww:property value="./message" escape="false" />
                                            </jira:linkbugkeys>

                                            <ww:if test="./bugId != null && ./bugId/trim/length > 0">
                                                (<a href="http://jira.atlassian.com/browse/<ww:property value="./bugId"/>"><ww:property value="./bugId"/></a>)
                                            </ww:if>
                                            </p>
                                        </div>
                                        </ww:iterator>
                                </ww:if>
                                </span>
                            </ww:else>
                        </li>
                    </ww:iterator>
                    </ul>
                </li>
            </ww:iterator>
            </ul>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
