# [Sqids Java](https://sqids.org/java)
[![javadoc](https://javadoc.io/badge2/org.sqids/sqids/javadoc.svg)](https://javadoc.io/doc/org.sqids/sqids)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sqids/sqids/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.sqids/sqids)


[Sqids](https://sqids.org/java) (*pronounced "squids"*) is a small library that lets you **generate unique IDs from
numbers**. It's good for link shortening, fast & URL-safe ID generation and decoding back into numbers for quicker
database lookups.

Features:

- **Encode multiple numbers** - generate short IDs from one or several non-negative numbers
- **Quick decoding** - easily decode IDs back into numbers
- **Unique IDs** - generate unique IDs by shuffling the alphabet once
- **ID padding** - provide minimum length to make IDs more uniform
- **URL safe** - auto-generated IDs do not contain common profanity
- **Randomized output** - Sequential input provides nonconsecutive IDs
- **Many implementations** - Support for [40+ programming languages](https://sqids.org/)

## 🧰 Use-cases

Good for:

- Generating IDs for public URLs (eg: link shortening)
- Generating IDs for internal systems (eg: event tracking)
- Decoding for quicker database lookups (eg: by primary keys)

Not good for:

- Sensitive data (this is not an encryption library)
- User IDs (can be decoded revealing user count)


## System Requirements
Java 8 or higher is required.


## 🚀 Getting started

Import dependency. If you are using Apache Maven, add the following dependency to your pom.xml's dependencies:

```
<dependency>
  <groupId>org.sqids</groupId>
  <artifactId>sqids</artifactId>
  <version>0.1.0</version>
</dependency>
```    

Alternatively, if you use Gradle or are on Android, add the following to your app's `build.gradle` file under dependencies:

```
implementation 'org.sqids:sqids:0.1.0'
```

## 👩‍💻 Examples

Import Sqids via:

```java
import org.sqids.Sqids;
```

Simple encode & decode:

```java
Sqids sqids=Sqids.builder().build();
String id=sqids.encode(Arrays.asList(1L,2L,3L)); // "86Rf07"
List<Long> numbers=sqids.decode(id); // [1, 2, 3]
```

> **Note**
> 🚧 Because of the algorithm's design, **multiple IDs can decode back into the same sequence of numbers**. If it's
> important to your design that IDs are canonical, you have to manually re-encode decoded numbers and check that the
> generated ID matches.

Enforce a *minimum* length for IDs:

```java
Sqids sqids=Sqids.builder()
        .minLength(10)
        .build();
String id=sqids.encode(Arrays.asList(1L,2L,3L)); // "86Rf07xd4z"
List<Long> numbers=sqids.decode(id); // [1, 2, 3]
```

Randomize IDs by providing a custom alphabet:

```java
Sqids sqids=Sqids.builder()
        .alphabet("FxnXM1kBN6cuhsAvjW3Co7l2RePyY8DwaU04Tzt9fHQrqSVKdpimLGIJOgb5ZE")
        .build();
String id=sqids.encode(Arrays.asList(1L,2L,3L)); // "B4aajs"
List<Long> numbers=sqids.decode(id); // [1, 2, 3]
```

Prevent specific words from appearing anywhere in the auto-generated IDs:

```java
Sqids sqids=Sqids.builder()
        .blockList(new HashSet<>(Arrays.asList("86Rf07")))
        .build();
String id=sqids.encode(Arrays.asList(1L,2L,3L)); // "se8ojk"
List<Long> numbers=sqids.decode(id); // [1, 2, 3]
```

## 📝 License

[MIT](LICENSE)
