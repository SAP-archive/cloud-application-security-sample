# Cloud Application Security Samples

## Description
Implementing security in SAP Cloud Platform Applications?  
The SAP Cloud Platform offers specific support to implement authentication and authorization for business users that access SAP Cloud Platform applications. As a developer of such an applications you have different options how to leverage the possibilities that SAP Cloud Platform offers. In this repository we want to showcase how this can be done. These samples demonstrate several different technologies to implement security authentication and authorization. Specifically, they integrate to SAP Cloud Platform XSUAA service using the [SAP Container Security Library (Java)](https://github.com/SAP/cloud-security-xsuaa-integration), which is available on [maven central](https://search.maven.org/search?q=com.sap.cloud.security).

### Overview of samples
Each sample is provided in a separate module.

   Module | Description | Availability
   ---- | -------- | ----
      [spring-security-basis](spring-security-basis) | This module shows how to implement basic access control in Spring-based SAP Cloud Platform applications. It leverages Spring Security 5.x and integrates to SAP Cloud Platform XSUAA service (OAuth Resource Server) using the SAP Container Security Library (Java), which is available on maven central. | 2019-06
   [spring-security-acl](spring-security-acl) | This module shows the usage of Spring Security ACL to implement instance-based access control in Spring-based SAP Cloud Platform applications. | 2019-01

## How to obtain support
For any question please [open an issue](https://github.com/SAP/cloud-application-security-sample/issues/new) in GitHub and make use of the [labels](https://github.com/SAP/cloud-application-security-sample/labels) in order to refer to the sample and to categorize the kind of the issue.

## License
Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, v.2 except as noted otherwise in the [LICENSE](/LICENSE.pdf) file and in the [CREDITS](/CREDITS) file.
