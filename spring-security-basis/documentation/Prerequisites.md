# Prerequisites

## Table of Contents
- [Local Setup](#headline-2)
  - [Proxy Settings](#headline-2.1)
  - [Java 8 JDK](#headline-2.2)
  - [Maven](#headline-2.3)
  - [Eclipse IDE](#headline-2.4)
  - [JMeter](#headline-2.5)
  - [Cloud Foundry Client](#headline-2.6)
  - [Docker](#headline-2.7)
- [Project Setup](#headline-3)
  - [Clone Git Repository](#headline-3.1)
  - [Import Maven project into Eclipse](#headline-3.2)

<a id="headline-2"></a>
## Local Setup

<a id="headline-2.1"></a>
### Proxy Settings
- Windows: Download and run [proxyEnv.cmd](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/tree/master/CoursePrerequisites/localEnvSetup/proxyEnv.cmd) to permanently set the proxy settings in your environment.
- MacOSX: Add the lines inside [proxyEnv.bash_profile](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/tree/master/CoursePrerequisites/localEnvSetup/proxyEnv.bash_profile) to your .bash_profile to permanently set the proxy settings in your environment. After setting this, you need to re-login (or reboot).

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

We make use of [Maven wrapper](https://github.com/takari/maven-wrapper), which is located at `.mvn\wrapper` in each module.
In the `.mvn/wrapper/maven-wrapper.properties` file, it can be defined which Maven version should be used.
The wrapper can be executed via the `mvnw` script located at the root of a module (one `bat` file for Windows and one shell script for Linux/Git Bash).
It takes care of installing the correct Maven version, so no manual local installation of Maven is required.

However, you still need to provide Maven configuration on your computer:
 - Create the directory `~/.m2/`, where `~` is your home directory, e.g. `C:\Users\D012345`.
 > Note: Windows explorer does not allow you create a directory ".name" - you have to add a dot at the end, i.e. ".name.", which will then be removed.
 - Download the [settings.xml](https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/CoursePrerequisites/localEnvSetup/settings.xml) configuration file and save it in the `.m2` directory created in the previous step.

To use the maven wrapper start a spring boot application, execute
- **`./mvnw spring-boot:run` (Linux/Git bash)** or
- **`mvnw spring-boot:run` (Windows)**.

<a id="headline-2.4"></a>
### Eclipse IDE
An integrated development environment (IDE) is useful for development and experimenting with the code.
We recommend to use Eclipse as the following descriptions are tailored for it.

- Eclipse Oxygen
  - [Download Eclipse](https://spring.io/tools/eclipse) (select Eclipse Oxygen - Eclipse IDE for Java EE Developers)
  - Unpack the ZIP file to a suitable location on your computer, e.g. `C:\dev\eclipse`
- Assign installed Java JRE to Eclipse: `Window` - `Preferences`, type `jre` in filter, in `Installed JREs`, select `Add...`->`Standard VM` and enter the path to your Java installation.
- Set proxies within Eclipse: `Window` - `Preferences`, type `network` in filter, in `network connections`, select `manual` and add the following values
  - For http and https: `proxy.wdf.sap.corp`, port `8080`
  - bypass: `*.sap.corp`

> Note: the Community edition of [IntelliJ IDEA](https://www.jetbrains.com/idea/) is an alternative IDE, but you have to figure out on your own how to import the projects properly.

<a id="headline-2.5"></a>
<a id="headline-2.7"></a>
### Cloud Foundry Client
The developed microservices will run on the Cloud Foundry platform.

- Install the Cloud Foundry Command Line Interface (CLI) following [this](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) guide
- Create an account using this tutorial: [CF @SAP BTP](https://help.cf.sap.hana.ondemand.com/). Don't forget to request your own trial space as explained there.

<a id="headline-2.7"></a>
### Docker
Docker is needed to conveniently start services like PostgreSQL, Redis or RabbitMQ on your local machine.

Download and install the latest [**Docker for Windows**](https://www.docker.com/docker-windows) or [**Docker for Mac**](https://www.docker.com/docker-mac) release.
See the [Getting Started](https://docs.docker.com/get-started/) documentation if you're not yet familiar with Docker.

> In case you experience problems with the latest version, you can try to install older versions that are linked in the release notes: [Windows](https://docs.docker.com/docker-for-windows/release-notes/), [Mac](https://docs.docker.com/docker-for-mac/release-notes/).

> Note: the docker installer automatically enables Hyper-V if you have not done so yet.
This requires a restart that may take several minutes to complete.

> Note: Hyper-V interferes with VirtualBox (Hyper-V must to be enabled for Docker, but this [crashes VirtualBox](https://www.virtualbox.org/ticket/16801))

To start all docker containers required for a module, execute `docker-compose up -d` in the directory of the module.
This will run all containers as defined in the `docker-compose.yml` file located at the root of the module. To tear down all containers, execute `docker-compose down`.

Execute `docker ps` to view all running docker images.

<a id="headline-3"></a>
## Project Setup
Each module is a separate Java project in a separate folder, but all projects are stored in the same Git repository.

<a id="headline-3.1"></a>
### Clone Git Repository
The project can be cloned using this the following URL: `git@github.wdf.sap.corp:CPSecurity/cp-application-security.git`.
Either use the command line and type `git clone https://github.wdf.sap.corp/CPSecurity/cp-application-security` or use the Git perspective in Eclipse and choose `Clone a Git Repostory`.

> Note: In case SSH is not working make use of the HTTPS link when cloning the repository.

<a id="headline-3.2"></a>
### Import Maven project into Eclipse
Within Eclipse you need to import the source code.

1. Select `File - Import` in the main menu
2. Select `Maven - Existing Maven Projects` in the dialog
3. Import the module you want to work on, e.g. `spring-security-acl` by selecting the respective directory and clicking `OK`
4. Finally, update the Maven Settings of the project by presssing `ALT+F5` and then `OK`.
