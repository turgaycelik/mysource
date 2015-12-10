<%--

Required Parameters:
    * text                      - Unescaped text of the description (should be in <p> tags)

Notes:
    See http://confluence.atlassian.com/display/AUI/Forms for more information

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<div class="form-message<ww:property value="parameters['messageType']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property>">
    <ww:property value="parameters['text']" escape="false" />
</div>