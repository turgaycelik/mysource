<ww:if test="/metaAttributes && /metaAttributes/empty == false">
    <table id="metas_table" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'admin.workflows.property.key'"/>
                </th>
                <th>
                    <ww:text name="'admin.workflows.property.value'"/>
                </th>
                <ww:if test="workflow/editable == true">
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
                </ww:if>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/metaAttributes" status="'status'">
            <tr>
                <td><ww:property value="./key"/></td>
                <td><ww:property value="./value"/></td>
                <ww:if test="workflow/editable == true">
                    <td>
                        <ul class="operations-list">
                            <li><a id="del_meta_<ww:property value="./key"/>" href="<ww:url value="/removeAttributeUrl(./key)"/>"><ww:text name="'common.words.delete'"/></a></li>
                        </ul>
                    </td>
                </ww:if>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.workflows.nowdefinedproperties'"/></p>
        </aui:param>
    </aui:component>
</ww:else>
