<%-- the reason this file looks so messed is that we are trying to avoid any spaces in the file.  WL 7.0 SP4 also can't handle any spaces at the end of closing tags eg. </ww:else > kill it
--%><%@ taglib uri="webwork" prefix="ww" %><ww:if test="./releaseDate != null"
><ww:if test="versionArchived(.) == true"
><span class="grey" title="<ww:text name="'version.releasedate'"/>"></ww:if><ww:elseIf
 test="/versionManager/versionOverDue(.) == true && versionReleased(.) == false"><span
class="warning" title="<ww:text name="'version.releasedate.past'"/>"></ww:elseIf><ww:else><span
title="<ww:text name="'version.releasedate'"/>"></ww:else><ww:property value="/dateFieldFormat/format(./releaseDate)" /></span></ww:if>
