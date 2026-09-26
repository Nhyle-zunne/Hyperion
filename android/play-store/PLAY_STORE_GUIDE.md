# Google Play Console Publishing Guide for Hyperion

This comprehensive guide details the exact steps to publish **Hyperion: MARINA Reviewer** to the Google Play Store.

---

## Prerequisites
1. **Google Play Developer Account**: Register at [play.google.com/console](https://play.google.com/console) (one-time $25 USD registration fee).
2. **Android Studio**: Install the latest version of [Android Studio Ladybug or Meerkat](https://developer.android.com/studio).
3. **Java Development Kit (JDK)**: JDK 17 or JDK 21 (bundled with Android Studio).

---

## Step 1: Generate a Release Keystore
Google Play requires all Android applications to be cryptographically signed.

Open your terminal and run:
```bash
keytool -genkey -v -keystore release.keystore -alias hyperionkey -keyalg RSA -keysize 2048 -validity 10000
```
- Store the generated `release.keystore` safely! You will need it for all future application updates.
- Keep note of your **keystore password** and **alias password**.

---

## Step 2: Build the Android App Bundle (.aab)

Google Play requires the **Android App Bundle (.aab)** format for all new apps (instead of legacy APKs).

### Option A: Using Android Studio (Recommended)
1. Open Android Studio.
2. Select **File > Open...** and choose the `android/` directory from this project.
3. Wait for Gradle sync to complete.
4. In the top menu, click **Build > Generate Signed Bundle / APK...**
5. Select **Android App Bundle** and click **Next**.
6. Choose your `release.keystore`, enter your password, alias, and key password.
7. Select **release** destination folder and click **Create**.
8. Android Studio will produce `app-release.aab` in `android/app/release/`.

### Option B: Using Command Line (Terminal)
Set your keystore environment variables and run:
```bash
cd android
./gradlew bundleRelease
```
The output file will be generated at:
`android/app/build/outputs/bundle/release/app-release.aab`

---

## Step 3: Create Application in Google Play Console
1. Log in to [Google Play Console](https://play.google.com/console).
2. Click **Create app** (top-right button).
3. Fill in basic details:
   - **App name**: `Hyperion: MARINA Reviewer`
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
   - Accept the Developer Program Policies and US export laws checkboxes.
4. Click **Create app**.

---

## Step 4: Complete Store Listing
Go to **Grow > Store presence > Main store listing**:
- **App name**: `Hyperion: MARINA Reviewer`
- **Short description**: `Offline MARINA Licensure Exam Reviewer for Philippine OIC-NW & GMDSS Seafarers.`
- **Full description**: Copy text from `android/play-store/STORE_LISTING.md`.
- **App icon**: Upload `public/logo.png` (512x512 PNG, up to 1 MB).
- **Feature graphic**: Upload 1024x500 banner (can be exported from the in-app Play Store Hub).
- **Phone screenshots**: Upload at least 4 screenshots (Home, Track Select, Practice Tutor Mode, Exam Result).

---

## Step 5: Complete Policy & Declarations
Under **Policy > App content**, complete all mandatory questionnaires:
1. **Privacy Policy**: Enter the URL where you host `android/play-store/PRIVACY_POLICY.md` (e.g. GitHub Pages or your website).
2. **Ads**: Select **No, my app does not contain ads**.
3. **App access**: Select **All functionality is available without special access**.
4. **Content ratings**: Complete the IARC questionnaire:
   - Category: Educational / Reference
   - Violence, offensive language, controlled substances: All **NO**.
   - Result: Rated for Everyone (3+).
5. **Target audience and content**:
   - Target age group: **18 and over**.
   - Appeals to children: **No**.
6. **Data safety**:
   - Does your app collect or share user data? Select **No**.
   - Does your app collect device identifiers or telemetry? Select **No**.
   - All data is strictly local to user device.
7. **Financial features**: Select **My app doesn't provide any financial features**.
8. **Government apps**: Select **No** (State clearly that this is an independent study tool based on public MARINA STCW competencies).

---

## Step 6: Testing & Production Rollout
1. Go to **Testing > Internal testing**.
2. Create a new release, upload `app-release.aab`.
3. Add internal tester email addresses and share the internal testing link.
4. For personal accounts created after Nov 2023, Google requires **Closed testing with at least 20 testers opted in for 14 days** before applying for Production.
5. Once closed testing concludes, apply for **Production** access in Google Play Console!
