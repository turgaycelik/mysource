<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.issuesecurity.delete.level'"/>:</title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="security_schemes"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.delete.level'"/>: <ww:property value="issueSecurityName" /></page:param>
    <page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.schemes.issuesecurity.delete.level.confirmation'"/></p>
            <ww:if test="affectedIssues/size > 0">
                <p><ww:text name="'admin.schemes.issuesecurity.delete.issues.currently.set'"/></p>
            </ww:if>
            </aui:param>
        </aui:component>
    <ww:if test="default(levelId)== true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.schemes.issuesecurity.delete.level.warning'"/><p>
            </aui:param>
        </aui:component>
    </ww:if>
    </page:param>

    <page:param name="action">DeleteIssueSecurityLevel.jspa</page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="cancelURI"><ww:url page="EditIssueSecurities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
    <ww:if test="affectedIssues/size > 0">
        <ui:component label="text('admin.schemes.issuesecurity.delete.level.issues.with.this.level')" name="'affectedIssues/size'" template="textlabel.jsp" >
            <ui:param name="'description'">

                <ww:iterator value="affectedIssues" >
                    <a href="<%= request.getContextPath() %>/browse/<ww:property value="string('key')" />"><ww:property value="string('key')" /></a>
                    <ww:property value="string('summary')" /> <br>
                </ww:iterator>

            </ui:param>
        </ui:component>
        <ww:if test="otherLevels/size > 0">
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="radio" name="affectsAction" value="swap" checked="checked"/>
                    <ww:text name="'admin.schemes.issuesecurity.delete.level.swap.to'"/>:
                    <ww:property value="otherLevels">
                        <select name="swapLevel">
                            <option value=-1><ww:text name="'common.words.none'"/></option>
                            <ww:iterator value="./keySet">
                                <option value="<ww:property value="." />" >
                                    <ww:property value="../(.)" />
                                </option>
                            </ww:iterator>
                        </select>
                     </ww:property>
                </td>
            </tr>
        </ww:if>
        <ww:else>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="hidden" name="affectsAction" value="remove"/>
                    <ww:text name="'admin.schemes.issuesecurity.delete.level.no.other.levels'"/>
                </td>
            </tr>
        </ww:else>
    </ww:if>

    <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />"/>
    <input type="hidden" name="levelId" value="<ww:property value="levelId" />"/>
    <input type="hidden" name="confirm" value="true"/>

</page:applyDecorator>
</body>
</html>
