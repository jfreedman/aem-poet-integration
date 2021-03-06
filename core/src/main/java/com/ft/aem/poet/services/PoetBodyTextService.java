package com.ft.aem.poet.services;

import org.apache.sling.api.resource.Resource;

/**
 * Service that can be overridden that defines logic for how body text is added to poet payload
 */
public interface PoetBodyTextService {
    String getBody(Resource resource);
}
