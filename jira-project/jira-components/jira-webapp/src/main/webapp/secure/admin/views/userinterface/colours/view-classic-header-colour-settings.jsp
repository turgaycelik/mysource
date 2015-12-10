<%@ taglib uri="webwork" prefix="ww" %>

<tr>
    <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.bar.colour'"/></b></td>
    <td width=70%><ww:property value="/color('jira.lf.top.bgcolour')" escape="false"/></td>
</tr>
<tr>
    <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.bar.hilightcolour'"/></b></td>
    <td width=70%><ww:property value="/color('jira.lf.top.hilightcolour')" escape="false"/></td>
</tr>
<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.top.text.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.top.textcolour')" escape="false"/>
        <ww:if test="/showInvisibleWarningForTopText == true">
            &nbsp;&nbsp;<span class="warning"><ww:text name="'admin.globalsettings.lookandfeel.toptext.samecolour'"/></span>
        </ww:if>
    </td>
</tr>
<tr>
    <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.text.hilightcolour'"/></b></td>
    <td width=70%><ww:property value="/color('jira.lf.top.texthilightcolour')" escape="false"/></td>
</tr>
<tr>
    <td width=30%><b><ww:text name="'admin.globalsettings.lookandfeel.top.separatorcolor'"/></b></td>
    <td width=70%><ww:property value="/color('jira.lf.top.separator.bgcolor')" escape="false"/></td>
</tr>
<!-- =========== MENU BAR =================== -->
<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.menu.bar.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.menu.bgcolour')" escape="false"/></td>
</tr>
<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.menu.bar.text.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.menu.textcolour')" escape="false"/></td>
</tr>

<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.menu.bar.separator.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.menu.separator')" escape="false"/></td>
</tr>
<!-- =========== TEXT / LINK / HEADING =================== -->
<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.link.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.text.linkcolour')" escape="false"/></td>
</tr>
<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.link.active.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.text.activelinkcolour')" escape="false"/></td>
</tr>
<tr>
    <td><b><ww:text name="'admin.globalsettings.lookandfeel.heading.colour'"/></b></td>
    <td><ww:property value="/color('jira.lf.text.headingcolour')" escape="false"/></td>
</tr>
