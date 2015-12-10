<%--
    The root context of this include must be a BulkEditBean.
    Also require the action to have:
    * getStatusName(GV)
    * getFieldName(field)
    * getNewViewHtml(field)
--%>

<!-- Issue Targets Table - Target Project and Issue Type -->
<table id="move_confirm_table" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th colspan="2"><ww:text name="'bulk.move.issuetargets'" /></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td style="max-width: 200px; width: 200px; word-wrap: break-word;"><ww:text name="'bulk.move.targetproject'" /></td>
            <td>
                <img alt="" src="<ww:url value="'/secure/projectavatar'" atltoken="false"><ww:param name="'pid'" value="./targetProjectGV/string('id')" /><ww:param name="'size'" value="'small'" /></ww:url>" />
                <ww:property value="./targetProjectGV/string('name')" />
            </td>
        </tr>
    <ww:if test="./targetIssueTypeGV">
        <tr>
            <td><ww:text name="'bulk.move.targetissuetype'" /></td>
            <td>
                <ww:component name="'issuetype'" template="constanticon.jsp">
                  <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                  <ww:param name="'iconurl'" value="./targetIssueTypeGV/string('iconurl')" />
                  <ww:param name="'alt'"><ww:property value="./targetIssueTypeGV/string('name')" /></ww:param>
                </ww:component>
                <ww:property value="/nameTranslation(./targetIssueTypeGV)" />
            </td>
        </tr>
    </ww:if>
    </tbody>
</table>


<!-- Workflow/Status Table - Target Workflow and Status Mappings -->
<ww:property value="./statusMapHolder">
    <ww:if test=". != null && ./empty == false">
        <table id="status_map_table" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th colspan="2"><ww:text name="'bulk.move.workflow'"/></th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td style="max-width: 200px; width: 200px; word-wrap: break-word;"><ww:text name="'bulk.move.targetworkflow'" /></td>
                    <td><ww:property value="../targetWorkflow/name" /></td>
                </tr>
                <tr>
                    <td style="max-width: 200px; width: 200px; word-wrap: break-word;"><ww:text name="'bulk.move.status.mapping.confirm'" /></td>
                    <td>
                        <table class="bordered">
                            <tr>
                                <th nowrap><ww:text name="'bulk.move.status.original'" /></th>
                                <th>&nbsp;</th>
                                <th nowrap><ww:text name="'bulk.move.targetstatus'" /></th>
                            </tr>
                            <ww:iterator value=".">
                                <tr>
                                    <td width="1%" nowrap>
                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                            <ww:param name="'issueStatus'" value="/constantsManager/statusObject(./key)"/>
                                            <ww:param name="'isSubtle'" value="false"/>
                                            <ww:param name="'isCompact'" value="false"/>
                                        </ww:component>
                                    </td>
                                    <td width="1%">
                                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align=absmiddle>
                                    </td>
                                    <td nowrap>
                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                            <ww:param name="'issueStatus'" value="/constantsManager/statusObject(./value)"/>
                                            <ww:param name="'isSubtle'" value="false"/>
                                            <ww:param name="'isCompact'" value="false"/>
                                        </ww:component>
                                    </td>
                                </tr>
                            </ww:iterator>
                        </table>
                    </td>
                </tr>
            </tbody>
        </table>
    </ww:if>
</ww:property>


<!-- Updated Fields Table -->
<ww:property value="./moveFieldLayoutItems">
    <ww:if test=". != null && . /empty == false">
        <table class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th><ww:text name="'bulk.move.updatedfields'" /></th>
                    <th><ww:text name="'bulk.move.newvalue'"/></th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value=".">
                <tr>
                    <td style="max-width: 200px; width: 200px; word-wrap: break-word;"><ww:property value="/fieldName(./orderableField)" /></td>
                    <td>
                        <ww:if test="/fieldUsingSubstitutions(../.., ./orderableField) == true" >
                            <table id="<ww:property value="../../key" /><ww:property value="./orderableField/id" />">
                                <ww:iterator value="/substitutionsForField(../.., ./orderableField)/entrySet">
                                    <tr>
                                        <td width="1%" nowrap><ww:property value="/mappingViewHtml(../../.., ../orderableField, ./key, 'true')" escape="'false'" /></td>
                                        <td width="1%"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align=absmiddle></td>
                                        <td nowrap><ww:property value="/mappingViewHtml(../../.., ../orderableField, ./value, 'false')" escape="'false'" /></td>
                                    </tr>
                                </ww:iterator>
                            </table>
                        </ww:if>
                        <ww:else>
                            <ww:property value="/newViewHtml(../.., ./orderableField)" escape="'false'" />
                        </ww:else>
                    </td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:if>
</ww:property>

<!-- Removed Fields Table -->
<ww:property value="./removedFields">
    <ww:if test=". != null && . /empty == false">
        <table id="removed_fields_table" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th><ww:text name="'bulk.move.removedfields'" /></th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value=".">
                <tr>
                    <td><ww:property value="/fieldName(.)" /></td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:if>
</ww:property>
