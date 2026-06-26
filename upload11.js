const { execSync } = require('child_process');
try {
    console.log('Uploading to catbox.moe...');
    const output = execSync('curl -m 120 -s -F "reqtype=fileupload" -F "fileToUpload=@./app/build/outputs/apk/debug/app-debug.apk" https://catbox.moe/user/api.php');
    console.log("Output:", output.toString());
} catch (e) {
    console.error(e.message);
}
