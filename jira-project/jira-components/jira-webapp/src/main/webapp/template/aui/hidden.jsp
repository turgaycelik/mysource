<%--

Required Parameters:
    * name                      - the NAME attribute of the input tag
    * id                        - The ID attribute of the input tag which will inherit the ID of the parent form
    * divId                     - The ID attribute of the wrapping div

Optional Parameters:
    * defaultValue              - Used when nameValue is not specified
    * cssClass                  - set additional CSS classes

Note:
    nameValue is retreived from the querystring via fieldName

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<div class="hidden"
     <ww:property value="parameters['divId']">
            <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
     </ww:property>
    >
    <input
        <ww:property value="parameters['id']">
            <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
        </ww:property>
        <ww:property value="parameters['name']">
            <ww:if test=".">name="<ww:property value="."/>"</ww:if>
        </ww:property>
        type="hidden"
        <ww:property value="parameters['nameValue']">
            <ww:if test=".">value="<ww:property value="."/>"</ww:if>
            <ww:elseIf test="parameters['defaultValue']">value="<ww:property value="parameters['defaultValue']" />"</ww:elseIf>
        </ww:property>
        <ww:property value="parameters['cssClass']">
            <ww:if test=".">class="<ww:property value="."/>"</ww:if>
        </ww:property>
    />
</div>
