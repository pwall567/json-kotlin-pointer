# json-kotlin-pointer

Kotlin implementation of [JavaScript Object Notation (JSON) Pointer](https://tools.ietf.org/html/rfc6901).

## Quick Start

This library uses the JSON classes from the [jsonutil](https://github.com/pwall567/jsonutil) library.
The examples below assume the existence of a `JSONObject` similar to the one created as follows:
```kotlin
    val json = JSON.parseObject("""{"list":[{"aaa":1},{"aaa":2}]}""")
```

To create a JSON Pointer from a string:
```kotlin
    val pointer = JSONPointer("/list/0")
```
This pointer selects the property named "list" from the object, and then selects the first itemfrom the array.

To use the pointer, all of the following three forms are equivalent:
```kotlin
    val result = pointer.eval(json)
```
```kotlin
    val result = json locate pointer
```
```kotlin
    val result = json locate "/list/0"
```
 In each case, `result` will contain the `JSONObject` representing the nested object:
 ```json
{"aaa":1}
```
 
More information to follow.

## Dependency Specification

The latest version of the library is 0.2, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-kotlin-pointer</artifactId>
      <version>0.2</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-kotlin-pointer:0.2'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-kotlin-pointer:0.2")
```

Peter Wall

2020-07-23
