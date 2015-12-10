package com.atlassian.jira.util.system;

import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jira.JiraException;
import org.ofbiz.core.entity.GenericEntityException;

import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public interface SystemInfoUtils
{
    long MEGABYTE = 1048576;

    String getDatabaseType();

    String getDbDescriptorValue();

    String getDbDescriptorLabel();

    String getAppServer();

    String getInstallationType();

    String getUptime(ResourceBundle resourceBundle);

    long getTotalMemory();

    long getFreeMemory();

    long getUsedMemory();

    List<MemoryInformation> getMemoryPoolInformation();

    long getTotalPermGenMemory();

    long getFreePermGenMemory();

    long getUsedPermGenMemory();

    long getTotalNonHeapMemory();

    long getFreeNonHeapMemory();

    long getUsedNonHeapMemory();

    String getJvmInputArguments();

    DatabaseMetaData getDatabaseMetaData() throws GenericEntityException, JiraException, SQLException;

    long getFreeAllocatedMemory();

    long getUnAllocatedMemory();

    long getAllocatedMemory();

    public static interface DatabaseMetaData {

        public String getDatabaseProductVersion();

        public String getDriverName();

        public String getDriverVersion();

        public String getMaskedURL();

    }

}
