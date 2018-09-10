package com.ft.aem.poet.workflow;


import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.ft.aem.poet.frost.FrostHttpUtil;
import com.ft.aem.poet.models.PoetConfigSlingModel;
import com.ft.aem.poet.models.PoetPayload;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


@Component(
        immediate = true,
        name = "FrostWorkflow",
        service = WorkflowProcess.class
)
public class FrostWorkflow implements WorkflowProcess {


    private final Logger log = LoggerFactory
            .getLogger(FrostWorkflow.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * Submits asset to po.et
     *
     * @param workItem        workflow work item
     * @param workflowSession workflow session
     * @param metadataMap     metadata for workflow
     */
    @Override
    public final void execute(WorkItem workItem, WorkflowSession workflowSession,
                              MetaDataMap metadataMap)
            throws WorkflowException {
        ResourceResolver resourceResolver = null;

        String path = workItem.getWorkflowData().getPayload().toString();
        try {
            Map<String, Object> serviceInfo = new HashMap<String, Object>();
            serviceInfo.put(ResourceResolverFactory.SUBSERVICE, "adminService");

            resourceResolver = resourceResolverFactory.getServiceResourceResolver(serviceInfo);
            FrostHttpUtil.registerContent(resourceResolver, path, null, true);

            PoetPayload poetPayload = resourceResolver.getResource(path).adaptTo(PoetPayload.class);

            PoetConfigSlingModel poetConfigSlingModel = poetPayload.getPoetConfigSlingModel();

            if (!FrostHttpUtil.registerContent(resourceResolver, path, poetPayload.getPayload().toString(), false)) {
                log.warn("error when submitting workflow payload to frost");
                if (poetConfigSlingModel.getAbandonOnFailure()) {
                    return;
                }
                // error so it will be retried
                throw new WorkflowException("error when submitting workflow payload to frost");
            }
        } catch (Exception ex) {
            log.error("error posting content to po.et", ex);

        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

}