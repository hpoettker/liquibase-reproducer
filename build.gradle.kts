plugins {
    java
    id("org.springframework.boot") version "3.0.8"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.liquibase:liquibase-core")
    // implementation("org.liquibase:liquibase-core:4.23.0")
    runtimeOnly("com.mysql:mysql-connector-j")
}
