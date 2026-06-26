const { execSync } = require('child_process');
try {
    console.log('Uploading to 0x0.st...');
    const output = execSync('curl -m 50 -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://0x0.st');
    console.log("Output:", output.toString());
} catch (e) {
    console.error(e.message);
}
