# LifeLog AI — Android starter project

## Included in this prototype
- Voice capture via Android's speech-recognition intent, resulting in editable text
- Quick text entry
- Timestamped timeline
- Explicit "Analyze / export" action that opens Android's share chooser with your log and an analysis prompt

## Important prototype limitations
- Entries are currently kept in memory: they disappear if the app process is closed. Add Room persistence before daily use.
- Speech recognition availability and offline behavior depend on your Android speech service/settings.
- AI is not embedded or called automatically. The analysis button deliberately shares only after you tap it.
- This is source code, not a signed/installable APK.

## Build
Open this folder in Android Studio (Giraffe or newer), allow Gradle sync, then Run on an Android device.
For a production build, add Room DAO/database and migrations, a secure AI backend (never embed a private API key in the APK), backup/export, and proper error/permission handling.
