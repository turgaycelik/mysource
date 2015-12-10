<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<jsp:include page="/template/aui/formFieldLabel.jsp"/>

<ww:if test="parameters['disabled'] == 'true'">
    <input type="text" class="aui-ss-disabled text" name="<ww:property value="parameters['name']" />" id="<ww:property value="parameters['id']" />"
    <ww:if test="parameters['userName']">
        value="<ww:property value="parameters['userName']" />"
    </ww:if>
    <ww:elseIf test="parameters['inputText']">
        value="<ww:property value="parameters['inputText']" />"
    </ww:elseIf>
    />
</ww:if>
<ww:else>
    <select <ww:if test="parameters['inputText']"> data-input-text="<ww:property value="parameters['inputText']" />"</ww:if> id="<ww:property value="parameters['id']" />" class="js-default-user-picker single-user-picker" name="<ww:property value="parameters['name']" />">
        <ww:if test="parameters['userName'] && parameters['inputText'] == null">
            <option selected="selected"
                    style="background-image:url('<ww:property value="parameters['userAvatar']" />')"
                    value="<ww:property value="parameters['userName']" />">
                <ww:if test="parameters['userFullName']">
                    <ww:property value="parameters['userFullName']"/>
                </ww:if>
                <ww:else>
                    <ww:property value="parameters['userName']"/>
                </ww:else>
            </option>
        </ww:if>
    </select>
    <ww:property value="parameters['description']"><ww:if test="."><div class="description"><ww:property value="." escape="false" /></div></ww:if></ww:property>
</ww:else>
<jsp:include page="/template/aui/formFieldError.jsp"/>
<jsp:include page="/template/aui/formFieldIcon.jsp"/>
