const { execSync } = require('child_process');
try {
    console.log('Uploading to file.io...');
    const output = execSync('curl -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://file.io');
    console.log("Output:", output.toString());
} catch (e) {
    console.error(e.message);
}
