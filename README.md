[![Build Status](https://travis-ci.org/lwes/lwes-java.svg?branch=master)](https://travis-ci.org/lwes/lwes-java)

This is the Java API for the Light Weight Event System.

***
Prerequisites
- JDK 1.5.x (http://java.sun.com/) or later
- Maven 3.0.4 (http://apache.maven.org/) or later
- GPG, if you are going to deploy a release (http://www.gnupg.org/download)

***
How to build:
% mvn clean package

To create a release:
% mvn versions:set -DnewVersion=VERSION
% mvn clean deploy -P release

NOTE: Read this document for deploying releases to central:
http://central.sonatype.org/pages/ossrh-guide.html

***
To increase the read buffer size on the multicast socket, set the system property
MulticastReceiveBufferSize. For example,

java -DMulticastReceiveBufferSize=8388608 ...
