# Cloud Application Security Samples

## Description
Implementing security in SAP Cloud Platform Applications?  
The SAP Cloud Platform offers specific support to implement authentication and authorization for business users that access SAP Cloud Platform applications. As a developer of such applications you have different options how to leverage the possibilities that SAP Cloud Platform offers. In this repository we want to showcase how this can be done. These samples demonstrate several different technologies to implement security authentication and authorization. Specifically, they integrate to SAP Cloud Platform XSUAA service using one of SAP's Container Security Libraries.

## Overview of Samples
Each sample is provided in a separate module.

Module | Description | Availability | Prerequisite
---- | -------- | ---- | ---
[spring-security-basis](spring-security-basis) | This module shows how to implement basic access control in Spring-based SAP Cloud Platform applications. It leverages Spring Security 5.x and integrates to SAP Cloud Platform XSUAA service (OAuth Resource Server) using the SAP Container Security Library (Java), which is available on maven central. | 2019-06 | [openSAP course: Cloud-Native Development with SAP CP](https://open.sap.com/courses/cp5) 
[spring-security-acl](spring-security-acl) | This module shows the usage of Spring Security ACL to implement instance-based access control in Spring-based SAP Cloud Platform applications. | 2019-01 | Module: [spring-security-basis](spring-security-basis) 

   
<a id='components'></a>
## Understanding OAuth 2.0 Components
To better understand the content of this repository, you should gain a rough understanding about the SAP CP OAuth 2.0 components, which are depicted in figure below.

![](images/Figure_OAuth2.0_SAP_CP_Components.png)

#### OAuth Resource Server
First, we still have a **microservice** or CF application that we want to secure. In OAuth terminology this is the **Resource Server** that protects the resources by checking the existence and validity of an OAuth2 access token before allowing a request from the Client to succeed.

#### OAuth Access Token (JWT)
Access and refresh tokens in the form of **JSON Web Token (JWT)** represent the user’s identity and authorization claims. If the access token is compromised, it can be revoked, which forces the generation of a new access token via the user’s refresh token.

Example JWT
```json
{
  "client_id": "sb-xsapplication!t895",
  "cid": "sb-xsapplication!t895",
  "exp": 2147483647,
  "user_name": "John Doe",
  "user_id": "P0123456",
  "email": "johndoe@test.org",
  "zid": "1e505bb1-2fa9-4d2b-8c15-8c3e6e6279c6",
  "grant_type": "urn:ietf:params:oauth:grant-type:saml2-bearer",
  "scope": [ "xsapplication!t895.Display" ],
  "xs.user.attributes": {
    "country": [
      "DE"
    ]
  }
}
```

#### OAuth Authorization Server
Furthermore we have the **Extended Services for User Account and Authentication (XSUAA)** that acts as **OAuth Authorization Server** and issues authorization codes and JWT tokens after the user was successfully authenticated by an identity provider. Technically the XSUAA is a SAP-specific extension of CloudFoundry’s UAA service to deal with authentication and authorization.

#### OAuth Client
The **Application Router (approuter)** is an edge service that provides a single entry point to a business application that consists of several backend microservices. It acts as reverse proxy that routes incoming HTTP requests to the configured target microservice, which allows handling Cross-origin resource sharing (CORS) between the microservices. It plays a central role in the OAuth flow.

Just like HTTP, token-based authentication is stateless, and therefore for scalability reasons an OAuth Resource Server must not store a JWT. The consequence would be that the JWT is stored client side as it must be provided with every request. Here, the Application Router takes over this responsibility and acts an **OAuth Client** and is mainly responsible for managing authentication flows.

The Application Router takes incoming, unauthenticated requests from users and initiates an OAuth2 flow with the XSUAA. After the user has successfully logged on the Identity Provider the XSUAA considers this request as authenticated and uses the information of the Bearer Assertion to finally create a JWT containing the authenticated user as well as all scopes that he or she has been granted. Furthermore the Application Router enriches each subsequent request with the JWT, before the request is routed to a dedicated microservice (instance), so that they are freed up from this task.

> In this flow it is important to notice that the JWT never appears in the browser as the Application Router acts as OAuth client where the user “authorizes” the approuter to obtain the authorizations - the JWT - from the XSUAA component.

#### Conclusion

You need to configure the Application Router for your business application as explained in the free [openSAP course](https://open.sap.com/courses/cp5) exercises:

- [Exercise 22: Deploy Application Router and Set Up Authentication](https://github.com/SAP/cloud-bulletinboard-ads/blob/Documentation/Security/Exercise_22_DeployApplicationRouter.md)
- [[Optional] Exercise 23: Setup Generic Authorization](https://github.com/SAP/cloud-bulletinboard-ads/blob/Documentation/Security/Exercise_23_SetupGenericAuthorization.md)

Note that the Application Router can be bypassed and the microservice can directly be accessed. So the backend microservices must protect all their endpoints by validating the JWT access token and implementing proper scope checks.

In order to validate an access token, the JWT must be decoded and its signature must be verified with one of the JSON Web Keys (JWK) such as public RSA keys. Furthermore the claims found inside the access token must be validated. For example, the client id (`cid`), the issuer (`iss`), the audience (`aud`), and the expiry time (`exp`).  
Hence, every microservice has to maintain a service binding to the XSUAA that provides the XSUAA url as part of `VCAP_SERVICES` to get the current JWKs and has to configure the XSUAA as OAuth 2.0 Resource Server with its XSUAA access token validators by making use of one of SAP's Container Security Libraries.

## How to obtain support
For any question please [open an issue](https://github.com/SAP/cloud-application-security-sample/issues/new) in GitHub and make use of the [labels](https://github.com/SAP/cloud-application-security-sample/labels) in order to refer to the sample and to categorize the kind of the issue.

## License
Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, v.2 except as noted otherwise in the [LICENSE](/LICENSE.pdf) file and in the [CREDITS](/CREDITS) file.
