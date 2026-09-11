# Marketplace Release Guide

## 1. Local checks

Use JDK 25 and Gradle 9.0 or newer:

```bash
gradle clean
gradle verifyPluginProjectConfiguration
gradle buildPlugin
gradle verifyPlugin
```

Install the ZIP from `build/distributions/` in a clean IntelliJ IDEA 2026.2 instance and test both Import and Submit.

## 2. Public source repository

Create a public GitHub repository, push this source tree, and update the Marketplace form with:

- Source repository URL
- Issue tracker URL
- Privacy-policy URL (`PRIVACY.md`)

## 3. Vendor profile

In JetBrains Marketplace, create/complete the **Taha Cheji** vendor profile with a valid website/profile URL, support email, and trader-status declaration.

## 4. Signing (recommended)

The Gradle configuration reads signing secrets from:

- `CERTIFICATE_CHAIN`
- `PRIVATE_KEY`
- `PRIVATE_KEY_PASSWORD`

Never commit these values.

## 5. Publishing

Create a Marketplace personal token and export it as:

```bash
export PUBLISH_TOKEN='...'
```

Then run:

```bash
gradle publishPlugin
```

You can also upload the `build/distributions/*.zip` manually through the Marketplace website.

## 6. First-release listing

Use the text in `MARKETPLACE_LISTING.md`. Choose GPL-3.0-or-later and provide the public source-code URL.

## GitHub Actions

The included workflows build/verify every push and provide a manual Marketplace publishing workflow. Add Marketplace/signing secrets to the repository before using the publish workflow.
