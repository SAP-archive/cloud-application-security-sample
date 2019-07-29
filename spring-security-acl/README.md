# Access Limitation on Instances using Spring ACL

Assume you've understood how Spring-based applications can be basically secured as explained [here](/spring-security-basis), this sample project goes one step further, demonstrating instance-based access control using access control lists (ACL) in applications based on Spring Boot and deployed on SAP Cloud Platform, Cloud Foundry (SAP CP CF).

## Goal of this sample project

This [Spring Boot 2.0](http://projects.spring.io/spring-boot/) demo application shows how to implement instance-based access control in Spring-based SAP Cloud Platform applications. It leverages [Spring Security ACL](https://github.com/spring-projects/spring-security) and integrates to SAP Cloud Platform XSUAA service using the [SAP Container Security Library (Java)](https://github.com/SAP/cloud-security-xsuaa-integration), which is available on [maven central](https://search.maven.org/search?q=com.sap.cloud.security).

Instances, you want to protect, could be **business objects** (e.g. procurement order), **data records** on the database (e.g. leave request) or any other **resource**.

The microservice is adapted from the code developed in the [openSAP course: Cloud-Native Development with SAP Cloud Platform](https://open.sap.com/courses/cp5) and runs in the Cloud Foundry environment within SAP Cloud Platform.

## Table of Contents
This document is divided into the following sections
- [Understanding Access Control Lists (ACL)](#motivation) - understand when and how ACL helps (in comparison to Attribute-Based Access Control (ABAC)) 
- [Use Cases](#examples)
- [Download and Installation](#setupandstart) - a description of how to use this project
- [Steps to Deploy and Test on Cloud Foundry](#deployAndTestOnCF) - explains how to deploy and test the application on Cloud Foundry
- [Implementation Details](#notes) - dig into details of its implementation, configuration, notable features
- [Further References](#furtherReading) - references and further learning material

<a id='motivation'></a>
## Understanding Access Control Lists (ACL)
Assume you have a cloud-native application that exposes REST APIs to access (read, write,...) instances that are worth protecting. An instance could be a business object having the type of a procurement order, a leave request or in our case an advertisement. With Access Control Lists (ACL) you can control the access to dedicated instances. 

An **Access Control List (ACL)** is a list of permissions attached to dedicated instances. An ACL specifies for an instance which operations (e.g. read, write, publish, ...) are granted to an identity. An identity can be a user principal or match to a user role/attribute.

The ACL approach can be useful when:
- The access to instances cannot be solely protected by roles (scopes and attributes), and in addition there is no "static" criteria (like cost center, confidentiality level, ...) that could be used as a filter. Then the decision whether a user is authorized for a certain instance can be defined independently per instance just by maintaining the ACL.
- The instance-based access shall change dynamically during the instance lifecycle without changing the instance and without consulting the administrator. For example, the instance owner wants to delegate permissions temporarily to dedicated users/substitutes or user groups. 
- You want to model ACL hierarchies.
  - For example, a facilitator should get edit-permissions to some buildings within a location. Furthermore, he should have the same permissions to most of the rooms, that are linked to the buildings.
  - Same could be the case for organizational hierarchies, like cost center.
- You like to manage the ACL as part of your application and close to the instances, you want to protect without delegating the task of authorization management to a dedicated user administrator. 

<a id='comparison'></a>
### Comparison of the Attribute-based filter approach with the ACL Approach

#### Advantages of the Attribute-Based Access Control (ABAC)
+ Simplicity, and lower development effort as no additional infrastructure is needed.

Attribute-Based Access Control is explained and implemented with this [basis sample project](/spring-security-basis).

#### Advantages of the ACL Approach
+ The authorization decision is better understandable as missing authorizations can be clearly identified as such and not just as a filtered result list of an operation, which can have different reasons.
+ Simplifies the implementation of audit logs and GDPR compliance as all relevant data is stored in central authorization tables.
+ Not limited to the attributes of an instance that needs somehow to match the attributes attached to a Role.
+ The ACL Approach can be combined with the Filter approach (with the cost of an additional table JOIN).

<a id='examples'></a>
## Use Cases

### Create a protect-worthy instance and assign initial permissions to owner
The user with log-on name `advertiser` creates a new advertisement (id = `55`). You can test with POST-request `/api/v1/ads/acl/` that the following entries are created in the postgreSql `test` database.

- For the user principal `advertiser` an entry gets created (if not yet there) in the `ACL_SID` table:

   id | principal | sid  
  -- | ---- | --------  
   7 | true | advertiser 
   
  > Note: the `sid` value is in fact not `advertiser` but contains also the origin (IdP) of the JWT token `user/{origin}/advertiser`.

- For the new advertisement instance with id `55` an entry gets created in the `ACL_OBJECT_IDENTITY` table:

  id | object_id_class | object_id_identity | parent_object | owner_sid | entries_inheriting |
  --- | --- | --- | --- | --- | --- |
  ... | ... | ... | ... | ... | ... |
  3 | 1 | 55 | 1200 | 1 | true |

  > The `ACL_OBJECT_IDENTITY` table refers to an `com.sap.cp.appsec.domain.Advertisement` instance. The advertisement instance id is specified in the `object_id_identity` column and its type is classified in the `ACL_CLASS` table.

- For each permission (read, write, admin, ...) the user (sid = `7`) gets for the instance (acl_object_identity = `3`) an `ACL_ENTRY` table entry:

  id | acl_object_identity | ace_order | sid | mask | granting | audit_success | audit_failure | 
  -- | - | - | - | -- | ---- | ---- | ---- | 
  26 | 3 | 0 | 7 |  1 | true | true | true | 
  27 | 3 | 1 | 7 |  2 | true | true | true | 
  28 | 3 | 2 | 7 | 16 | true | true | true |

  > It's possible to insert both granting and revoking entries in `ACL_ENTRY`. The `ace_order` matters meaning it's possible to revoke access in line 0 and that will take precedence over a grant on line 1.

### Delegate: grant write-permission to individuals
Any user with "admin" permission (e.g. instance owner) for an advertisement should be able to grant permissions to other individuals (e.g. `myfriend`) without involvement of an administrator. You can test with PUT-request to `/api/v1/ads/acl/grantPermissionsToUser/{id}`.

- Again, for the user principal `myfriend` an entry gets created (if not yet there) in the `ACL_SID` table:

  id | principal | sid  
  -- | ---- | --------  
   7 | true | advertiser 
   8 | true | myfriend 
   
- For each permission (read, write, admin, ...) the user (sid = `8`) gets for the instance (acl_object_identity = `3`) an `ACL_ENTRY` table entry:

  id | acl_object_identity | ace_order | sid | mask | granting | audit_success | audit_failure | 
  -- | - | - | - | -- | ---- | ---- | ---- | 
  ... | ... | ... | ... |  ... | ... | ... | ... | 
  29 | 3 | 3 | 8 |  1 | true | true | true | 
  30 | 3 | 4 | 8 |  2 | true | true | true | 
  31 | 3 | 5 | 8 | 16 | true | true | true |   
  

### Collaborate: grant write-permission to my team members
Any user with "admin" permission for an advertisement should be able to grant permission to all users of a dedicated user group. You can test with PUT-request to `/api/v1/ads/acl/grantPermissionsToUserGroup/{id}`.

First question might be, how to model / specify a user group? How we've done it: any user must provide its group assignnments as `xs.user.attributes` as part of its JWT token:

```
{   
    ...
    "scope": [],
    "xs.user.attributes": {
       "group": [
          "UG_MY_TEAM"
       ]
    }
}
```

These groups e.g. `UG_MY_TEAM` must be exposed in the `ACL_SID` table:

  id | principal | sid  
  --- | ---- | --------  
  7  | true | advertiser 
  8  | true | myfriend 
  9  | false | ATTR:GROUP=UG_MY_TEAM
  10 | false | ATTR:GROUP=UG_OTHER_TEAM
   
For each permission (read, write,...) the user group "UG_MY_TEAM" (sid = `9`) gets for the instance (acl_object_identity = `3`) an `ACL_ENTRY` table entry as we've learnt in the examples above. With that the number of `ACL_ENTRY` table entries can be reduced by the number of individuals a user group consists of.

> Technical note: when grouping individuals by non-principal SIDs (e.g. `ATTR:GROUP=UG_MY_TEAM`) the number of entries in the `ACL_ENTRY` table can be reduced.
  

### Append instance to a parent instance and inherit its permissions
Now the `advertisement` (id = `55`) is in a final state and should be published to a bulletinboard that is watched by many other people.

Here the user acts as "publisher", who requires "admin" permission of the advertisement to be published as well as access to the target bulletinboard:

```
{   
    ...
    "scope": [],
    "xs.user.attributes": {
       "bulletinboard": [
          "DE_WDF03_Board"
       ]
    }
}
```

Note that in our case the bulletinboard is not a core domain object that is *managed* by our `Advertisement` application.
So we'd only like to refer to it by a key of type `java.lang.String`.

- "Static" entries in `ACL_CLASS` table:

  | id | class | class_id_type |
  | -- | ----- | ------------- |
  | 2.000 | location | java.lang.String |
  | 2.002 | bulletinboard | java.lang.String |
  | 1 | com.sap.cp.appsec.domain.Advertisement | java.lang.Long |

  > Note that the target parent instance - in our case the `bulletinboard` - must not necessarily be an instance of a Java Class with an identifier of type `java.lang.Long`. With Spring security version `5.2.0` you can also specify a unique identifier such as `DE_WDF03_Board` of type `java.lang.String` or `java.util.UUID` in the `ACL_OBJECT_IDENTITY` table.

- Entries in `ACL_OBJECT_IDENTITY` before publishing:

  id | object_id_class | object_id_identity | parent_object | owner_sid | entries_inheriting |
  -- | -- | -- | -- | -- | -- |
  1.100 | 2.000 | DE | [NULL] | 3.000 | false |
  1.101 | 2.000 | IL | [NULL] | 3.000 | false |
  1.200 | 2.002 | DE_WDF03_Board | 1.100 | 3.005 | true |
  1.201 | 2.002 | DE_WDF04_Board | 1.100 | 3.005 | true |
  1.202 | 2.002 | IL_RAA03_Board | 1.101 | 3.005 | true |
  3 | 1 | 55 | [NULL] | 1 | true |

- After successful publishing the column `parent_object` of the advertisements `ACL_OBJECT_IDENTITY` entry has changed:

  id | object_id_class | object_id_identity | parent_object | owner_sid | entries_inheriting |
  -- | -- | -- | -- | -- | -- |
  ... | ... | ... | ... | ... | ... |
  3 | 1 | 55 | 1200 | 1 | true |
  
  > Technical note: with hierachical permissions the number of entries in the `ACL_OBJECT_IDENTITY` table can be reduced.  

### Pageable access to all instances i have explicit / implicit access to
User with permissions for a bulletinboard, has the same permissions for its (published) advertisement instances. In this case the advertisement instances inherits the permissions of the parent instance, i.e. the bulletinboard.

User with permissions for a location, has the same permissions for its bulletinboards and as well its (published) advertisement instances. In this case the advertisement instances inherits the permissions of its parent instances, i.e. the bulletinboard, location.

User with "read" permission can paginate advertisement instances, he has "read" authorizations for. You can test with GET-request to `http://localhost:8080/api/v1/ads/acl/published`.

### Remove permissions
User with "admin" permission for an advertisement can remove permissions to it from users (user groups). You can test with PUT-request to `/api/v1/ads/acl/removePermissionsFromUser/{id}`.

After this the related entries for the principal user (sid) should disappear from the `ACL_ENTRY` table.

### Cleanup of the ACL tables
The `JdbcMutableAclService` Spring ACL class supports the following methods:
```
deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren)
```
This should be called whenever an object instance, for which acl entries are mapped, is deleted.

But this will not remove all / some permissions from a dedicated user principal. This needs to be implemented in the ACL Service wrapper class, namely [AclSupport](https://github.wdf.sap.corp/CPSecurity/cp-application-security/blob/master/spring-security-acl/src/main/java/com/sap/cp/appsec/security/AclSupport.java).

See also the Spring forum [here](http://forum.spring.io/forum/spring-projects/security/72871-delete-all-ace-s-in-multiple-acls-for-a-given-sid).


## <a name="setupandstart"></a>Download and Installation
### Prerequisites
Setup your development environment according to the description [here](/prerequisites/README.md).

### Start PostgreSQL database in docker container
We need to make sure that a PostgreSQL database is running on the local machine, as referenced in [`application-localdb.properties`](src/main/resources/application-localdb.properties). 

The `docker-compose.yml` specifies all required docker containers.
In order to start a fresh database container with PostgreSQL, execute
```bash
docker-compose up -d
```
To tear down all containers, execute:
```bash
docker-compose down
```

To run the application locally you have two options: start it directly via Maven on the command line or within your IDE (Eclipse, IntelliJ).

In both cases, your application will be deployed to an embedded Tomcat web server and is visible at the address `http://localhost:8080/api/v1/ads/acl`.

The provided [`localEnvironmentSetup`](localEnvironmentSetup.bat) shell script can be used to set the necessary values for local execution. Within your development IDE (Eclipse, IntelliJ), you need to define the following environment variables: `VCAP_APPLICATION` and `SPRING_PROFILES_ACTIVE` - as done in the script.

### Run on the command line
Execute in terminal (within project root, which contains the`pom.xml`):
```bash 
source localEnvironmentSetup.sh
mvn spring-boot:run
```

Or on Windows command line:
```bash
localEnvironmentSetup.bat
mvn spring-boot:run
```

### Run in Eclipse (STS)
In Eclipse Spring Tool Suite (STS) you can import the project as an existing Maven project. There you can start the main method in `com.sap.cp.appsec.Application`.
You can also right-click on the class in the Package Explorer, and select `Run As` - `Spring Boot App`. Make sure that you have set in the same environment variables in the Run Configuration as specified in the [`localEnvironmentSetup script`](localEnvironmentSetup.bat).

## Test using Postman
Now you are ready to test the application manually using the [`Postman` chrome plugin](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop).

The service endpoints are secured, that means no unauthorized user can access the endpoint. The application expects a so called `JWT` (JSON Web Token) as part of the `Authorization` header of the service that also contains the scope, the user is assigned to.

You can import the [Postman collection](documentation/testing/spring-acl-local.postman_collection.json), as well as the [Postman environment](documentation/testing/spring-acl-local.postman_environment.json) that provides different JWT tokens for the `Authorization` headers to do some sample requests.

**Note**: For all requests make sure, that you provide a header namely `Authorization` with a JWT token as value e.g. `Bearer eyJhbGciOiJSUzI1NiIs...`.

For reference look up [Postman documentation](https://www.getpostman.com/docs/environments).

<a id='deployAndTestOnCF'></a>
## Steps to Deploy and Test on Cloud Foundry

### Build Advertisement Service (our Java application)
Build the Advertisement Service which is a Java web application running in a Java VM. Maven build tool compiles the code and packages it in its distributable format, such as a `JAR` (Java Archive). With this the maven dependencies are downloaded from the [Maven central](https://search.maven.org/) into the `~/.m2/repository` directory. Furthermore the JUnit tests are executed and the `target/demo-application-security-acl.jar` is created.

Execute in the command line (within project directory, which contains the`pom.xml`):
```
mvn package
```

### Login to Cloud Foundry
Make sure your are logged in to Cloud Foundry and you target your trial space.
The following commands will setup your environment to use the provided Cloud Foundry instance.

 - `cf api <<Your API endpoint>>` (API endpoints are listed [here](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html))
 - `cf login -u <<your user id>>`
 - In case you are assigned to multiple orgs, select the `trial` organisation.


### Create services
Create the (backing) services that are specified in the [`manifest.yml`](manifest.yml).

Execute in terminal (within project directory, which contains the `security` folder):
```
cf create-service postgresql v9.6-dev postgres-bulletinboard-ads
cf create-service xsuaa application uaa-bulletinboard -c security/xs-security.json
```
> Using the marketplace (`cf m`) you can see the backing services and its plans that are available on SAP CP and (!) you are entitled to use.

### Configure the manifest
As a prerequisite step open the [../vars.yml](../vars.yml) file locally and replace the `ID` for example by your SAP account user name, to make the routes unique. You might want to adapt the `LANDSCAPE_APPS_DOMAIN` as well.

### Deploy the approuter and the advertisement service
The application can be built and pushed using these commands (within root directory, which contains the`manifest.yml`):
```
cf push --vars-file ../vars.yml
```
> The application will be pushed using the settings provided in the `manifest.yml` and `../vars.yml`. You can get the exact urls/routes of your deployed application with `cf apps`.

<a id='approuterUri'></a>
### Create approuter route per tenant ID
We make use of the `trial` subaccount. As you can see in the SAP CP Cockpit subaccounts have properties (see *Subaccount Details*) which of the most important one is the **Subdomain**. The Subdomain serves as the value for the technical property Tenant ID.

The Tenant ID is encoded in the url, for example `https://<<your tenant>>-approuter-<<ID>>.<<LANDSCAPE_APPS_DOMAIN>>`.
That's why we need to specify an approuter route per Tenant ID (subdomain name), e.g. `p012345trial`. For example:
```
cf map-route approuter <<LANDSCAPE_APPS_DOMAIN e.g. cfapps.eu10.hana.ondemand.com>> -n <<your tenant e.g. p0123456trial>>-approuter-<<ID e.g. p0123456>>
```

And `cf app approuter` shows another tenant-specific approuter route, which is hereinafter **also called "`approuterUri`"**.

### Cockpit administration tasks
Go to the [SAP Cloud Platform Cloud Cockpit](https://account.hanatrial.ondemand.com/#/home/welcome)
- Navigate to your bulletinboard-ads application. Create some Role, e.g. `ROLE_MY_TEAM_MEMBER` based on the `GroupMember` role template. And specify its attribute `group`=`UG_MY_TEAM`:

![](documentation/images/CreateRole.jpg)  

> Note: Alternatively, in case your IdP supports SAML user attributes, you can also map to a dedicated SAML user attribute e.g. `Groups`. This gives you the advantage to inherit all group values, that are managed as part of the IdP.  

- Then navigate to your Subaccount and create a Role Collection e.g. `RC_GroupMember_MY_TEAM` and add the created Role.
- Finally, as part of your Identity Provider, e.g. SAP ID Service, assign the created Role Collection to your user.

Further up-to-date information you can get on sap.help.com:
- [Maintain Roles for Applications](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/7596a0bdab4649ac8a6f6721dc72db19.html).
- [Maintain Role Collections](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/d5f1612d8230448bb6c02a7d9c8ac0d1.html)
- [Assign Role Collections to Business Users](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/9e1bf57130ef466e8017eab298b40e5e.html)

### Test the deployed application
Open a browser to test whether your microservice runs in the cloud. Call the tenant-specific `approuterUri` URL as created [previously](#approuterUri). This will bring you the **login page**. Note: You have to enter here your SAP Cloud Identity credentials. After successful login you get redirected to the advertisement service that returns you the status of the application.

> Note: This [`xs-app.json`](src/main/approuter/xs-app.json) file specifies how the approuter routes are mapped to the advertisement routes. E.g. `<<approuterUri>>/ads/actuator/health` maps to ` <<bulletinboardAdsUri>>/actuator/health`.

Test the deployed REST Service on Cloud Foundry via the approuter URI using the `Postman` chrome plugin together with the `Postman Interceptor` chrome plugin. You can import the [Postman collection](documentation/testing/spring-acl-cloudfoundry.postman_collection.json) and create an environment, which specifies the key-value pair `approuterUri`=`<<your tenant-specific approuterUri>>`.

Find a more detailed description on how to test using `Postman` [in the basis sample](/spring-security-basis/README.md#steps-to-deploy-and-test-on-cloud-foundry).

<a id='notes'></a>
## Implementation Details

1. Setup Spring ACL database table (using liquibase): [database changelog](src/main/resources/db/changelog), [database population](src/main/resources/db/population)
1. Configure Spring ACL: [AclConfig](src/main/java/com/sap/cp/appsec/config/AclConfig.java) and [AclAuditLogger](src/main/java/com/sap/cp/appsec/config/AclAuditLogger.java)
1. Convenience wrapper for Spring `AclService` implementation: [AclSupport](https://github.wdf.sap.corp/CPSecurity/cp-application-security/blob/master/spring-security-acl/src/main/java/com/sap/cp/appsec/security/AclSupport.java)
1. Assign user's attributes to Spring Security Context: [CustomTokenAuthorizationsExtractor](src/main/java/com/sap/cp/appsec/security/CustomTokenAuthorizationsExtractor.java).
1. Assign and validate instance permissions: [AdvertisementService](src/main/java/com/sap/cp/appsec/services/AdvertisementService.java).
1. Support of pagination is implemented with Spring Data and SQL CE functions: [AdvertisementAclRepository](src/main/java/com/sap/cp/appsec/domain/AdvertisementAclRepository.java)
1. The application should support values-request for the application-specific `xs.user.attributes`, e.g. `/api/v1/attribute/group` : [AttributeFinderController](src/main/java/com/sap/cp/appsec/controllers/AttributeFinderController.java).
1. Application security model consists of role-templates with references to attributes only: [xs-security.json](security/xs-security.json)

### Permissions and bitwise masking 
By default, Spring ACL refers to [`BasePermission`](https://docs.spring.io/spring-security/site/apidocs/org/springframework/security/acls/domain/BasePermission.html) class for all available permissions (actions). 
You may subclass `BasePermission` in order to define application specific permissions. In this case you also need to replace as part of the `lookupStrategy` bean the `DefaultPermissionFactory` by your custom `PermissionFactory` implementation.

By default the `DefaultPermissionGrantingStrategy` does only support exact mask matching, even when using a composite mask e.g. "RW". This behaviour was discussed here on Spring ACL github project:
- https://github.com/spring-projects/spring-security/issues/1388
- https://github.com/spring-projects/spring-security/issues/2571

### Support of paginated REST APIs
With Spring ACL you can reject unauthorized access of instances on method level using `@PreAuthorize("hasPermission(<object>,<permission>)")` or `@PreAuthorize("hasPermission(<id>, <type>, <permission>)")`. Furthermore you can filter the result set by making use of `@PostAuthorize("hasPermission(<object>, <permission>)")`, after the result set has been retrieved from underlying layers (often database). This is obviously not the best approach in terms of performance, too many useless objects are retrieved but evicted by the filter. Let's consider pagination and assume a page size of 20 items. Spring Data repository returns up to 20 items, but all of them needs to be filtered, as the caller is not granted to access those... 

As consequence we have implemented as part of our Spring Data repository our own SQL CE function to fetch only these instances from the database, the user has granted access to.

**Example**: [Spring Data repository implementation](src/main/java/com/sap/cp/appsec/domain/AdvertisementAclRepository.java).


### (Audit) logging
You can implement an audit logger that is able to write audit-relevant logs in case of granted / un-granted access to an [AuditableAcl (ACL)](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/acls/model/AuditableAcl.html) or more precisely to an [AuditableAccessControlEntry (ACE)](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/acls/model/AuditableAccessControlEntry.html). 

Note: by default, an ACE is not configured to audit successful or failed access. You need to specify that in context of the `AuditableAcl` object with `updateAuditing(aceIndex, auditSuccess, auditFailure)`. 

By default `AuditLogger.logIfNeeded(isGranted, ace)` is only called in a few cases, e.g. in case of owner change, change of audit log settings or general change (see [`AclAuthorizationStrategy`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/acls/domain/AclAuthorizationStrategy.html)).   
Additionlly note that `AuditLogger.logIfNeeded(isGranted, ace)` with `isGranted=false` is only called when ACE explicitly specifies "granted=false" and not when a permission (ACE) is missing.

Therefore we suggest to enhance your audit logger implementation in order to handle other audit relevant *events".

**Example**: [Implementation of AuditLogger](src/main/java/com/sap/cp/appsec/config/AclAuditLogger.java).


#### Example logs when Advertisement is created and ACL is instantiated 
```
DEBUG 16360 --- o.s.s.w.a.i.FilterSecurityInterceptor    : Secure object: FilterInvocation: URL: /api/v1/ads/acl; Attributes: [authenticated]
DEBUG 16360 --- o.s.s.w.a.i.FilterSecurityInterceptor    : Previously Authenticated: com.sap.cloud.security.xsuaa.token.AuthenticationToken@e69e23c2: Principal: user/userIdp/adOwner; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@957e: RemoteIpAddress: 127.0.0.1; SessionId: null; Granted Authorities: ATTR:BULLETINBOARD=IL_RAA03_Board, ATTR:LOCATION=DE
DEBUG 16360 --- o.s.s.access.vote.AffirmativeBased       : Voter: org.springframework.security.web.access.expression.WebExpressionVoter@63e17053, returned: 1
DEBUG 16360 --- o.s.s.w.a.i.FilterSecurityInterceptor    : Authorization successful
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : CREATED ACE: AccessControlEntryImpl[id: 100000005024; granting: true; sid: PrincipalSid[user/userIdp/adOwner]; permission: BasePermission[...............................R=1]; auditSuccess: true; auditFailure: true]
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : CREATED ACE: AccessControlEntryImpl[id: 100000005025; granting: true; sid: PrincipalSid[user/userIdp/adOwner]; permission: BasePermission[..............................W.=2]; auditSuccess: true; auditFailure: true]
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : CREATED ACE: AccessControlEntryImpl[id: 100000005026; granting: true; sid: PrincipalSid[user/userIdp/adOwner]; permission: BasePermission[...........................A....=16]; auditSuccess: true; auditFailure: true]
DEBUG 16360 --- s.s.w.c.SecurityContextPersistenceFilter : SecurityContextHolder now cleared, as request processing completed
```
For the POST-request the user must only be authenticated (see [WebSecurityConfig](src/main/java/com/sap/cp/appsec/config/WebSecurityConfig.java)), additionally we log the creation of the three ACEs that provides the owner of the created advertisement these access permissions: read, write and admin.

#### Example logs when Advertisement owner grants permission to another user
```
DEBUG 16360 --- o.s.s.w.u.matcher.AntPathRequestMatcher  : Checking match of request : '/api/v1/ads/acl/grantPermissionsToUser/1'; against '/api/v1/ads/acl/**'
DEBUG 16360 --- o.s.s.w.a.i.FilterSecurityInterceptor    : Secure object: FilterInvocation: URL: /api/v1/ads/acl/grantPermissionsToUser/1; Attributes: [authenticated]
DEBUG 16360 --- o.s.s.w.a.i.FilterSecurityInterceptor    : Previously Authenticated: com.sap.cloud.security.xsuaa.token.AuthenticationToken@810322a0: Principal: user/userIdp/adOwner; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@957e: RemoteIpAddress: 127.0.0.1; SessionId: null; Granted Authorities: ATTR:BULLETINBOARD=IL_RAA03_Board, ATTR:LOCATION=DE
... 
DEBUG 16360 --- o.s.s.a.i.a.MethodSecurityInterceptor    : Secure object: ReflectiveMethodInvocation: public void com.sap.cp.appsec.services.AdvertisementService.grantPermissions(java.lang.Long,java.lang.String,org.springframework.security.acls.model.Permission[]); target is of class [com.sap.cp.appsec.services.AdvertisementService]; Attributes: [[authorize: 'hasPermission(#id, 'com.sap.cp.appsec.domain.Advertisement', 'administration')', filter: 'null', filterTarget: 'null']]
DEBUG 16360 --- o.s.s.a.i.a.MethodSecurityInterceptor    : Previously Authenticated: com.sap.cloud.security.xsuaa.token.AuthenticationToken@e774a5e7: Principal: user/userIdp/adOwner; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@957e: RemoteIpAddress: 127.0.0.1; SessionId: null; Granted Authorities: ATTR:BULLETINBOARD=IL_RAA03_Board, ATTR:LOCATION=DE
DEBUG 16360 --- o.s.s.acls.AclPermissionEvaluator        : Checking permission 'administration' for object 'org.springframework.security.acls.domain.ObjectIdentityImpl[Type: com.sap.cp.appsec.domain.Advertisement; Identifier: 1]'
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : GRANTED due to ACE: AccessControlEntryImpl[id: 100000005026; granting: true; sid: PrincipalSid[user/userIdp/adOwner]; permission: BasePermission[...........................A....=16]; auditSuccess: true; auditFailure: true]
DEBUG 16360 --- o.s.s.acls.AclPermissionEvaluator        : Access is granted
DEBUG 16360 --- o.s.s.access.vote.AffirmativeBased       : Voter: org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter@4f3891db, returned: 1
DEBUG 16360 --- o.s.s.a.i.a.MethodSecurityInterceptor    : Authorization successful
... 
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : GRANTED due to ACE: AccessControlEntryImpl[id: 100000005026; granting: true; sid: PrincipalSid[user/userIdp/adOwner]; permission: BasePermission[...........................A....=16]; auditSuccess: true; auditFailure: true]
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : GRANTED due to ACE: AccessControlEntryImpl[id: 100000005026; granting: true; sid: PrincipalSid[user/userIdp/adOwner]; permission: BasePermission[...........................A....=16]; auditSuccess: true; auditFailure: true]
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : CREATED ACE: AccessControlEntryImpl[id: 100000005030; granting: true; sid: PrincipalSid[user/userIdp/otherOne]; permission: BasePermission[...........................A....=16]; auditSuccess: true; auditFailure: true]
 INFO 16360 --- com.sap.cp.appsec.config.AclAuditLogger  : CREATED ACE: AccessControlEntryImpl[id: 100000005031; granting: true; sid: PrincipalSid[user/userIdp/otherOne]; permission: BasePermission[...............................R=1]; auditSuccess: true; auditFailure: true]
DEBUG 16360 --- s.s.w.c.SecurityContextPersistenceFilter : SecurityContextHolder now cleared, as request processing completed
```

For the PUT-request `/api/v1/ads/acl/grantPermissionsToUser/{advertisementId}` the user must be authenticated (see [WebSecurityConfig](src/main/java/com/sap/cp/appsec/config/WebSecurityConfig.java)). Then the `@PreAuthorize` method security expression checks, whether the user has admin permissions. Finally, as the given ACL for an existing advertisment is changed the admin permission is again checked before the new ACEs are persisted. Additionally we log the creation of the ACEs that provides another user (here: `otherOne`) these access permissions: admin and read.

### Breakpoints for troubleshoot
- AclPermissionEvaluator.hasPermission
- AclImpl.isGranted


### Other requirements to Access Control concepts, not discussed here
- Dual control principle. Example: an advertisement can only be published, when another user (with a dedicated role) has approved it.
- Time-based access control. Example: A user has only access granted in a dedicated time-period, like a representative. This person should not be able to access instances that were created/updated in a time period before or after time period.

### <a name="features"></a>Notable Features
 - REST endpoints using Spring Web MVC (`@RestController`)
 - Tests (Servlet with `RestTemplate`, `MockMvc`, JUnit, Mockito, Hamcrest
 - JPA Implementation: Hibernate
 - Spring Data (repository)
 - Security with Spring Security and Xsuaa
 - Spring Security ACL, setup database schema using Liquibase

## <a name="furtherReading"></a>Further References
- [Github: spring-projects/spring-security](https://github.com/spring-projects/spring-security)
- [Github: SAP Container Security Library (Java)](https://github.com/SAP/cloud-security-xsuaa-integration)
- [Spring.io: Domain Object Security (ACLs)](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#domain-acls)
- [Spring.io: ACL database schema](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#dbschema-acl)
- [openSAP course: Cloud-Native Development with SAP Cloud Platform](https://open.sap.com/courses/cp5)
- [Baeldung tutorial: Introduction to Spring Security ACL](https://www.baeldung.com/spring-security-acl)
