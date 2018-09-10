package com.ft.aem.poet.services.impl;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;
import com.ft.aem.poet.models.PoetConfigSlingModel;
import com.ft.aem.poet.services.PoetBodyTextService;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * The default implementation of the body text service will look at the mappings of resource types and paths defined in the poet cloud services configuration
 * if a matching sling resourcetype is found that has the path defined, all child text nodes of that path will be included as body text when submitted to poet
 */
@Component(
        immediate = true,
        service = PoetBodyTextService.class,
        property = {
                "service.ranking=99"
        }
)
public class PoetBodyTextServiceImpl implements PoetBodyTextService {

    private static final Logger logger = LoggerFactory.getLogger(PoetAuthorServiceImpl.class);

    public String getBody(Resource resource) {
        StringBuilder content = new StringBuilder();
        InheritanceValueMap hierarchyNodeInheritanceValueMap = new HierarchyNodeInheritanceValueMap(resource);
        String[] services = hierarchyNodeInheritanceValueMap.getInherited("cq:cloudserviceconfigs", new String[]{});
        ConfigurationManager cfgMgr = resource.getResourceResolver().adaptTo(ConfigurationManager.class);
        if (cfgMgr != null) {
            Configuration cfg = cfgMgr.getConfiguration("poet", services);
            if (cfg != null) {

                PoetConfigSlingModel cfgModel = cfg.getResource().getChild("jcr:content").adaptTo(PoetConfigSlingModel.class);

                if (cfgModel != null) {
                    Map<String, String> mappings = cfgModel.getMappings();
                    for (Map.Entry<String, String> entry : mappings.entrySet()) {
                        if (entry.getKey().endsWith("/" + resource.getResourceType())) {
                            // found a match, navigate to parsys and extract text components
                            Resource parsysResource = resource.getResourceResolver().getResource(resource.getPath() + "/" + entry.getValue());
                            if (parsysResource == null) {
                                logger.debug("parsys for content to submit was null, exiting");
                                return null;
                            }
                            Iterator<Resource> children = parsysResource.getChildren().iterator();
                            while (children.hasNext()) {
                                Resource child = children.next();
                                if (child.getValueMap().containsKey("text")) {
                                    content.append(child.getValueMap().get("text", String.class));
                                }
                            }
                        }
                    }
                }
            }
        }
        return content.toString();
    }
}
