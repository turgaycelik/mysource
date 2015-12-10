<%@ taglib uri="webwork" prefix="ww" %>
<ww:if test="errors[parameters['name']]">
<ww:if test="!parameters['noTable']"><td class="formErrors <ww:if test="parameters['class']"><ww:property value="parameters['class']"/></ww:if>"></ww:if>
<span class="errMsg"><ww:property value="errors[parameters['name']]"/></span><br /></ww:if>
<ww:else>
<ww:if test="!parameters['noTable']"><td <ww:if test="parameters['bgcolor']"> bgcolor="<ww:property value="parameters['bgcolor']"/>"</ww:if> <ww:if test="parameters['class']">class=<ww:property value="parameters['class']"/></ww:if>></ww:if>
</ww:else>

