# bw-notifier [![Build Status](https://travis-ci.org/Bedework/bw-notifier.svg)](https://travis-ci.org/Bedework/bw-notifier)

This project provides a notification service for
[Bedework](https://www.apereo.org/projects/bedework).

This is a service running on the application server which can deliver
noifications to subscribed users via email.

## Requirements

1. JDK 17
2. Maven 3

## Building Locally

> mvn clean install

## Releasing

Releases of this fork are published to Maven Central via Sonatype.

To create a release, you must have:

1. Permissions to publish to the `org.bedework` groupId.
2. `gpg` installed with a published key (release artifacts are signed).

To perform a new release use the release script:

> ./bedework/build/quickstart/linux/util-scripts/release.sh <module-name> "<release-version>" "<new-version>-SNAPSHOT"

When prompted, indicate all updates are committed

For full details, see [Sonatype's documentation for using Maven to publish releases](http://central.sonatype.org/pages/apache-maven.html).


## Release Notes
* Initial release

### 4.0.1
* Update library versions
* Fix email adaptor to allow multiple body parts. Get subject and other data from template. Send XML document to template.
* Allow from, to, cc, and bcc to be specified in template.
* Don't save dynamic information stored in config
* Add capability to get CardDAV information for notifications.
* Fix namespace to match notification document element namespace.
* Fix check to make sure the carddav server is only called once for each href.
* Issue #101: Notifier: The test for mailto should be case-insensitive.
* Pass recipients to templates for processing.
* Pass JSON event object to templates for resource-change notifications.
* Handle recurring and deleted events.
* Fix handling of deleted events.
* Change parameter name from resource to resourceName.
* On shutdown check for null handlers

### 4.0.2
* 4.0.1 failed.

### 4.0.3
* Update library versions

### 4.0.5 (403 failed)
* Update library versions
* Logging changes

### 4.0.6
* Update library versions

### 4.0.7
* Update library versions

### 4.0.8
* Update library versions

### 4.0.9
* Update library versions
* Switch to PooledHttpClient

### 4.0.10
* Update library versions

### 4.0.11
* Update library versions
* Pass class loader as parameter when creating new objects. JMX interactions were failing.
* Changes to fix OptimisticLockException
* Watch for null subscription.
* Unwrap subscription for refresh

### 5.0.0
* Use bedework-parent for builds.
* Update library versions
* Changes for schema build fixes.

### 5.0.0
* Update library versions
* Simplify the configuration utilities.
* Replace "synch" with "notifier" adn rename some methods. (Notifier was copied to a large extent from synch engine).
* Make notifier a war only. Ear screws up classpath so hibernate doesn't work
* Remove bw-xml 

