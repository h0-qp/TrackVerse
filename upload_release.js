const { execSync } = require('child_process');
const fs = require('fs');

try {
    // Copy APK to root directory for easy access
    try {
        fs.copyFileSync('./app/build/outputs/apk/release/app-release.apk', './app-release.apk');
        console.log('Copied app-release.apk to root directory.');
    } catch (copyErr) {
        console.error('Failed to copy APK:', copyErr.message);
    }

    console.log('Fetching server...');
    const serversRes = execSync('curl -s https://api.gofile.io/servers');
    const servers = JSON.parse(serversRes.toString());
    
    const serverList = servers.data.servers;
    
    for (const server of serverList) {
        const serverName = server.name;
        console.log('Trying server:', serverName, 'Uploading...');
        try {
            const output = execSync(`curl -m 30 -s -F "file=@./app/build/outputs/apk/release/app-release.apk" https://${serverName}.gofile.io/contents/uploadfile`);
            const resStr = output.toString();
            console.log("URL:", resStr);
            if (resStr.includes('"status":"ok"')) {
                break;
            }
        } catch (err) {
            console.log('Failed on', serverName, 'trying next...');
        }
    }
} catch (e) {
    console.error(e);
}
