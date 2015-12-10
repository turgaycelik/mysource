<tr class="filter_list">
    <td class="colHeaderLink bolded" style="width:65%"><ww:text name="'common.concepts.project'" /></td>
    <ww:if test=". != null && ./empty != true">
        <td class="colHeaderLink bolded" style="width:10%"><ww:text name="'common.concepts.key'" /></td>
        <td class="colHeaderLink bolded" style="width:25%"><ww:text name="'common.concepts.projectlead'" /></td>
    </ww:if>
</tr>
<ww:if test=". != null && ./empty != true">
<ww:iterator value="." status="'myStatus'">
    <tr id="pl_<ww:property value="./id" />" class="<ww:if test="@myStatus/modulus(2) == 1">rowNormal</ww:if><ww:else>rowAlternate</ww:else>">
        <td >
            <ww:property value="parameters['linkRenderer']/render(./id, ./name)" escape="false"/>
            <ww:if test="./description != null && ./description/length > 0">
                <br/><span class="subText"><ww:property value="/renderedProjectDescription(.)" escape="false" /></span>
            </ww:if>
        </td>
        <td><ww:property value="./key"/></td>
        <td>
            <ww:if test="./lead != null">
                <ww:property value="./lead/displayName"/> (<ww:property value="./lead/name"/>)
            </ww:if>
            <ww:else>
                <ww:text name="'browse.projects.no.lead'" />
            </ww:else>
        </td>
    </tr>
</ww:iterator>
</ww:if>
