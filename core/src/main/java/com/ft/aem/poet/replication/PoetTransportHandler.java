package com.ft.aem.poet.replication;

import com.day.cq.replication.*;
import com.ft.aem.poet.PoetException;
import com.ft.aem.poet.frost.FrostHttpUtil;
import com.ft.aem.poet.frost.FrostResponse;
import com.ft.aem.poet.models.PoetConfigSlingModel;
import com.ft.aem.poet.models.PoetPayload;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transport handler for posting content to po.et and storing work id returned
 */
@Component(immediate = true,
        name = "PoetTransportHandler",
        service = TransportHandler.class,
        property = "name=PoetTransportHandler")
public class PoetTransportHandler implements TransportHandler {

    private static final Logger log = LoggerFactory.getLogger(PoetTransportHandler.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;


    /**
     * Is this agent configured to handle poet replication
     *
     * @param agentConfig configuration of the agent
     * @return boolean indicating if agent can handle event
     */
    public boolean canHandle(final AgentConfig agentConfig) {
        if (agentConfig != null && agentConfig.isEnabled()) {
            String serializationType = agentConfig.getSerializationType();
            return serializationType.equals("poet");
        }
        return false;
    }

    /**
     * Delivers the replication payload to Po.et
     *
     * @param transportContext       the transport context
     * @param replicationTransaction the replication transaction
     * @return result of replication
     * @throws ReplicationException of error
     */
    public ReplicationResult deliver(final TransportContext transportContext, final ReplicationTransaction replicationTransaction)
            throws ReplicationException {

        ResourceResolver resourceResolver = null;
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        serviceInfo.put(ResourceResolverFactory.SUBSERVICE, "poet-replication-service");
        try {
            if(!replicationTransaction.getAction().getType().equals(ReplicationActionType.ACTIVATE)) {
                // no action if we aren't activating
                return ReplicationResult.OK;
            }
            if(replicationTransaction.getContent().getInputStream()==null) {
                log.warn("no replication payload found for " + replicationTransaction.getAction().getPath() + " not submitting to Po.et");
                return ReplicationResult.OK;
            }
            // ensure payload is valid before posting
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(serviceInfo);
            if(FrostHttpUtil.registerContent(resourceResolver, replicationTransaction.getAction().getPath(), IOUtils.toString(replicationTransaction.getContent().getInputStream()), false)) {
                return ReplicationResult.OK;
            } else {
                    return new ReplicationResult(false, 500, "non 200 response returned from frost");
                }
        } catch (Exception e) {
            log.warn("error submitting to po.et", e);
            return new ReplicationResult(false, 500, "error submitting content to poet");
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

    /**
     * Polls for relication result
     *
     * @param ctx     transport context of event
     * @param tx      transaction for replication
     * @param result  the replication result
     * @param factory the replication factory
     * @return replication result of poll
     * @throws ReplicationException if error occurs
     */
    public ReplicationResult poll(TransportContext ctx, ReplicationTransaction tx, List<ReplicationContent> result, ReplicationContentFactory factory) throws ReplicationException {
        String msg = "Unsupported operation.";
        throw new ReplicationException(msg);
    }


}
