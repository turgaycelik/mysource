<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:if test="./enabled != true">
    <ww:text name="'admin.issuefields.customfields.not.configured.context'"/>
</ww:if>
<ww:else>
    <dl class="context-list">
        <dt><ww:text name="'admin.issuefields.customfields.issue.types'"/>:</dt>
        <ww:if test="./allIssueTypes == true">
            <dd><ww:text name="'admin.issuefields.customfields.global.all.issues'"/></dd>
        </ww:if>
        <ww:else>
            <ww:iterator value="./associatedIssueTypes" status="'status'">
                <ww:property value="/constantsManager/issueTypeObject(./string('id'))">
                    <dd class="context-issue-type">
                        <a href="<ww:url value="'EditIssueType!default.jspa'" atltoken="false"><ww:param name="'id'" value="id" /><ww:param name="'returnUrl'" value="'ViewCustomFields.jspa'" /></ww:url>">
                            <ww:component name="'customfieldicon'" template="constanticon.jsp">
                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                <ww:param name="'iconurl'" value="iconUrl"/> -
                                <ww:param name="'alt'"><ww:property value="nameTranslation"/></ww:param>
                                <ww:param name="'title'"><ww:property value="nameTranslation"/> - <ww:property value="descTranslation"/></ww:param>
                            </ww:component>
                        </a>
                    </dd>
                </ww:property>
            </ww:iterator>
        </ww:else>
        <ww:if test="./associatedProjectCategories != null && ./associatedProjectCategories/empty == false">
            <dt><ww:text name="'admin.issuefields.customfields.project.categories'"/>:</dt>
            <ww:iterator value="./associatedProjectCategories" status="'status'">
                <dd>
                    <a title="<ww:property value="string('name')" /><ww:property value="string('description')"><ww:if test=". && !./equals('')"> - <ww:property value="." /></ww:if></ww:property>" href="<ww:url value="'EditProjectCategory!default.jspa'" atltoken="false"><ww:param name="'returnUrl'" value="'ViewCustomFields.jspa'" /><ww:param name="'id'" value="long('id')" /></ww:url>"><ww:property value="string('name')"/></a>
                </dd>
            </ww:iterator>
        </ww:if>
        <ww:if test="./associatedProjects != null && ./associatedProjects/empty == false">
            <dt><ww:text name="'admin.issuefields.customfields.projects'"/>:</dt>
            <ww:iterator value="./associatedProjects" status="'status'">
                <dd>
                    <a title="<ww:property value="string('name')" /><ww:property value="string('description')"><ww:if test=". && !./equals('')"> - <ww:property value="." /></ww:if></ww:property>" href="<ww:url value="'/plugins/servlet/project-config/' + ./string('key') + '/summary'" atltoken="false" />"><ww:property value="string('name')"/></a>
                </dd>
            </ww:iterator>
        </ww:if>
    </dl>
</ww:else>
