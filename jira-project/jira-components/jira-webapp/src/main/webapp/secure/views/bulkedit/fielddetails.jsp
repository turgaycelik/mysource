<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>

<ui:component name="'subTaskPhase'" template="hidden.jsp"  />

<ww:property value="/bulkEditBean/moveFieldLayoutItems">
    <ww:if test=". != null && ./empty == false">
        <table id="editbulkmovefields" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th><ww:text name="'bulk.move.fieldname'" /></th>
                    <th><ww:text name="'bulk.move.fieldvalue'" /></th>
                    <th><ww:text name="'bulk.move.retain'" /></th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value=".">
                <tr>
                    <td class="fieldLabelAreaTop">
                        <label for="retain_<ww:property value="./orderableField/id"/>">
                            <ww:property value="./orderableField/name"/><span class="icon icon-f-required"></span>
                        </label>
                    </td>
                    <ww:property value="/fieldHtml(.)">
                        <ww:if test="./length > 0">
                            <ww:property value="." escape="'false'"/>
                            <td class="cell-type-collapsed">
                                <input type="checkbox" id="retain_<ww:property value="../orderableField/id"/>" name="retain_<ww:property value="../orderableField/id"/>" value=""
                                        <ww:if test="/bulkEditBean/retainChecked(../orderableField/id) == true || /retainMandatory(../orderableField) == true">checked="checked"</ww:if>
                                        <ww:if test="/retainMandatory(../orderableField) == true">disabled="true"</ww:if>
                                        />
                                <ww:if test="/retainMandatory(../orderableField) == true"><input type="hidden" name="retain_<ww:property value="../orderableField/id"/>" value="true"/></ww:if>
                            </td>
                        </ww:if>
                        <ww:else>
                            <td colspan="2">
                                <aui:component template="auimessage.jsp" theme="'aui'">
                                    <aui:param name="'messageType'">warning</aui:param>
                                    <aui:param name="'messageHtml'">
                                        <p><ww:text name="'bulk.move.error.message'"/></p>
                                    </aui:param>
                                </aui:component>
                            </td>
                        </ww:else>
                    </ww:property>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'bulk.move.fields.noupdate'" /></p>
            </aui:param>
        </aui:component>
    </ww:else>
</ww:property>
