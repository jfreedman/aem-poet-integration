# AEM Po.et Blockchain Integration

The Po.et blockchain network allows you to generate immutable and timestamped titles for your creative works and register your assets to the Po.et network.  Po.et uses cryptography to give both publishers and content creators the tools to automate the licensing process without relying on third parties.

The AEM Po.et Blockchain Integration enables AEM to register page content to the Po.et Blockchain network either on publication or via a workflow.  It extends several AEM features:

###Po.et Cloud Service Configuration
Alows a user to create one or more Po.et Cloud Services Configuration(s) for AEM defining your Po.et frost api key, and what page resource types are registered with Po.et

###Po.et Content Builder and Transport Handler
A custom AEM replication agent used to register content with Po.et
###Po.et Workflow
An AEM workflow that can be used to register a page or batch of pages with Po.et without having to activate pages
###Po.et Body Text and Author Services
OSGi services that define how an author name is derived from a page, or how body text is found on a page.  Default implementations are included in this project, but can be overridden if your logic for author names or body text needs to be customized by added new services with a higher service.ranking property than the default services. 

## Building, Installing, and Configuring

The Po.et Blockchain Integration runs on an AEM Author server and supports AEM 6.3 and 6.4.  To install the project to your local machine run:

    mvn clean install -PautoInstallPackage

This will build and package the project, as well as install it to localhost:4502

The package contains UI components stored in the repository under /apps/poet-cloudservices and /etc/cloudservices/poet, as well as an OSGi bundle that is installed to Felix.

Once installed, you must configure the Cloud Services configuration for Po.et:

1.  go to https://frost.po.et/, register or signin to your account, and copy your access token
2.  go to: /etc/cloudservices.html, and find the "Po.et Blockchain Network" Cloud Service.  Press the "Show Configurations" button, then press the "+" button to add a new configuration.
3.  In the dialog that opens, provide a title for your Po.et cloud service configuration and press "ok", your configuration will be created and a new dialog will open.
4.  In the new dialog, populate the following:
    * Frost API Token - Enter your frost token from step 1.  This is stored as encrypted in AEM, if you reopen the dialog, you will see the encrypted value instead of the original.
    * Frost Uri - Enter the frost Uri, this likely won't change from the default
    * Terminate on Failure - if when registering a page with Po.et we receive a non 200 response, should the replication agent keep retrying or discard the item?
    * Resource Type Mappings - one or more key value pairs defining what types of content will be regsitered with Po.et, and where the text of the content lives in the page.  For example, a value of: /apps/weretail/components/structure/page=/root/responsivegrid will only send pages of type "/apps/weretail/components/structure/page" to Po.et, and the body content registered with Po.et will include all text components under the "jcr:content/root/responsivegrid"
5. Press ok to save your changes
6. Repeat steps 1 through 5 if you have multiple AEM sites in your repository that use different Frost api keys.
7. If you wish to configure a replication agent for Po.et, go to /miscadmin#/etc/replication/agents.author and create a new replication agent.
8. Once the replication agent is created, open it's dialog, and modify the following:
    * Serialization Type - set this to "Po.et blockchain network"
    * URI - This value isn't used, but is required by a replication agent, you can use a value of "poet://frost.po.et". 
9.  For the sites you want to integrate with Po.et, configure the cloud services tab in page properties to use the Po.et configuration on the page you wish to register, or on a parent of the Page you wish to register.  If you want to register any new content within a site with Po.et, configure cloud services for the site root page.

## How To Use
Once you have built, installed, and configured your Po.et cloud service in AEM, you are ready to register content with Po.et.

