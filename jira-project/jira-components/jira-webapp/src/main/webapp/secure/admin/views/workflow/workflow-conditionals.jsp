<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<%-- Show conditional results (if any) --%>
<ww:if test="transition/conditionalResults && transition/conditionalResults/empty == false">
    <h3><ww:text name="'admin.workflowtransition.conditional.results'"/></h3>

    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th><ww:text name="'admin.workflowtransition.destinationstep'"/></th>
                <th><ww:text name="'admin.workflowtransition.linked.status'"/></th>
                <th width=1%><ww:text name="'common.words.operations'"/></th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="transition/conditionalResults" status="'status'">
            <tr>
                <ww:property value="/stepDescriptor(.)">
                    <td>
                        <b><ww:property value="./name"/></b>
                    </td>
                    <td>
                        <ww:if test="metaAttributes/('jira.status.id')">
                        <ww:property value="metaAttributes/('jira.status.id')">
                            <ww:property value="/status(.)">
                                <ww:component name="'status'" template="constanticon.jsp">
                                    <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                    <ww:param name="'iconurl'" value="./string('iconurl')" />
                                    <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                                    <ww:param name="'title'"><ww:property value="./string('name')" /> - <ww:property value="./string('description')" /></ww:param>
                                </ww:component>
                                <ww:property value="./string('name')" />
                            </ww:property>
                        </ww:property>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'admin.workflowtransition.no.linked.status'"/>
                        </ww:else>
                    </td>
                </ww:property>
                <td>
                    <a id="conditional_step_xml_<ww:property value="@status/count"/>" href="<ww:url page="ViewWorkflowTransitionConditionalResult.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'resultCount'" value="@status/count" /></ww:url>"><ww:text name="'admin.common.words.xml'"/></a>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>

</ww:if>
