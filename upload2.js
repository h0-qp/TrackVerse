const { execSync } = require('child_process');

try {
    const output = execSync('curl -s -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://litterbox.catbox.moe/user/api.php?reqtype=fileupload&time=12h');
    console.log(output.toString());
} catch (e) {
    console.error('Error:', e.message);
}
