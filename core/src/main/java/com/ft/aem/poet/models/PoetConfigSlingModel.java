package com.ft.aem.poet.models;

import com.adobe.granite.crypto.CryptoSupport;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an individual cloud services configuration for po.et
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PoetConfigSlingModel {
    private static final Logger logger = LoggerFactory.getLogger(PoetConfigSlingModel.class);
    @Inject
    private boolean abandonOnFailure;
    @Inject
    private String frostKey;
    @Inject
    private String[] mappings;
    @Inject
    private String uri;
    @Inject
    private CryptoSupport cryptoSupport;

    /**
     * frost token
     *
     * @return token
     */
    public String getToken() {
        try {
            return cryptoSupport.unprotect(frostKey);
        } catch (Exception ex) {
            logger.error("error decrypting frost key", ex);
            return null;
        }
    }

    /**
     * frost uri
     *
     * @return uri
     */
    public String getUri() {
        try {
            return uri;
        } catch (Exception ex) {
            logger.error("error getting frost uri", ex);
            return null;
        }
    }

    /**
     * should replication retry Frost failures or abandon replication events
     *
     * @return should we retry
     */
    public Boolean getAbandonOnFailure() {
        return abandonOnFailure;
    }


    /**
     * returns mappings of resource types and the path to the paragraph system containing the text for the page
     *
     * @return mappings of resource types and paragraph system paths
     */
    public Map<String, String> getMappings() {
        Map<String, String> resolvedMappings = new HashMap<>();
        if(mappings!=null) {
            for (int i = 0; i < mappings.length; i++) {
                String val = mappings[i];
                if (val == null || val.isEmpty() || !val.contains("=")) {
                    continue;
                }
                String[] split = val.split("=");
                if (split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
                    continue;
                }
                resolvedMappings.put(split[0].trim(), split[1].trim());
            }
        }
        return resolvedMappings;
    }

}