# Firebase App Distribution

Mengirim APK release (ditandatangani dengan `keystore/kerabite-release.jks`) ke penguji lewat
Firebase App Distribution, project `kerabites-975e0`.

## Sekali saja

1. Firebase Console → App Distribution → Get started (aktifkan untuk aplikasi Android `com.test.cobacompose`).
2. Login Firebase CLI (dipakai plugin Gradle untuk autentikasi): `npm install -g firebase-tools`, lalu `firebase login`.
3. Isi penguji (salah satu atau keduanya):
   - `testers.txt`: email penguji, dipisah koma atau satu per baris.
   - `groups.txt`: alias grup yang sudah dibuat di Console → App Distribution → Testers & Groups.
   File ini hanya berisi email/alias (tanpa komentar). Kalau keduanya kosong, APK tetap diunggah
   dan penguji bisa ditambahkan dari Console.

## Setiap rilis

1. Naikkan `versionCode` (dan `versionName`) di `app/build.gradle.kts`.
2. Tulis catatan rilis di `release-notes.txt`.
3. Dari root repo (PowerShell: set `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"` dulu):

   ```
   ./gradlew assembleRelease appDistributionUploadRelease
   ```

Upload ditolak kalau `keystore.properties` tidak ada, karena APK tanpa tanda tangan tidak bisa dipasang.
Google Sign-In di APK ini hanya jalan kalau SHA-1 kunci release terdaftar di Firebase.

Untuk CI tanpa `firebase login`, pakai service account dengan peran "Firebase App Distribution Admin"
dan set env `GOOGLE_APPLICATION_CREDENTIALS` ke file JSON-nya. Jangan simpan file itu di repo.
