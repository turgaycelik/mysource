<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<jsp:include page="/template/aui/formFieldLabel.jsp" />
<ww:property value="parameters['issueType']">
    <ww:if test=".">
        <ww:if test="./parameters['iconurl']/startsWith('http://') == true || ./parameters['iconurl']/startsWith('https://') == true">
            <img alt="<ww:property value="/nameTranslation(.)" escape="false" />" height="16" src="<ww:property value="./string('iconurl')" />" title="<ww:property value="/nameTranslation(.)" escape="false" /> - <ww:property value="/descTranslation(.)" escape="false" />" width="16" />
        </ww:if>
        <ww:else>
            <img alt="<ww:property value="/nameTranslation(.)" escape="false" />" height="16" src="<%= request.getContextPath() %><ww:property value="./string('iconurl')" />" title="<ww:property value="/nameTranslation(.)" escape="false" /> - <ww:property value="/descTranslation(.)" escape="false" />" width="16" />
        </ww:else>
        <span class="field-value"<ww:property value="parameters['id']"><ww:if test="."> id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if></ww:property>><ww:property value="/nameTranslation(.)" /></span>
    </ww:if>
</ww:property>
