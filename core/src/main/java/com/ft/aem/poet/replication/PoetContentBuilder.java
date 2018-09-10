package com.ft.aem.poet.replication;

import com.day.cq.replication.*;
import com.ft.aem.poet.models.PoetPayload;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Content builder implementation for submitting content to poet
 */
@Component(
        immediate = true,
        name = "PoetContentBuilder",
        service = ContentBuilder.class,
        property = {
                "name=poet",
                "title=Po.et Blockchain Network"
        }
)
public class PoetContentBuilder implements ContentBuilder {

    private static final Logger log = LoggerFactory.getLogger(PoetContentBuilder.class);
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * returns the json payload submitted to Po.et
     *
     * @param session the current session
     * @param action  the replication action
     * @param factory the replication content factory
     * @return The replication content to use
     * @throws ReplicationException if error occurs
     */
    public ReplicationContent create(Session session, ReplicationAction action, ReplicationContentFactory factory, Map<String, Object> map) throws ReplicationException {
        // we only send activation events to po.et
        log.error("creating content payload");
        if (action.getType() != ReplicationActionType.ACTIVATE) {
            return ReplicationContent.VOID;
        }
        ResourceResolver resourceResolver = null;
        try {
            Map<String, Object> auth = new HashMap<>();
            auth.put("user.jcr.session", session);
            resourceResolver = resourceResolverFactory.getResourceResolver(auth);
            Resource resource = resourceResolver.getResource(action.getPath());
            PoetPayload payload = resource.adaptTo(PoetPayload.class);
            if (payload == null) {
                return ReplicationContent.VOID;
            }
            Path tempFile;

            try {
                tempFile = Files.createTempFile("poet_content_agent", ".tmp");
            } catch (IOException e) {
                throw new ReplicationException("Could not create temporary file", e);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, Charset.forName("UTF-8"))) {
                writer.write(payload.getPayload().toString());
                writer.flush();
                return factory.create("application/json", tempFile.toFile(), true);
            } catch (IOException e) {
                throw new ReplicationException("Could not write to temporary file", e);
            }
        } catch (LoginException le) {
            log.error("could not access content");
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        return ReplicationContent.VOID;
    }

    /**
     * returns the json payload submitted to Po.et
     *
     * @param session the current session
     * @param action  the replication action
     * @param factory the replication content factory
     * @return The replication content to use
     * @throws ReplicationException if error occurs
     */
    public ReplicationContent create(Session session, ReplicationAction action, ReplicationContentFactory factory) throws ReplicationException {
        return create(session, action, factory, null);
    }

    /**
     * the name of the content builder must match the replication agent type in the UI dropdown
     *
     * @return name of content builder to be used by CQ UI
     */
    public String getName() {
        return "poet";
    }

    /**
     * the title of the content builder
     *
     * @return title of content builder
     */
    public String getTitle() {
        return "Po.et blockchain network";
    }

}
