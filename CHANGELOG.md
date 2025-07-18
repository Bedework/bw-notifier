# Release Notes

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased (6.1.0-SNAPSHOT)

## [6.0.0] - 2025-07-17
* First jakarta release
* Changes for orm initialisation
* Update web.xml for jakarta
* Use more fluent approach for db api.

## [5.0.5] - 2025-02-06
* Upgrade library versions
* Move response classes and ToString into bw-base module.
* Use the refresh method
* Switch to use DbSession from bw-database.
* Convert the hql queries into valid jpql. No hibernate specific terms were required (I think).
* Pre-jakarta release

## [5.0.4] - 2024-12-09
* Upgrade library versions

## [5.0.3] - 2024-11-26
* Upgrade library versions

## [5.0.2] - 2024-06-06
* Upgrade library versions
* Fix needed to deal with util.hibernate bug relating to static sessionFactory variable.

## [5.0.1] - 2024-03-22

## [5.0.0] - 2022-02-12
* Use bedework-parent for builds
* Update library versions
* Changes for schema build fixes.

## [4.0.11] - 2020-11-15
* Update library versions
* Pass class loader as parameter when creating new objects. JMX interactions were failing.
* Changes to fix OptimisticLockException
* Watch for null subscription.
* Unwrap subscription for refresh

## [4.0.10] - 2020-03-20
* Update library versions

## [4.0.9] - 2019-10-16
* Update library versions
* Switch to PooledHttpClient

## [4.0.8] - 2019-08-27
* Update library versions

## [4.0.7] - 2019-06-27
* Update library versions

## [4.0.6] - 2019-04-15
* Update library versions

## [4.0.5] - 2018-12-14
* 4.0.4 failed.

## [4.0.4] - 2018-12-14
* Update library versions
* Logging changes

## [4.0.3] - 2018-11-27
* Update library versions

## [4.0.2] - 2018-08-04
* 4.0.1 failed.

## [4.0.1] - 2018-08-04
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

## [4.0.0] - 2015-10-30
* First github/maven release
* Error in sync-collection response. Fix it and fix handling of the response at client end. 
* Add unsubscribe URI.
* Don't force check of notification on subscribe. Seems to lead to race conditions.
* Fix possible cause of NPE (no response for notification collection url).
* Don't wipe the sync toklen on (re)subscribe - causes duplicate emails.
* Bad call to httclient for put
* Remove extraneous defs from hbm
* Fix up emails - extra values from event.
* Update to ensure admin note parsers registered
* ...
