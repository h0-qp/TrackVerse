const { execSync } = require('child_process');
try {
    console.log('Uploading to temp.sh...');
    const output = execSync('curl -m 50 -T ./app/build/outputs/apk/debug/app-debug.apk https://temp.sh');
    console.log("Output:", output.toString());
} catch (e) {
    console.error(e.message);
}
