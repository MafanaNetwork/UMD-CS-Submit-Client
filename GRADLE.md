# Gradle setup

This source package targets the IntelliJ Platform Gradle Plugin 2.18.1, which requires Gradle 9.0+.

If you want a Gradle Wrapper in the repository, run once on your machine:

```bash
gradle wrapper --gradle-version 9.7.0
```

Commit `gradlew`, `gradlew.bat`, and the `gradle/wrapper/` directory. The included GitHub Actions workflows do not require the wrapper because they install Gradle 9.7.0 explicitly.
