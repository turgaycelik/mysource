<%@ taglib uri="webwork" prefix="ww" %>
<ww:property value="parameters['nameValue']">
<ww:if test=". == null || ./rows.size == 0 || ./total == 0 ">
    <td style="width:100%; background-color:#f0f0f0;"><img src="<%= request.getContextPath() %>/images/border/spacer.gif" style="height:10px; width:100%; border-width:0" alt="" /></td>
</ww:if>
<ww:else>
    <ww:iterator value="rows">
        <ww:if test="../percentage(.) != 0" >
            <td style="width:<ww:property value="../percentage(.)"/>%; background-color:<ww:property value="color" />" ><img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                    style="height:10px; width:100%; border-width:0" class="hideOnPrint"
                    title="<ww:property value="./description" />"
                    alt="<ww:property value="./description" />" ></td>
        </ww:if>
    </ww:iterator>
</ww:else>
</ww:property>
