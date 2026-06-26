const { execSync } = require('child_process');
try {
    console.log('Fetching servers from Gofile...');
    const serverOut = execSync('curl -m 15 -s https://api.gofile.io/servers').toString();
    const serverData = JSON.parse(serverOut);
    const servers = serverData.data.servers.map(s => s.name);
    
    let uploaded = false;
    for (const server of servers) {
        try {
            console.log('Trying gofile', server);
            const output = execSync(`curl -m 25 -s -F "file=@./app/build/outputs/apk/debug/app-debug.apk" https://${server}.gofile.io/contents/uploadfile`);
            const resStr = output.toString();
            console.log("Output:", resStr);
            if (resStr.includes('"status":"ok"')) {
                uploaded = true;
                break;
            }
        } catch (e) {
            console.log(server, "failed");
        }
    }
    if (!uploaded) {
        console.log("Failed all gofile servers. Trying transfer.sh (which takes some time)...");
        try {
            const res = execSync('curl -m 180 --upload-file ./app/build/outputs/apk/debug/app-debug.apk https://transfer.sh/app-debug.apk').toString();
            console.log(res);
        } catch(e) {
            console.log("transfer.sh failed.");
        }
    }
} catch (e) {
    console.error(e.message);
}
