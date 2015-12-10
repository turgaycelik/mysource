<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="ww" %>
<ui:component name="'workflowName'" value="../workflow/name" template="hidden.jsp" theme="."/>
<ui:component name="'workflowMode'" value="../workflow/mode" template="hidden.jsp" theme="."/>
<ww:if test="../project != null">
    <ui:component name="'project'" value="../project" template="hidden.jsp" theme="."/>
</ww:if>
<ww:if test="../issueType != null">
    <ui:component name="'issueType'" value="../issueType" template="hidden.jsp" theme="."/>
</ww:if>