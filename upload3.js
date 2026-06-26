const { execSync } = require('child_process');
try {
    const serverName = "store-eu-par-5";
    console.log('Trying server:', serverName);
    const output = execSync(`curl -m 60 -s -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://${serverName}.gofile.io/contents/uploadfile`);
    console.log("URL:", output.toString());
} catch (e) {
    console.error(e.message);
}
