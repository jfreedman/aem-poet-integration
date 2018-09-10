package com.ft.aem.poet.frost;


import com.day.cq.replication.ReplicationResult;
import com.ft.aem.poet.PoetException;
import com.ft.aem.poet.models.PoetConfigSlingModel;
import com.ft.aem.poet.models.PoetPayload;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Utility class to post content to frost
 */
public class FrostHttpUtil {

    private static final Logger log = LoggerFactory.getLogger(FrostHttpUtil.class);

    private static PoolingHttpClientConnectionManager cManager = new PoolingHttpClientConnectionManager();

    /**
     * Posts a payload to frost
     *
     * @param uri     frost base uri
     * @param payload the payload to post
     * @param token   the token to use
     * @return response with info from frost
     * @throws PoetException error occurred posting
     */
    public static FrostResponse postToFrost(URI uri, String payload, String token) throws PoetException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom()
                    .setConnectionManager(cManager)
                    .build();
            HttpPost postMethod = new HttpPost(uri.toString() + "/works");
            postMethod.addHeader("token", token);
            postMethod.addHeader("Content-Type", "application/json");
            StringEntity entity = new StringEntity(payload);
            postMethod.setEntity(entity);
            response = httpClient.execute(postMethod);
            return new FrostResponse(IOUtils.toString(response.getEntity().getContent()), response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        } catch (IOException ex) {
            throw new PoetException("error calling poet api", ex);
        }
    }

    public static boolean registerContent(ResourceResolver resourceResolver, String pagePath, String content, boolean fetchContentFromPage) throws PoetException {
        boolean abandonOnFailure = true;
        try {
            Resource poetResource = resourceResolver.getResource(pagePath);
            if (poetResource == null) {
                log.info("path " + pagePath + "isn't valid for poet");
                return true;
            }
            PoetPayload poetPayload = poetResource.adaptTo(PoetPayload.class);
            if (poetPayload == null) {
                log.info("path " + pagePath + "isn't valid for poet");
                return true;
            }
            PoetConfigSlingModel poetConfigSlingModel = poetPayload.getPoetConfigSlingModel();
            if (poetConfigSlingModel == null) {
                log.info("path " + pagePath + "doesn't have valid poet config");
                return true;
            }

            // if it fails, do we queue the replication event and retry, or exit?
            abandonOnFailure = poetConfigSlingModel.getAbandonOnFailure();

            if(fetchContentFromPage) {

            }
            // get payload content
            if (content != null && !content.isEmpty()) {

                // post payload to frost
                FrostResponse frostResponse = FrostHttpUtil.postToFrost(new URI(poetConfigSlingModel.getUri()), content, poetConfigSlingModel.getToken());
                if (frostResponse.getResponseCode() != 200) {
                    log.warn("non 200 response returned from frost-" + frostResponse.getResponseCode() + ":" + frostResponse.getResponseCodeText());
                    if (abandonOnFailure) {
                        return true;
                    }
                    log.warn("non 200 response returned from frost-" + frostResponse.getResponseCode());
                    return false;
                }

                // store work id returned on page
                JSONObject response = new JSONObject(frostResponse.getContent());
                String workId = response.getString("workId");
                poetPayload.addWorkId(workId, resourceResolver);
                return true;
            } else {
                log.info("path " + pagePath + "doesn't have content to replicate");
                return true;
            }
        } catch (Exception ex) {
            log.error("error registering content with poet", ex);
            if(!abandonOnFailure) {
                throw new PoetException("error registering content with poet", ex);
            }
        }
        return true;
    }


}
