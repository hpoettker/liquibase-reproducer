# A liquibase bug reproducer

This project is a reproducer for the Liquibase bug 
[liquibase/liquibase#4460](https://github.com/liquibase/liquibase/issues/4460).

## Steps to reproduce

### Start MySQL container

```shell
docker run --rm -v ./initdb.d:/docker-entrypoint-initdb.d -e MYSQL_ROOT_PASSWORD=my-secret-pw -p 3306:3306 -d mysql:8.0.33
```

### Start application

Start the application to create the Liquibase tables and execute one migration with Liquibase 4.17.2.

```shell
./gradlew bootRun
```

The table `liquibase.DATABASECHANGELOG` will contain the following row afterward:

| ID | AUTHOR | FILENAME | MD5SUM | LIQUIBASE |
| -- | ------ | -------- | ------ | --------- |
| 0  | me     | classpath:/db/changelog/db.changelog-master.yaml | 8:1797ce334e172e6d4d40a71d1d7f4055 | 4.17.2 |

### Upgrade Liquibase to 4.23

Change the file [build.gradle.kts](build.gradle.kts) to contain the upgraded dependency:

```kotlin
dependencies {
    ...
    // implementation("org.liquibase:liquibase-core")
    implementation("org.liquibase:liquibase-core:4.23.0")
    ...
}
```

### Start the application again

Start the application with upgraded Liquibase:

```shell
./gradlew bootRun
```

The application will log

```text
Upgrading checksum for Changeset db/changelog/db.changelog-master.yaml::0::me from 8:1797ce334e172e6d4d40a71d1d7f4055 to 9:bfc81a0300ec33c6f421cf7ef6e09a41.
INFO --- [main] liquibase.util     : UPDATE SUMMARY
INFO --- [main] liquibase.util     : Run:                          0
INFO --- [main] liquibase.util     : Previously run:               1
INFO --- [main] liquibase.util     : Filtered out:                 0
INFO --- [main] liquibase.util     : -------------------------------
INFO --- [main] liquibase.util     : Total change sets:            1


UPDATE SUMMARY
Run:                          0
Previously run:               1
Filtered out:                 0
-------------------------------
Total change sets:            1

INFO --- [main] liquibase.util     : Update summary generated
INFO --- [main] liquibase.command  : Update command completed successfully.
Liquibase: Update has been successful.

```

## Problem

The last step can be executed multiple time but the log stating that the check sum will be upgraded will appear
everytime. Also, the table `DATABASECHANGELOG` is not actually updated.

The problem is that Liquibase executes the SQL statement

```sql
UPDATE liquibase.DATABASECHANGELOG
SET MD5SUM = '9:bfc81a0300ec33c6f421cf7ef6e09a41'
WHERE ID = '0' 
  AND AUTHOR = 'me'
  AND FILENAME = 'db/changelog/db.changelog-master.yaml'
```

which is missing the prefix `classpath:` in front of the `FILENAME`.
