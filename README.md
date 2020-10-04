# Overview

[shax](https://en.wikipedia.org/wiki/List_of_demons_in_the_Ars_Goetia#Marquises) is
a minimal (30KB, zero dependencies), opinionated (see below) JSON structured logging
backend for SLF4j.

It is inspired by and combines heavily modified parts from [minimal-json](https://github.com/ralfstx/minimal-json)
and the [slf4j-simple](https://github.com/qos-ch/slf4j/tree/master/slf4j-simple) reference
backend implementation.

## Getting started

Pull the following dependency into your project (currently only available in JCenter):

[![Download](https://api.bintray.com/packages/vaccovecrana/vacco-oss/shax/images/download.svg) ](https://bintray.com/vaccovecrana/vacco-oss/shax/_latestVersion)

```io.vacco.shax:shax:<VERSION>```

> Note: All `shax` releases align to the `slf4j-api` version they were compiled against.
> Make sure to exclude other SLF4J bindings in your class path, otherwise `slf4j` will complain.

Your regular SLF4J logging statements are exactly the same, but will now look like this (single-line version):

```
log.info("Let's see some cats and owners");
```

Which produces:

```
{"utc":"2020-08-21T13:42:42.312455","utc_ms":1598017362312,"level":"INFO","level_value":20,"logger_name":"io.vacco.shax.test.ShLoggingSpec","thread_name":"Test worker","message":"Let's see some cats and owners"}
```

Harsh on the eyes? Make it pretty with `io.vacco.shax.prettyprint`:

```
{
  "utc": "2020-08-21T13:43:20.788755",
  "utc_ms": 1598017400788,
  "level": "INFO",
  "level_value": 20,
  "logger_name": "io.vacco.shax.test.ShLoggingSpec",
  "thread_name": "Test worker",
  "message": "Let's see some cats and owners"
}
```

Still harsh on the eyes? Switch to dev mode with `io.vacco.shax.devmode`:

```
INFO [1598017441508] (Test worker): Let's see some cats and owners
```

Now add more keys to a log record with `io.vacco.shax.logging.ShArgument.kv(String, Object)`
    
```
Map<String, String> catOwners = new TreeMap<>();
catOwners.put("Garfield", "Jon");
catOwners.put("Arlene", "Jon");
catOwners.put("Azrael", "Gargamel");
catOwners.put("Chi", "Youhei");

log.info("Cats and Owners [{}]", kv("catOwners", catOwners));
```

To get:

```
{
  "utc": "2020-08-21T13:45:44.956641",
  "utc_ms": 1598017544956,
  "level": "INFO",
  "level_value": 20,
  "logger_name": "io.vacco.shax.test.ShLoggingSpec",
  "thread_name": "Test worker",
  "message": "Cats and Owners [{catOwners=java.util.TreeMap}]",
  "catOwners": {
    "Arlene": "Jon",
    "Azrael": "Gargamel",
    "Chi": "Youhei",
    "Garfield": "Jon"
  }
}
```

Or in dev mode:

```
INFO [1598017595726] (Test worker): Cats and Owners [{catOwners=java.util.TreeMap}]
{
  "Arlene": "Jon",
  "Azrael": "Gargamel",
  "Chi": "Youhei",
  "Garfield": "Jon"
}
```

Not happy with the output format? Place a record transformer on the target `Logger` you intend to modify.
For example, let's say we want to make the logger `io.vacco.shax.test` align more to the
[logstash-logback-encoder fields](https://github.com/logstash/logstash-logback-encoder#standard-fields) 
(which it kind of already does :P).

```
Logger log = ShLogger.withTransformer(
    LoggerFactory.getLogger(ShLoggingSpec.class),
    r -> {
      r.put("@timestamp", r.get(ShLogRecord.ShLrField.utc.name()));
      r.put("@version", 1);
      r.remove(ShLogRecord.ShLrField.utc.name());
      r.remove(ShLogRecord.ShLrField.utc_ms.name());
      return r;
    }
);
```

Which produces:

```
{
  "level": "INFO",
  "level_value": 20,
  "logger_name": "io.vacco.shax.test.ShLoggingSpec",
  "thread_name": "Test worker",
  "message": "Cats and Owners [{catOwners=java.util.TreeMap}]",
  "catOwners": {
    "Arlene": "Jon",
    "Azrael": "Gargamel",
    "Chi": "Youhei",
    "Garfield": "Jon"
  },
  "@timestamp": "2020-08-21T13:57:59.74028",
  "@version": 1
}
```

Neat!

## Configuration

Pass in the following `Environment` or `System`  properties to configure:

- `IO_VACCO_SHAX_DEVMODE` or `io.vacco.shax.devmode` to display messages like [pino-pretty](https://github.com/pinojs/pino-pretty) would. Defaults to `false`.
- `IO_VACCO_SHAX_SHOWDATETIME` or `io.vacco.shax.showdatetime` to display or hide UTC times. Defaults to `true`.
- `IO_VACCO_SHAX_LOGLEVEL` or `io.vacco.shax.loglevel` to set the root logger level. Defaults to `INFO`.
- `IO_VACCO_SHAX_PRETTYPRINT` or `io.vacco.shax.prettyprint`, `true` to output formatted JSON, `false` to output a single line. Defaults to `false`.
- `IO_VACCO_SHAX_LOGGER_X_Y_Z` or `io.vacco.shax.logger.x.y.z` (multiple times with different values) to set individual logger namespace levels.

> Note: `shax` will search for `Environment` variables, then `System` properties to load these values.

## Caveats

Again, `shax` is opinionated. It will:

- Output only to `stderr`, no Files or TCP/UDP forwarding. So plug your favorite log forwarding agent at the process level to capture log output.
- Display time:
  - In the UTC timezone only (the entire planet lives there).
  - As `ISO-8601` extended offset date-time format.
  - As a Unix millisecond timestamp.
- Not support `slf4j`'s MDC logging.

If you're not okay with any of these, then `shax` is not for you.

Go back to [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) as it may suit your use case better.

## Using record transformers

If you use log record transformer functions in your code, be aware that:

- Only a single transformer can be assigned once per `Logger` instance. Reassignments will result in errors.
- In `dev` mode, the only mandatory fields you must preserve in your record transformer are: `[level, thread_name, message]`.
- Any transformer function *may* be stateless and *must* be thread-safe, since many threads will be calling your code.

## Disclaimer

> This project is not production ready, and still requires security and code correctness audits.
> You use this software at your own risk. Vaccove Crana, LLC., its affiliates and subsidiaries
> waive any and all liability for any damages caused to you by your usage of this software.
