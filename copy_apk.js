const fs = require('fs');
try {
    fs.copyFileSync('./app/build/outputs/apk/debug/app-debug.apk', './app-debug.apk');
    console.log('Successfully copied app-debug.apk to root directory.');
} catch (e) {
    console.error('Failed to copy APK:', e.message);
}
