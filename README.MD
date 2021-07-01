## Getting Started:


###### Description:
 
Azure is Microsoft's cloud-based application platform for developing, managing, and hosting applications in the cloud. Azure consists of several components: the cloud operating system itself; SQL Azure, which provides database services in the cloud; and .NET services.

Azure Resource Manager enables you to work with the resources in your solution as a group. You can deploy, update, or delete all the resources for your solution in a single, coordinated operation.

The Action Pack for Microsoft Azure allows you to build visual workflows to fully automate all of your Azure services including managing Cloud Services, Virtual Machines, Storage Containers, Blob (Block) Storage, Deployments and Virtual Network Configurations. It also handles MS Azure Resource Manager so that you can deploy your applications as a whole using CA Automic Release Automation or CA Automic Service Orchestration.

Key benefits:

* Ease the efforts of developers, application engineers, and system administrators to create, deploy, and run applications by           automatically utilizing Azure services as part of deployment workflows or pre-approved, self-help runbooks

* Ship code and container as one coherent deployable package that can be automatically promoted from environment to environment

* Standardize an Azure best practice for application deployments and avoid expensive cloud sprawl   
  Know what application is where - auto provision and deprovision containers

 
 ###### Actions:
 1. Get Subscription Info Action 
 2. Change Virtual Machine State
 3. Check the Azure Request Status    
 4. Delete Azure Virtual Machine 
 5. Create Storage Container Action 
 6. Create Virtual Machine Deployment 
 7. Delete Storage Container Action 
 8. Create Cloud Service 
 9. Put Blob 
10. Delete Blob 
11. Delete Cloud Service 
12. Set Virtual Network Configuration 
13. Create Deployment 
14. Delete Deployment 
15. Delete File 
16. Get Access Token 
17. Create Resource Group 
18. Deploy to Resource Group 
19. Get Deployment Status 
20. List Resource Groups 
21. List Locations 

 ###### Compatibility:

1. Microsoft Azure Rest API for ARM 2016-09-01 
2. Microsoft Azure Rest API for Management Services 2015-04-01 
3. Microsoft Azure Rest API for Storage Services 2015-02-21 



###### Prerequisite:

1. Automation Engine should be installed.
2. Automic Package Manager should be installed.
3. ITPA Shared Action Pack should be installed.
4. RA REST Action Pack should be installed.
5. Oracle JRE 1.7 or higher must be installed on Agent OS and "java" must be set as executable on the same.
6. You should have active subscription provided by Microsoft Azure Services.
7. REST API(s) should be enabled for your subscription.
8. Certificate should be set up and uploaded to Azure Portal for your subscription.

###### Setup Azure Certificate:

1. Create Java Key Store file (JKS format) using following command and answer some basic questions. 
   keytool -genkeypair -alias <alias name> -keyalg RSA -keystore <keystorefile> -keysize 2048 -storepass <keystorepassword>
   Note: Please replace the entities enclosed in "<>"
   <alias name> represents the key that you want to use for the corresponding certificate.
   <keystorefile> represents the keystore file name ending with (.jks). If you just specify file name, it will create the file into current directory.
   <keystore password> represents password for the keystore.
   E.g.  keytool -genkeypair -alias azure -keyalg RSA -keystore azurekeystore.jks -keysize 2048 -storepass test123
   This will create a keystore file azurekeystore.jks in the current directory.

2. Export the certificate from the keystore as created in step 1 using following command.
   E.g. keytool -v -export -file azure.cer -keystore azurekeystore.jks -alias azure
   This will create a file azure.cer in the current directory.


###### Upload the certificate to your Azure subscription:
Log onto the Azure Portal site (https://manage.windowsazure.com) using your account details.
Click on settings->Management Certificates, there will be an option to upload the certificate. Click on upload.
Select the certificate file (azure.cer) as generated in step 2.

###### Verify access of Azure using using AE Actions:
Execute the Action Get Subscription Info.
Provide the Subscription ID/Key store file and key store password (As created above)
Choose the agent (Please make sure that Key store file is accessible from Agent)
Action should be successfully executed and subscription information should be logged into job report as well.

###### Steps to install action pack source code:

1. Clone the code to your machine.
2. Go to the package directory.
3. Run the command apm upload in the directory which contains package.yml (source/):

Ex. **apm upload -force -u <Name>/<Department> -c <Client-id> -H <Host> -pw <Password> -S AUTOMIC -y -ia -ru**

###### Package/Action Documentation:

Please refer to the link for [package documentation](source/ae/DOCUMENTATION/PCK.AUTOMIC_AZURE.PUB.DOC.xml)

###### Third party licenses:

The third-party library and license document reference.[Third party licenses](source/ae/DOCUMENTATION/PCK.AUTOMIC_AZURE.PUB.LICENSES.xml)


###### Useful References

1. [About Packs and Plug-ins](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#PluginManager/PM_AboutPacksandPlugins.htm?Highlight=Action%20packs)
2. [Working with Packs and Plug-ins](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#PluginManager/PM_WorkingWith.htm#link10)
3. [Actions and Action Packs](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#_Common/ReleaseHighlights/RH_Plugin_PackageManager.htm?Highlight=Action%20packs)
4. [PACKS Compatibility Mode](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#AWA/Variables/UC_CLIENT_SETTINGS/UC_CLIENT_PACKS_COMPATIBILITY_MODE.htm?Highlight=Action%20packs)
5. [Working with actions](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#ActionBuilder/AB_WorkingWith.htm#link4)
6. [Installing and Configuring the Action Builder](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#ActionBuilder/install_configure_plugins_AB.htm?Highlight=Action%20packs)

###### Distribution: 

In the distribution process, we can download the existing or updated action package from the Automation Engine by using the apm build command.
Example: **apm build -y -H AE_HOST -c 106 -u TEST/TEST -pw password -d /directory/ -o zip -v action_pack_name**
			
			
###### Copyright and License: 

Broadcom does not support, maintain or warrant Solutions, Templates, Actions and any other content published on the Community and is subject to Broadcom Community [Terms and Conditions](https://community.broadcom.com/termsandconditions)
