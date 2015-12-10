<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<jsp:include page="/template/aui/formFieldLabel.jsp" />
<ww:property value="parameters['nameValue']"><ww:if test="."><span class="field-value"<ww:property value="parameters['id']"><ww:if test="."> id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if></ww:property>><ww:property value="."/></span></ww:if></ww:property>
<ww:property value="parameters['texthtml']"><ww:if test="."><span class="field-value"><ww:property value="." escape="false"/></span></ww:if></ww:property>
<jsp:include page="/template/aui/formFieldIcon.jsp" />
