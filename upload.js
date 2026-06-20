const { execSync } = require('child_process');
try {
    console.log('Fetching server...');
    const serversRes = execSync('curl -s https://api.gofile.io/servers');
    const servers = JSON.parse(serversRes.toString());
    
    // Pick a random server to avoid hanging servers
    const serverList = servers.data.servers;
    const serverName = serverList[Math.floor(Math.random() * serverList.length)].name;
    
    console.log('Got server:', serverName, 'Uploading...');
    // Add -m 45 for max time 45 seconds to avoid silent timeouts
    const output = execSync(`curl -m 45 -s -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://${serverName}.gofile.io/contents/uploadfile`);
    console.log("URL:", output.toString());
} catch (e) {
    console.error(e);
}
