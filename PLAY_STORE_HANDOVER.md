# Kerabite: Play Store and Service Handover

This checklist separates work that must be done by the school/student account owners from work that lives in the Android repository.

## Decisions required before creating the Play Console app

- Obtain written school approval to use the school name, branding, domain namespace, and application ID.
- Confirm the permanent application ID. Recommended: `id.sch.man2kotamalang.kerabite`.
- Choose the legal publisher shown in Google Play and the privacy-policy controller.
- Confirm the official support email, privacy email, postal address, and monitored phone/WhatsApp number.
- Decide which organization-owned Google account will be the primary owner. Do not leave sole ownership with a graduating student.

## Student handover

The student should invite at least two long-term school-controlled Google accounts as owners/admins where supported, then verify access before removing anyone:

- GitHub repository or GitHub organization;
- Firebase project and linked Google Cloud project;
- Google Play Console developer account/app;
- Maps Platform billing account and API credentials;
- any Firebase App Distribution groups;
- design/source assets and proof of licenses; and
- the release/upload keystore, alias, passwords, recovery notes, and encrypted backups.

Never send passwords or keystores through GitHub. Use the school's approved password manager or another encrypted handover channel.

## Firebase and Google Cloud

- Add the final Android application ID to Firebase and download a new `google-services.json`.
- Enable Email/Password and Google authentication providers.
- Add SHA-1 and SHA-256 for debug, upload, and (after enrollment) Play App Signing certificates.
- Deploy `firestore.rules` and test that users cannot read another user's documents.
- Enable Firebase App Check for supported products after testing enforcement.
- Use a Firebase-only API key for Firebase services.
- Use a separate Android-restricted key for Maps/Places, restricted by application ID and SHA-1.
- Set API quotas and billing alerts.
- Verify the Data Retention setting for Analytics and document it in the privacy disclosures.

## Public policy pages

- Replace every bracketed TODO in `docs/privacy-policy.md` and `docs/account-deletion.md`.
- In GitHub: Settings > Pages > Deploy from branch > `main` and `/docs`.
- Confirm both public URLs load without authentication:
  - `https://lintangtedjaratri-sudo.github.io/Kerabite/privacy-policy.html`
  - `https://lintangtedjaratri-sudo.github.io/Kerabite/account-deletion.html`
- Re-check that the app opens both links.

## Play Console

- Create the app only after the permanent application ID is final.
- Enroll in Play App Signing and securely retain the upload key.
- Declare the app as an app (not a game), its price, and its default language.
- Complete App access with a durable reviewer account and precise login instructions.
- Complete Ads, Target audience, Content rating, Data safety, Account deletion, and Privacy policy declarations.
- Target audience: high-school students; choose only the age bands that truthfully match the intended students. Get policy advice before including under-13 users.
- Prepare the app icon, 1024x500 feature graphic, at least two phone screenshots, short description, and full description.
- Upload the signed AAB to Internal testing first, review the pre-launch report, then use Closed testing.

## Release verification

```bash
./gradlew testDebugUnitTest
./gradlew lintRelease
./gradlew bundleRelease
```

- Test email registration/login, Google login, reset password, maps, camera/gallery, every challenge, offline/online sync, account deletion, privacy links, and RTL Arabic layout.
- Test on at least one physical low-end phone and current Android versions.
- Increment `versionCode` for every uploaded build.

