<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<%--<ww:generator id="hiddenFields" val="parameters['fields']" separator="','"/>--%>
<ww:property value="parameters['fields']">
    <ww:if test=".">
        <jira:split value="." separator="," />
        <ww:iterator value=".">
            <ui:component name="." template="hidden.jsp" theme="'aui'" />
        </ww:iterator>
    </ww:if>
</ww:property>

