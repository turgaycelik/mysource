<%-- Expects a WorkflowFunctionBean to be in context with id 'descriptorBean' --%>
<ww:iterator value="@descriptorBean/descriptorCollection" status="'status'" >
    <ww:if test="@status/first == false">
        <div class="operator">
            <span style="color: green;">&mdash; <ww:text name="@descriptorBean/operatorTextKey"/></span>
        </div>
    </ww:if>

    <%@ include file="/includes/admin/workflow/viewworkflowdescriptor.jsp" %>
</ww:iterator>
