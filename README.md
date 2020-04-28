# ECS Logging for Quarkus

This dependency provides ECS compatible logging for quarkus by just adding
this to the list of dependencies.

Just add it to your quarkus project like this in maven

```
<dependency>
  <groupId>de.spinscale.quarkus</groupId>
  <artifactId>quarkus-logging-ecs</artifactId>
  <version>0.0.2</version>
</dependency>
```

or in gradle

```
compile 'de.spinscale.quarkus:quarkus-logging-ecs:0.0.2'
```

Once the project is included, your log message will look something like this

```json
{
  "@timestamp":"2020-04-28T16:01:17.338Z", "log.level": "INFO",
  "message":"Quarkus 1.4.1.Final started in 1.052s. Listening on: http://0.0.0.0:8081",
  "service.name":"api_backend","process.thread.name":"main",
  "log.logger":"io.quarkus"
}
```

This is a format that can be easily consumed by a filebeat and sent over to
Elasticsearch without any further processing.

## Introduction

As you probably know quarkus already, let's do a quick introduction into what
ECS is.

ECS is short for [Elastic Common
Schema](https://www.elastic.co/guide/en/ecs/current/index.html), a
specification for field naming in order to store event data in
Elasticsearch.

The advantage of storing data like this is that you can reuse existing
dashboards in the Elastic Stack and there is no need to change your data
when ingesting.

Quarkus comes already with a json logging extension, that one however cannot
be configured to adapt to ECS. One of the advantages of ECS is the
strictness, so you as a user do not have many options to configure. This is
what I tried to do with this extension as well.

## Configuration options

* `quarkus.logging.ecs.enable`: indicates if this should be enabled,
  defaults to `true`.
* `quarkus.logging.ecs.service-name`: An optional service name, that is
  stored in the service name field of the ECS specification. Empty by
  default.
* `quarkus.logging.ecs.stack-trace-as-array`: A boolean that indicates if
  stack traces should become an array. Defaults to `false`
* `quarkus.logging.ecs.include-origin`: A boolean that indicates if the
  origin of that log message should be included. Defaults to `false`
* `quarkus.logging.ecs.additional-fields`: A map that can contains fields,
  that should be logged with every message, empty by default.

## Compatibility with quarkus versions

| Quarkus version | quarkus-logging-ecs version |
| --------------- | --------------------------- |
| 1.4.1.Final     | 0.0.2                       |


## TODO

* Check if we can figure out the service name automatically
