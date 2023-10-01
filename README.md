# JavaScript Executor Api

JavaScript Executor Api is a RESTful API 
allows you to load, execute, 
and get result of your JS code by sending HTTP request. 

Api conceived as public, so any security was provided.
UserId (e.g. nickname) only needed 
for filtering and to know owner of script. 

## Technology Stack

- Java 17
- Spring (Boot, Mongo, REST)
- MongoDb
- Java Multithreading
- GraalVM (Polyglot Programming, Embedding Languages)
- Swagger

## Requirements

- Java 17+
- Maven 3.8.6+

## Startup with GraalVM

You need to set `JAVA_HOME` to avoid further issues just
```
export JAVA_HOME=<path to java home>
```

The JavaScript application will achieve near-native performance, 
if GraalVM’s JavaScript engine is executed 
on a JVM with the Graal compiler enabled.

```
mvn clean && mvn package && mvn exec:exec@graal
```

This will ensure you are using Graal for JIT compilation 
of GraalVM’s JavaScript code.

For the sake of comparison (or if you run into troubles with Graal), 
you can execute the same without Graal compilation enabled:

```
mvn clean && mvn package && mvn exec:exec@nograal
```

## Ways to work with Executor API

### Function execution

You have to provide function and params to execute with, e.g.
```js
function sum(p1, p2) {
    return p1 + p2;
}
```

Result of execution will be persisted 
and can be accessed via our API.

### Void function execution

If you don't need to get result back, you can make `Void function execution`.
It could be useful for sending notification, health checking external api, 
using external api and much more.

You need to simply provide void function, e.g.
```js
function helloWorld(p1) {
    console.log("Hello, " + p1 + "!")
}
```

Result of execution will not be persisted 
and console output will be ignored.

> Should be provided only one function per execution, 
> otherwise exception will be thrown.

### Console print execution

You can run the common javascript code, where via `console.log`
you can persist all needed data. 

```js
function ageCheck(p1) {
    return p1 > 18;
} 

if (ageCheck(input)) {
    console.log("access=approved");
} else {
    console.log("access=denied")
}
```

`input` can be set outside javascript code by our API.

> Console log should be applied to specific `key=value` 
> case to be parsed properly. Should be in lower case and 
> mustn't be used specific symbols, e.g. dot, hyphen and et cetera.

### Variable persist execution

You can run the common javascript code, 
where you can persist all needed data 
as `const param1 = "value1"` variable.
Executor read all variables
and persist data from them.

```js
function ageCheck(p1) {
    return p1 > 18;
} 

if (ageCheck(input)) {
    const access = "approved";
} else {
    const access = "denied";
}
```

`input` can be set outside javascript code by our API.

> If you want to persist via `variables` your data, 
> let's define the next name convention for variables that need to be persisted:
> `EXECUTOR__variableName`.

## OpenAPI & Swagger

OpenAPI & Swagger were connected to the Executor API. So, It is available by

```
localhost:8080\swagger-ui
```

Path might be changed, 
so check in properties to ensure the path is still the same.


## Endpoints

* `GET api/v1/code-executor/scripts/{scriptId}/result`

* `GET api/v1/code-executor/scripts/{scriptId}`

* `GET api/v1/code-executor/scripts/{filterBy}/?userId=&sortBy=&page=`

* `POST api/v1/code-executor/scripts/?blocking=&schedule=`

* `PUT api/v1/code-executor/scripts/{scriptId}`

* `PATCH api/v1/code-executor/scripts/{scriptId}?forced=`

* `DELETE api/v1/code-executor/scripts/{scriptId}`

## Filtering & Sorting

To filter a requested data you need to correctly create `filterBy` string.
Here is examples of filtering strings:

- /executionStatus=eq:CANCELLED/
- /executionStatus=eq:NEW?scriptId=gte:10/

To sort a requested data you need to correctly fill `sortBy` query param.
Here is example of sorting query param:

- ?sortBy=asc(executionStatus)
- ?sortBy=desc(executionStatus),asc(userId)

## Persistence

There a few way to persist:

- in-memory 
- mongo

To select needed to fill `executor.persistence.storage.type` property.
Use profile `local` to use local presets for mongo db connection.

### Mongo Db Docker Launch

To launch the mongo container in docker just execute:

```shell
$ docker run --name local-mongo --rm -d -e MONGO_INITDB_ROOT_USERNAME=mongo -e MONGO_INITDB_ROOT_PASSWORD=mongo -p 27017:27017 mongo:6
```
