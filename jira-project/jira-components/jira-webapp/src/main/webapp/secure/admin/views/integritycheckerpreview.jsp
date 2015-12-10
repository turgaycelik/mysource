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
<script language="JavaScript">
    function changeAll(checkbox)
    {
        checkStartingWith("<ww:property value="/integrityCheckPrefix"/>", checkbox.checked);
    }

    function changeIntegrity(checkbox)
    {
        checkStartingWith("<ww:property value="/checkPrefix"/>" + checkbox.name.split("_")[1], checkbox.checked);
    }

    function checkStartingWith(prefix, checked)
    {
        var elements = document.forms['jiraform'].elements;
        for (var i=0;i<elements.length;i++)
        {
            if (elements[i].name.indexOf(prefix) == 0)
            {
                elements[i].checked = checked;
            }
        }
    }
</script>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.integritychecker.integrity.checker'"/></page:param>
    <page:param name="action">IntegrityChecker.jspa</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="columns">1</page:param>
    <ww:if test="/hasCorrectableResults == true">
        <page:param name="leftButtons">
            <input class="aui-button" type="submit" name="fix" value="<ww:text name="'admin.common.words.fix'"/>" />
        </page:param>
    </ww:if>
    <page:param name="buttons">
        <input class="aui-button" type="submit" name="back" value="<ww:text name="'admin.common.words.back'"/>" />
    </page:param>

    <page:param name="description">
        <ww:if test="/hasCorrectableResults == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.integritychecker.choose'"/></p>
                </aui:param>
            </aui:component>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">success</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.integritychecker.no.errors'"/></p>
                </aui:param>
            </aui:component>
        </ww:else>
    </page:param>

    <tr>
        <td>
            <table class="aui aui-table-rowhover">
                <thead>
                    <tr>
                        <th width="1%">
                            <ww:if test="/hasCorrectableResults == true">
                                <input id="selectAllChecks" type="checkbox" name="all" onClick="changeAll(this)">
                            </ww:if>
                            <ww:else>
                                &nbsp;
                            </ww:else>
                        </th>
                        <th>
                            <ww:if test="/hasCorrectableResults == true">
                                <label for="selectAllChecks"><ww:text name="'admin.integritychecker.fix.all'"/></label>
                            </ww:if>
                            <ww:else>
                                &nbsp;
                            </ww:else>
                        </th>
                    </tr>
                </thead>
                <tbody>
                <ww:iterator value="integrityChecks" status="'status'">
                    <tr>
                        <td>
                            <%-- Only show the check box if we have correctable results for this integrity check --%>
                            <ww:if test="/integrityCheckAvailable(.) == true">
                                <input type="checkbox" id="integrity_<ww:property value="./id"/>" name="integrity_<ww:property value="./id"/>" value="<ww:property value="./id"/>" onClick="changeIntegrity(this)">&nbsp;&nbsp;
                            </ww:if>
                            <ww:else>
                                &nbsp;
                            </ww:else>
                        </td>
                        <td>
                            <label for="integrity_<ww:property value="./id"/>"><ww:property value="./description" /></label>
                            <ww:iterator value="./checks">
                            <div class="integrity-checks">
                                <%-- Check if the action is available--%>
                                <ww:if test="available == false">
                                    <div class="secondary-text"><ww:property value="./description"/> - <ww:property value="unavailableMessage"/></div>
                                </ww:if>
                                <ww:else>
                                    <%-- Ensure that if the check box --%>
                                    <ww:if test="/checkAvailable(.) == true">
                                        <ww:component name="/checkId(.)" value="/checked(.)" label="''" template="checkbox.jsp" theme="'single'" >
                                            <ww:param name="'fieldValue'" value="./id" />
                                        </ww:component>
                                    </ww:if>
                                    
                                    <%-- Work out the color for the check --%>
                                    <ww:if test="/checked(.) == true">
                                        <ww:if test="/results/(.) != null && /results/(.)/empty == false">
                                            <ww:if test="/hasCorrectableResults(.) == true">
                                                <span class="status-inactive">
                                            </ww:if>
                                            <ww:else>
                                                <span class="status-correctable">
                                            </ww:else>
                                        </ww:if>
                                        <ww:else>
                                            <span><span class="status-active"><ww:text name="'admin.integritychecker.PASSED'"/></span>:
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
                                            <%-- determine the color of the message --%>
                                            <ww:if test="./error == true">
                                                <span class="status-inactive"><ww:text name="'admin.integritychecker.ERROR'"/></span>:
                                            </ww:if>
                                            <ww:else>
                                                <span class="status-unfixable"><ww:text name="'admin.integritychecker.UNFIXABLE.ERROR'"/></span>:
                                            </ww:else>

                                            <jira:linkbugkeys>
                                                <ww:property value="./message" escape="false" />
                                            </jira:linkbugkeys>

                                            <ww:if test="./bugId != null && ./bugId/trim/length > 0">
                                                (<a href="http://jira.atlassian.com/browse/key=<ww:property value="./bugId"/>"><ww:property value="./bugId"/></a>)
                                            </ww:if>
                                            </p>
                                        </div>
                                        </ww:iterator>
                                    </ww:if>
                                </ww:else>
                            </div>
                            </ww:iterator>
                        </td>
                    </tr>
                </ww:iterator>
                </tbody>
            </table>
        </td>
    </tr>

</page:applyDecorator>
</body>
</html>
