const { execSync } = require('child_process');
try {
    console.log('Uploading to transfer.sh...');
    const output = execSync('curl -m 50 --upload-file ./app/build/outputs/apk/debug/app-debug.apk https://transfer.sh/app-debug.apk');
    console.log("URL:", output.toString());
} catch (e) {
    console.error(e.message);
}
