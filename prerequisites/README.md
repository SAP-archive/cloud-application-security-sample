# Prerequisites

## Table of Contents
- [Local Setup](#headline-2)
  - [Java 8 JDK](#headline-2.2)
  - [Maven](#headline-2.3)
  - [Eclipse IDE](#headline-2.4)
  - [Cloud Foundry Client](#headline-2.6)
  - [Docker](#headline-2.7)
- [Project Setup](#headline-3)
  - [Clone Git Repository](#headline-3.1)
  - [Import Maven project into Eclipse](#headline-3.2)
- [Rest API Testing Tools](#headline-4)

<a id="headline-2"></a>
## Local Setup
In case you like to run the example locally, you need to prepare your local system environment. Like the sample code this comes with no warranty and we can not provide support here. For further details see also [LICENCE](LICENCE.pdf) file.

<a id="headline-2.2"></a>
### Java 8 JDK
- Install the latest version of the [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) on your machine (at least Java 8).
- Windows:
    - Run the console `cmd` and enter `setx JAVA_HOME "C:\Program Files\Java\jdk1.8.X"` (replace the path with the path leading to your JDK installation)
    - To test your installation, open a new console and run both `"%JAVA_HOME%\bin\java" -version` and `java -version`. Both should return "java version 1.8.X".
- MacOSX: nothing to do, env variables should be adjusted automatically.

<a id="headline-2.3"></a>
### Maven
The builds of the individual microservices are managed using Apache Maven.

Install and configure Maven as documented [here](https://maven.apache.org/users/index.html).

To test your installation, open a new console and run `mvn --version`.


<a id="headline-2.4"></a>
### Eclipse IDE
An integrated development environment (IDE) is useful for development and experimenting with the code.
We recommend to use Eclipse as the following descriptions are tailored for it.

- Eclipse IDE
  - [Download Eclipse](https://spring.io/tools/eclipse) (select Eclipse IDE for Java EE Developers)
  - Unpack the ZIP file to a suitable location on your computer, e.g. `C:\dev\eclipse`
- Assign installed Java JRE to Eclipse: `Window` - `Preferences`, type `jre` in filter, in `Installed JREs`, select `Add...`->`Standard VM` and enter the path to your Java installation.
- Optionally you can configure your proxies within Eclipse as explained [here](https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Freference%2Fref-net-preferences.htm).

> Note: the Community edition of [IntelliJ IDEA](https://www.jetbrains.com/idea/) is an alternative IDE.


<a id="headline-2.7"></a>
### Cloud Foundry Client
The developed microservices will run on the Cloud Foundry platform.

- Install the Cloud Foundry Command Line Interface (CLI) following [this](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) guide.
- Create a account as explained in this tutorial: [SAP Cloud Platform: Get a Free Trial Account in Cloud Foundry environment](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/e3d82674bd68448eb85198619aa99b6d.html#42e7e54590424e65969fced1acd47694.html).


<a id="headline-2.7"></a>
### Docker
Docker is needed to conveniently start services like PostgreSQL on your local machine.

Download and install the latest [**Docker for Windows**](https://www.docker.com/docker-windows) or [**Docker for Mac**](https://www.docker.com/docker-mac) release.
See the [Getting Started](https://docs.docker.com/get-started/) documentation if you're not yet familiar with Docker.

> In case you experience problems with the latest version, you can try to install older versions that are linked in the release notes: [Windows](https://docs.docker.com/docker-for-windows/release-notes/), [Mac](https://docs.docker.com/docker-for-mac/release-notes/).

> Note: the docker installer automatically enables Hyper-V if you have not done so yet.
This requires a restart that may take several minutes to complete.

> Note: Hyper-V interferes with VirtualBox (Hyper-V must to be enabled for Docker, but this [crashes VirtualBox](https://www.virtualbox.org/ticket/16801))

To start all docker containers required for a sample module, execute `docker-compose up -d` in the directory of the module.
This will run all containers as defined in the `docker-compose.yml` file located at the root of the module. To tear down all containers, execute `docker-compose down`.

Execute `docker ps` to view all running docker images.


<a id="headline-3"></a>
## Project Setup
Each module is a separate Java project in a separate folder as part of this Git repository.

<a id="headline-3.1"></a>
### Clone Git Repository
The project can be cloned using this the following URL: `git@github.com:SAP/cloud-application-security-sample.git`.
Either use the command line and type `git clone https://github.com/SAP/cloud-application-security-sample.git` or use the Git perspective in Eclipse and choose `Clone a Git Repostory`.

> Note: In case SSH is not working make use of the HTTPS link when cloning the repository.

<a id="headline-3.2"></a>
### Import Maven project into Eclipse
Within Eclipse you need to import the source code.

1. Select `File - Import` in the main menu
2. Select `Maven - Existing Maven Projects` in the dialog
3. Import the module you want to work on, e.g. `spring-security-acl` by selecting the respective directory and clicking `OK`
4. Finally, update the Maven Settings of the project by presssing `ALT+F5` and then `OK`.

<a id="headline-4"></a>
## REST API Testing Tools
- [Postman](https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop) is a Chrome Plugin that helps to create and test custom HTTP requests.
- You might need to install another [`Postman Interceptor` Chrome Plugin](https://chrome.google.com/webstore/detail/postman-interceptor/aicmkgpgakddgnaphhhpliifpcfhicfo), which will help you to send requests which uses browser cookies through the `Postman` app.
