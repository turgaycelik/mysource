package com.atlassian.jira.bc.dataimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.dataimport.DataImportService.ImportError;
import com.atlassian.jira.bc.dataimport.DataImportService.ImportResult;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.module.propertyset.PropertySet;

public class DefaultImportResultStore implements ImportResultStore, Startable
{
    private static final Logger log = LoggerFactory.getLogger(DefaultImportResultStore.class); 
    
    protected static final String PS_KEY = "admin.import.result";
    protected static final String IMPORT_ERROR_KEY = "import.error";
    protected static final String SPECIFIC_ERROR_MESSAGE_KEY = "specific.error.message";
    
    private EventPublisher eventPublisher;
    @ClusterSafe("This is only used by the studio importer and as it is an import, the database is completely replaced. Only ever written once.")
    private ResettableLazyReference<PropertySet> propertiesReference;

    public DefaultImportResultStore(final JiraPropertySetFactory jiraPropertySetFactory, final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.propertiesReference = new ResettableLazyReference<PropertySet>()
        {
            protected PropertySet create() throws Exception
            {
                return jiraPropertySetFactory.buildNoncachingPropertySet(PS_KEY, 0L);
            }
        };
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public synchronized void onClearCache(final ClearCacheEvent event)
    {
        propertiesReference.reset();
    }

    @Override
    @ClusterSafe("This is only used by the studio importer and as it is an import, the database is completely replaced.")
    public synchronized void storeResult(ImportResult importResult)
    {
        ImportError importError = importResult.getImportError();
        if (importError != null)
        {
            log.info("Storing import result with error type: {}", importError.name());
            propertiesReference.get().setString(IMPORT_ERROR_KEY, importError.name());
            String specificErrorMessage = importResult.getSpecificErrorMessage();
            propertiesReference.get().setText(SPECIFIC_ERROR_MESSAGE_KEY, specificErrorMessage == null ? "" : specificErrorMessage);
        }
        else
        {
            log.warn("Could not store import result.");
        }
    }

    @Override
    @ClusterSafe("This is only used by the studio importer and as it is an import, the database is completely replaced.")
    public synchronized ImportResult getLastResult()
    {
        if (propertiesReference.get().exists(IMPORT_ERROR_KEY))
        {
            String importErrorType = propertiesReference.get().getString(IMPORT_ERROR_KEY);
            try
            {
                ImportError importError = ImportError.valueOf(importErrorType);
                String message = propertiesReference.get().getText(SPECIFIC_ERROR_MESSAGE_KEY);
                
                ImportResult.Builder builder = new ImportResult.Builder(null);
                builder.setSpecificError(importError, message);
                
                log.info("Retrieved last import result. It has error type: {}", importErrorType);
                return builder.build();
            }
            catch (IllegalArgumentException e)
            {
                log.warn("An unrecognized error type was stored, clearing it: {}", importErrorType);
                clear();
            }
        }
        log.info("No import result retrieved.");
        return null;
    }

    @Override
    @ClusterSafe("This is only used by the studio importer and as it is an import, the database is completely replaced.")
    public synchronized void clear()
    {
        log.info("Clearing last import result.");
        
        if (propertiesReference.get().exists(IMPORT_ERROR_KEY))
        {
            propertiesReference.get().remove(IMPORT_ERROR_KEY);
        }
        if (propertiesReference.get().exists(SPECIFIC_ERROR_MESSAGE_KEY))
        {
            propertiesReference.get().remove(SPECIFIC_ERROR_MESSAGE_KEY);
        }        
    }

}
