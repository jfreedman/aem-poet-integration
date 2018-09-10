package com.ft.aem.poet.services;

import org.apache.sling.api.resource.Resource;

/**
 * Service that can be overridden that defines logic for how author name is added to poet payload
 */
public interface PoetAuthorService {
    String getAuthor(Resource resource);
}
