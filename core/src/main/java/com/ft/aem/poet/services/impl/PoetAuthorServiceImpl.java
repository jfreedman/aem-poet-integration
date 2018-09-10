package com.ft.aem.poet.services.impl;

import com.day.cq.replication.ContentBuilder;
import com.ft.aem.poet.services.PoetAuthorService;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.QueryBuilder;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * The default implementation of the author service looks up the author name in AEM based on the user that created the page
 */
@Component(
        immediate = true,
        service = PoetAuthorService.class,
        property = {
                "service.ranking=99"
        }
)
public class PoetAuthorServiceImpl implements PoetAuthorService {

    private static final Logger logger = LoggerFactory.getLogger(PoetAuthorServiceImpl.class);


    public String getAuthor(Resource resource) {
        String authorName = "";
        try {
            UserManager userManager = resource.getResourceResolver().adaptTo(UserManager.class);

            Iterator<Authorizable> auths = null;

                auths = userManager.findAuthorizables(new Query() {
                    public <T> void build(QueryBuilder<T> builder) {
                        builder.setCondition(builder.nameMatches(resource.getValueMap().get("jcr:createdBy", String.class)));
                    }});

            String lastName = null;
            String firstName = null;
            if (auths != null && auths.hasNext()) {
                Authorizable auth = auths.next();
                lastName = auth.getProperty("./profile/familyName") != null ? auth.getProperty("./profile/familyName")[0].getString() : null;
                firstName = auth.getProperty("./profile/givenName") != null ? auth.getProperty("./profile/givenName")[0].getString() : null;
            }
        if (firstName != null && lastName != null) {
            authorName = firstName + " " + lastName;
        } else {
            authorName = resource.getValueMap().get("jcr:createdBy", String.class);
        }
        } catch (Exception ex) {
            logger.warn("error finding user", ex);
        }
        return authorName;
    }
}
