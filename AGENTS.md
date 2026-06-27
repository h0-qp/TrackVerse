When the user wants to download the APK, or after major changes, build the app using `gradle clean && gradle :app:assembleDebug`, then run `npx --yes node upload.js` (which uses curl to upload to gofile.io) to provide a direct download link for the user. Do not tell the user to use the export button if they specifically ask for an APK link.

Whenever you build the app using gradle or compile_applet, always run `npx --yes node copy_apk.js` afterwards to ensure the latest `app-debug.apk` is available in the root directory for the user.
