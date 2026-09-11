# UMD CS Submit Client

An open-source JetBrains IDE client for importing and submitting University of Maryland computer science course projects.

Maintained by **Taha Cheji**. This project is based on Atheesh Thirumalairajan's older **UMD CS Project Integration** plugin and preserves the original GPL attribution.

## Features

- Import UMD CS starter-project ZIP files into a JetBrains IDE
- Submit projects using the current UMD CAS/browser verification workflow
- JDK-native HTTP networking (no legacy Apache HttpClient dependency)
- IntelliJ Platform 2026.2 / build 262 support
- No analytics or telemetry

## Build

Requirements:

- Gradle 9.0+
- JDK 25 for IntelliJ Platform 2026.2 development

Run:

```bash
gradle verifyPluginProjectConfiguration
gradle buildPlugin
gradle verifyPlugin
```

The installable ZIP is produced under `build/distributions/`.

## Publishing

See [`MARKETPLACE_RELEASE.md`](MARKETPLACE_RELEASE.md). Publishing secrets are read from environment variables and must never be committed.

## Privacy

See [`PRIVACY.md`](PRIVACY.md). The plugin does not collect analytics or send data to the maintainer. Project files and authentication information are sent only to the submit-server endpoint configured by the course's `.submit` file when the user explicitly submits.

## License and attribution

GPL-3.0-or-later. See [`LICENSE`](LICENSE) and [`NOTICE.md`](NOTICE.md).

This project is independent and is **not affiliated with or endorsed by the University of Maryland**.
