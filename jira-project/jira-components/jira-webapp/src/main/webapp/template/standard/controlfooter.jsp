<%@ taglib uri="webwork" prefix="ww" %>
<ww:property value="parameters['helpURL']"><ww:if test=".">
    <ww:component template="help.jsp" name="." >
        <ww:param name="'noalign'" value="true" />
        <ww:param name="'helpURLFragment'" value="parameters['helpURLFragment']"/>
    </ww:component>
</ww:if></ww:property>
<ww:property value="parameters['extrahtml']"><ww:if test="."><ww:property value="." escape="false" /></ww:if></ww:property>
<ww:property value="parameters['description']"><ww:if test="."><div class="fieldDescription"><ww:property value="." escape="false" /></div></ww:if></ww:property>
	</td>
<ww:if test="!parameters['nolabel']">
</tr>
</ww:if>
