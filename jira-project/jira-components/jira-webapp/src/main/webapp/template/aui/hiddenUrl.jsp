<%--

Required Parameters:
    * name                      - the NAME attribute of the input tag
    * id                        - The ID attribute of the input tag which will inherit the ID of the parent form

Optional Parameters:
    * defaultValue              - Used when nameValue is not specified
    * requestParams                TODO add support if necessary (this is a bit tricky, since ParamTags do not support nesting
                                        it might be done either by convention (e,g, param name="urlparam.*" ...) or by implementing
                                        composite parameter tags)

Note:
    nameValue is retreived from the Webwork stack via the 'name' param

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<div class="hidden">
    <input
        <ww:property value="parameters['id']">
            <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
        </ww:property>
        <ww:property value="parameters['name']">
            <ww:if test=".">name="<ww:property value="."/>"</ww:if>
        </ww:property>
        type="hidden"
        <ww:property value="parameters['nameValue']">
            <ww:if test=".">value="<ww:url value="." />"</ww:if>
            <ww:elseIf test="parameters['defaultValue']">value="<ww:url value="parameters['defaultValue']" />"</ww:elseIf>
        </ww:property>
    />
</div>
<jsp:include page="/template/aui/formFieldError.jsp" />
