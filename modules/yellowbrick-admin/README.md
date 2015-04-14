# Yellowbrick Admin

Welcome to the Yellowbrick Admin module.

## Prerequisites

Besides the global pre-requisites (JDK8, ...), there are a few requirements specific to building the admin webapp.
These are mostly related to building the assets. They are:

* on the Mac the latest **XCode** will fulfill requirements for a C compiler and Python
* on Windows you need the latest [Python 2.x.x](https://www.python.org/getit/windows/). Be sure to set the **PYTHON** environment variable
* on Windows you also need [Visual Studio express 2013 for windows desktop](http://www.microsoft.com/en-us/download/details.aspx?id=44914)
* [nodejs](https://nodejs.org/) (on Windows make sure to add npm to the **PATH**)

* Access to Kabisa's [Nexus](http://nexus.kabisa.nl/index.html#welcome)
* Oracle 10g for running the app locally

## Getting started

* Run `mvn spring-boot:run`

## Running in development

* Run `mvn spring-boot:run`
* If you want to attach a debugger: `mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"`
* Sass is compiled with gulp, which is bound to maven. Use `gulp watch` to have stylesheets hot reload
* Your IDE may use a different target directory than maven, specially when running on a container. You can overwrite the default target for gulp actions: `gulp watch --target target/yellowbrick-admin-0.0.1-SNAPSHOT/WEB-INF/classes/static/`
