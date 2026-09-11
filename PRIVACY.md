# Privacy Policy

**Effective:** September 11, 2026

UMD CS Submit Client does not include analytics, advertising, telemetry, or maintainer-operated servers.

When you explicitly choose **Submit This Project**, the plugin reads the course-provided `.submit` configuration, creates a temporary archive of the project's `src` directory, and sends that archive and the submission fields required by the course to the submit-server URL specified in `.submit`. For CAS/browser authentication, the plugin opens the course submit-server page and uses the one-time verification information that you paste into the dialog. If a course is configured for legacy LDAP authentication, the entered credentials are used only for the authentication request and are not intentionally persisted by the plugin.

The temporary `submit.zip` file is deleted after the submission attempt. The plugin does not send project data, credentials, verification values, or usage statistics to Taha Cheji.

Data sent to a University or course-operated submit server is subject to that service's own policies.

Questions about this policy should be directed to the maintainer through the public source repository's issue tracker once published.
