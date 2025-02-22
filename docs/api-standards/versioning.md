# Versioning

Wherever possible breaking changes should be avoided, however, in some cases it is required to make breaking changes and so versioning is required. This document describes the Harness approach to versioning.

- [Backward Compatitable Changes](#backward-compatitable-changes)
- [End of Life](#end-of-life)
- [API Versioning](#api-versioning)
  * [Primary Version](#primary-version)
  * [Secondary Version](#secondary-version)
    + [Affected Services](#affected-services)
    + [Release Process](#release-process)
    + [Technical Implementation](#technical-implementation)
    + [Examples of Breaking Changes and Handling](#examples-of-breaking-changes-and-handling)

## Backward Compatible Changes

Harness does not consider the following to be breaking changes:

- Additional resources within the API
- Additional actions which can be taken against a given resource
- Addition of optional attributes on a resource
- Addition of optional filter parameters against a resource
- Order of attributes within a resource
- Length of strings auto-generated by the harness platform

## End of Life

All versions should have an EOL beyond which harness no longer officially supports and behaviour is undocumented. Currently, this period is TBD.

## API Versioning

The API is versioned on two levels:
1. Primary Version: based on the core principles of the API.
2. Secondary Version: at the implementation level.

### Primary Version

The core principles should rarely change and marks a significant departure from the basic underpinnings of the existing API e.g. a system-wide refactor of core concepts or resources. For this reason individual services or endpoints should not need to be concerned with changes at this level. This version is represented as the initial segment within the path e.g. the “v1” within `api.hanress.io/v1/`.

### Secondary Version

Within the primary version, Harness provides a secondary level of versioning which allows for breaking changes at the service and implementation level.

This versioning is represented by the date of release e.g. `2022-05-18`. This ensures the API is versioned as a whole but removes the need for the customer to keep track of different versions across multiple endpoints. However, it is still a breaking change and should so continue to be a last resort.

#### Affected Services

On roll-out of a new secondary version services fall into one of three positions: 

1. Service not affected by the change.
2. Service making the breaking change.
3. Service dependant on breaking endpoint.

In case 1 the service does not need to make any changes and can continue accepting the new version as part of it's rolling updates. 

In the case of 2 and 3 care needs to be given to support the older version until the EOL time has been reached or there are guarantees all clients have moved to a new version. Typically these are handled through different code paths within the same service as usually it is only a part of the overall service which is breaking. 

#### Release Process

To aid API stability, planning of these breaking releases should be handled centrally and follow a process allowing for clear communication and integration across products. 

1. Breaking change identified.
2. All efforts exhausted to avoid breaking change new version release estimate is decided with PM.
3. Breaking service accepts incoming calls on future version behind Feature Flag and initially returns a `501 Not Implemented` response.
4. All upstream dependant services informed of breaking change and work prioritised accordingly to handle new version.
5. Breaking service should release often allowing integration to happen in parallel. 
6. Once definition of done is met, official release date to be decided with PM possibly bundling other breaking changes in the same release.
7. Feature Flag removed on release date along with usual documentation, change-log and EOL. 

#### Technical Implementation

- The `Harness-Version` request header should be used. 
  - Preferred over a further path parameter as allows the default to be set by the platform, typically to the latest releases. 
  - Preferred over query parameters as gives the subtle UX this is something which should be set across all API interactions, similar to an auth-token. Where query parameters are often to alter individual action/endpoint interactions.
  - Preferred over content negotiation as it typically is concerned only with individual response types and not the overall API.


#### Examples of Breaking Changes and Handling

- Removal of or action against a resource type: the resource and functionality must remain on the old API, any persisted data must continue to be applied to the backing storage / db.
- Removal of a resource attribute: the attribute must continue to be available on the old API, any persisted data must continue to be applied to the backing storage / db.
- Renaming a resource attribute: internally a translation layer could ensure the naming change continues correctly on the new version as well as previous.
- Removal of a filter parameter: filters must continue to be available to the old API 
