<%@ taglib uri="webwork" prefix="ww" %>
<tr>
    <td class="fieldLabelArea"><ww:property value="parameters['label']"/></td>
    <ww:if test="parameters['escape'] == false">
        <td class="fieldValueArea"><ww:property value="parameters['value']" escape="false" />
    </ww:if>
    <ww:else>
        <td class="fieldValueArea"><ww:property value="parameters['value']"/>
    </ww:else>
        <div class="fieldDescription"><ww:property value="parameters['description']" escape="false" /></div>
    </td>
</tr>
