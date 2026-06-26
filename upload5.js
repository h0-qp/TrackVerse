const { execSync } = require('child_process');
try {
    console.log('Uploading to bashupload.com...');
    const output = execSync('curl -m 50 https://bashupload.com/ -T ./app/build/outputs/apk/debug/app-debug.apk');
    console.log("Output:", output.toString());
} catch (e) {
    console.error(e.message);
}
