package com.atlassian.core.ofbiz.test.mock;

import org.ofbiz.core.entity.SequenceUtil;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class MockSequenceUtil extends SequenceUtil {

    private final Map<String, Long> sequences = new HashMap<String, Long>();

    public MockSequenceUtil(String helperName, ModelEntity seqEntity, String nameFieldName, String idFieldName) {
        super(helperName, seqEntity, nameFieldName, idFieldName);
    }

    public Long getNextSeqId(String seqName) {
        Long id;

        synchronized (sequences) {
            id = sequences.get(seqName);

            if (id == null) {
                id = 0L;
            }

            id += 1;

            sequences.put(seqName, id);
        }

        return id;
    }
}
