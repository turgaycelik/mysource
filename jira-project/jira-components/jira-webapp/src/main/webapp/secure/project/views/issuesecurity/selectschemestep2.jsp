<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.iss.select.issue.security.scheme'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="title"><ww:text name="'admin.iss.associate.security.scheme.to.project'"/> <ww:property value="issueSecurityName" /></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.iss.step2'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text></p>
            <font size=1><ww:text name="'admin.iss.selecting.new.level'"/></font>
        </page:param>

        <page:param name="action">SelectProjectSecuritySchemeStep2.jspa</page:param>
        <page:param name="submitId">associate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>
        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/issuesecurity</page:param>
            <%--if there are no previous Security levels or affected Issues --%>
            <ww:if test="./originalSecurityLevels/size > 0 && ./totalAffectedIssues/size > 0">
                <tr bgcolor=#fffff0>
                    <td width><b><ww:text name="'admin.iss.security.levels.for'"/> <ww:property value="./securityScheme(origSchemeId)/name"/></b></td>
                    <ww:property value="./securityScheme(newSchemeId)/name">
                    <td><b>
                        <ww:if test="."><ww:text name="'admin.iss.security.levels.for'"/> <ww:property value="."/></ww:if>
                        <ww:else>&nbsp;</ww:else>
                    </b></td>
                    </ww:property>
                </tr>
                <%--Loop through each of the original schemes levels--%>
                <ww:property value="./originalSecurityLevels">
                <ww:iterator value="./keySet">
                    <%--If there are no affected issues for this level then dont bother --%>
                    <ww:if test="/affectedIssues(.)/size > 0">
                        <tr bgcolor=#ffffff>
                            <%--Show the original level and the number of affected issues--%>
                            <td>
                                <%--This hidden value is used for the action --%>
                                <ww:property value = "../(.)"/>
                                (<font size=1><ww:text name="'admin.iss.num.affected.issues'">
                                    <ww:param name="'value0'"><ww:property value ="/affectedIssues(.)/size"/></ww:param>
                                </ww:text></font>)
                            </td>
                            <td>
                                <%--If the new scheme is set to None then inform that security will be removed--%>
                                <ww:if test="../newSchemeId == null">
                                    <font color=#cc00000><ww:text name="'admin.iss.security.will.be.removed'"/><font>
                                </ww:if>
                                <ww:else>
                                    <%--Create a select box with teh values of all the levels of the new scheme--%>
                                    <ww:property value="/newSecurityLevels">
                                    <ww:if test="./size > 1">
                                    <select name="<ww:property value="/levelPrefix"/><ww:property value=".."/>">
                                        <ww:iterator value="./keySet">
                                            <ww:if test="../(.)">
                                                <option value="<ww:property value="." />"><ww:property value="../(.)" /></option>
                                            </ww:if>
                                            <ww:else>
                                                <option><ww:text name="'common.words.none'"/></option>
                                            </ww:else>
                                        </ww:iterator>
                                    </select>
                                    </ww:if>
                                    <ww:else>
                                    <font color=#cc00000><ww:text name="'admin.iss.security.will.be.removed'"/><font>
                                    </ww:else>
                                    </ww:property>
                                </ww:else>
                            </td>
                         </tr>
                     </ww:if>
                </ww:iterator>
                </ww:property>
            </ww:if>
            <ww:else>
                <page:param name="columns">1</page:param>
                    <tr bgcolor=#fffff0>
                        <td width><ww:text name="'admin.iss.no.previous.secured.issues'"/></td>
                    </tr>
            </ww:else>
        <%--These hidden values are used for the action--%>
        <ui:component name="'projectId'" template="hidden.jsp"/>
        <ww:if test="newSchemeId != null">
            <ui:component name="'newSchemeId'" template="hidden.jsp"/>
        </ww:if>
        <ww:if test="origSchemeId != null">
            <ui:component name="'origSchemeId'" template="hidden.jsp"/>
        </ww:if>
    </page:applyDecorator>
</body>
</html>
