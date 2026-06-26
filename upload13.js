const { execSync } = require('child_process');
try {
    console.log('Uploading to file.io...');
    const result = execSync('curl -m 30 -L -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://file.io').toString();
    console.log("Output: ", result);
} catch (e) {
    console.error(e.message);
}
