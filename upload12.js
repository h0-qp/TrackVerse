const { execSync } = require('child_process');
try {
    console.log('Uploading to tmpfiles.org...');
    const output = execSync('curl -m 120 -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://tmpfiles.org/api/v1/upload');
    console.log("Output:", output.toString());
} catch (e) {
    console.error(e.message);
}
