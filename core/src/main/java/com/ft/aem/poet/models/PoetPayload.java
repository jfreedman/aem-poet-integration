package com.ft.aem.poet.models;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;
import com.ft.aem.poet.services.PoetAuthorService;
import com.ft.aem.poet.services.PoetBodyTextService;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * represents a page as a frost payload
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PoetPayload {

    private static final Logger logger = LoggerFactory.getLogger(PoetConfigSlingModel.class);

    @SlingObject
    private Resource resource;

    @Inject
    private PoetAuthorService poetAuthorService;

    @Inject
    private PoetBodyTextService poetBodyTextService;

    private boolean isValid = true;

    private PoetConfigSlingModel cfgModel;

    private Resource contentResource;

    @PostConstruct
    public void postConstruct() {
        if (resource == null || resource.adaptTo(Page.class) == null) {
            logger.debug("Not a page resource");
            isValid = false;
        }
        contentResource = resource.getChild("jcr:content");

        if (contentResource == null || contentResource.getResourceType() == null) {
            logger.debug("No jcr content node for page or no resource type defined");
            isValid = false;
        }


        // get cloud service configs inherited by page
        InheritanceValueMap hierarchyNodeInheritanceValueMap = new HierarchyNodeInheritanceValueMap(resource.getChild("jcr:content"));
        String[] services = hierarchyNodeInheritanceValueMap.getInherited("cq:cloudserviceconfigs", new String[]{});
        ConfigurationManager cfgMgr = resource.getResourceResolver().adaptTo(ConfigurationManager.class);
        if (cfgMgr != null) {
            Configuration cfg = cfgMgr.getConfiguration("poet", services);
            if (cfg == null) {
                logger.debug("no po.et config found for page, exiting");
                isValid = false;
            }
            cfgModel = cfg.getResource().getChild("jcr:content").adaptTo(PoetConfigSlingModel.class);

            if (cfgModel == null) {
                logger.debug("po.et cloud services config null, exiting");
                isValid = false;
            }
        } else {
            isValid = false;
        }
    }

    /**
     * Gets the cloud services config used by poet for this page
     *
     * @return po.et cloud services config
     */
    public PoetConfigSlingModel getPoetConfigSlingModel() {
        return cfgModel;
    }

    /**
     * adds a work id to a page after submitting to frost
     *
     * @param workId           the work id to be added
     * @param resourceResolver the resolver to use for the save
     * @throws PersistenceException if save error occurs
     */
    public void addWorkId(String workId, ResourceResolver resourceResolver) throws PersistenceException {
        ArrayList<String> workIds = new ArrayList<String>();
        if (contentResource.getValueMap().containsKey("workIds")) {
            String[] tempWorkIds = contentResource.getValueMap().get("workIds", String[].class);
            if (tempWorkIds != null) {
                workIds = new ArrayList(Arrays.asList(tempWorkIds));
            }
        }
        workIds.add(workId);
        ModifiableValueMap map = contentResource.adaptTo(ModifiableValueMap.class);
        map.put("workIds", workIds.toArray());
        resourceResolver.commit();
    }

    /**
     * gets the json payload of the page for frost
     *
     * @return json payload of page in frost format
     */
    public JSONObject getPayload() {
        if (!isValid) {
            logger.debug("Not a valid payload, exiting");
            return null;
        }

        try {
            Page page = resource.adaptTo(Page.class);
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz);
            JSONObject payload = new JSONObject();
            String body =  poetBodyTextService.getBody(contentResource);
            if(body.isEmpty()) {
                logger.warn("no body content found for:" + resource.getPath() + " cannot create payload");
                return null;
            }
            payload.put("content", poetBodyTextService.getBody(contentResource));

            String title = contentResource.getValueMap().get("jcr:title", String.class);
            if (title == null || title.trim().isEmpty()) {
                logger.debug("page title is null, cannot submit to po.et");
                return null;
            }
            payload.put("name", title);
            Tag[] tags = null;
            try {
                tags = page.getTags();
            } catch (NullPointerException ne) {
            }
            StringBuilder tagText = new StringBuilder();
            if (tags != null) {
                for (int i = 0; i < tags.length; i++) {
                    Tag tag = tags[i];
                    tagText.append(tag.getTitlePath());
                    if (i != tags.length - 1) {
                        tagText.append(",");
                    }
                }
            }
            if (!tagText.toString().isEmpty()) {
                payload.put("tags", tagText.toString());
            }
            Calendar lastModified = Calendar.getInstance();
            try {
                lastModified = page.getLastModified();
            } catch (NullPointerException ne) {
            }
            payload.put("datePublished", df.format(lastModified.getTime()));
            payload.put("dateCreated", df.format(lastModified.getTime()));
            payload.put("author", poetAuthorService.getAuthor(contentResource));

            return payload;
        } catch (JSONException je) {
            logger.warn("error generating payload", je);
            return null;
        }

    }
}
