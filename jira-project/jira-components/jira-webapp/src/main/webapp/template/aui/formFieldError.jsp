<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:if test="parameters['errorCollectionKey']">
     <ww:property id="errorCollectionKey" value="parameters['errorCollectionKey']" />
</ww:if>
<ww:else>
     <ww:property id="errorCollectionKey" value="parameters['name']" />
</ww:else>
<ww:property value="errors[@errorCollectionKey]">
    <ww:if test="."><div<ww:property value="parameters['id']"><ww:if test="."> id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>-error"</ww:if></ww:property> data-field="<ww:property value="@errorCollectionKey" />" class="error"><ww:property value="errors[@errorCollectionKey]"/></div></ww:if>
</ww:property>