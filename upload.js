const { execSync } = require('child_process');
try {
    console.log('Fetching server...');
    const serversRes = execSync('curl -s https://api.gofile.io/servers');
    const servers = JSON.parse(serversRes.toString());
    const serverName = servers.data.servers[0].name;
    console.log('Got server:', serverName, 'Uploading...');
    const output = execSync(`curl -s -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://${serverName}.gofile.io/contents/uploadfile`);
    console.log("URL:", output.toString());
} catch (e) {
    console.error(e);
}
