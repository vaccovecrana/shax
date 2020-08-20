# Overview

[shax](https://en.wikipedia.org/wiki/List_of_demons_in_the_Ars_Goetia#Marquises) is
a minimal (30KB, zero dependencies), opinionated (see below) JSON structured logging
backend for SLF4j.

It is inspired by and combines heavily modified parts from [minimal-json](https://github.com/ralfstx/minimal-json)
and the [slf4j-simple](https://github.com/qos-ch/slf4j/tree/master/slf4j-simple) reference
backend implementation.

## Getting started

Pull the following dependency into your project:

```io.vacco.shax:shax:1.7.30.1```

All `shax` releases align to the `slf4j` api version they were compiled against.

Make sure to exclude other SLF4J binding in your classpath, otherwise `slf4j` will complain.

Pass in the following `System` or `Environment` properties to configure:

- `IO_VACCO_SHAX_SHOWDATETIME` or `io.vacco.shax.showdatetime` to display or hide UTC times. Defaults to `true`.
- `IO_VACCO_SHAX_LOGLEVEL` or `io.vacco.shax.loglevel` to set the root logger level. Defaults to `INFO`.
- `IO_VACCO_SHAX_PRETTYPRINT` or `io.vacco.shax.prettyprint`, `true` to output formatted JSON, `false` to output a single line. Defaults to `false`.
- `IO_VACCO_SHAX_LOGGER_X_Y_Z` or `io.vacco.shax.logger.x.y.z` (multiple times with different values) to set individual logger namespace levels.

Your regular logging statements are exactly the same, but will now look like this (pretty-print version):

```
log.trace("This is a TRACE message with format: [{}]", 1);
```

Which produces:

```
{
  "utc": "2020-08-20T17:53:12.12998",
  "utcMs": 1597945992129,
  "thread": "Test worker",
  "message": "This is a TRACE message with format: [1]",
  "logName": "io.vacco.shax.test.ShLoggingSpec",
  "logLevel": "TRACE"
}
```

If you need to add more keys to this log record, use `io.vacco.shax.logging.ShArgument.kv(String, Object)`
to insert custom Object values as JSON into your message. For example:

```
Set<String> cats = new HashSet<>();
cats.add("fido");
cats.add("garfield");
cats.add("felix");

log.info("Some cats [{}]", kv("cats", cats));
```

To get:

```
{
  "utc": "2020-08-20T18:00:16.496595",
  "utcMs": 1597946416496,
  "thread": "Test worker",
  "message": "Some cats [io.vacco.shax.logging.ShArgument@942b58d]",
  "logName": "io.vacco.shax.test.ShLoggingSpec",
  "logLevel": "INFO",
  "cats": ["felix", "fido", "garfield"]
}
```

## Caveats

`shax` is opinionated. It will:

- Output only to `stderr`, no files or TCP forwarding. So plug your favorite log forwarding
  agent at the process level to capture log output.
- Display time:
  - In the UTC timezone only (the entire planet lives there).
  - As `ISO-8601` extended offset date-time format.
  - As a Unix millisecond timestamp.
- Not support `slf4j`'s MDC logging.

If you're not okay with any of these, then `shax` is not for you.

Go back to [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder)
as it may suit your use case better.


## Disclaimer

> This project is not production ready, and still requires security and code correctness audits.
> You use this software at your own risk. Vaccove Crana, LLC., its affiliates and subsidiaries
> waive any and all liability for any damages caused to you by your usage of this software.
