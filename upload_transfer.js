const fs = require('fs');
const http = require('https');
const path = './app/build/outputs/apk/debug/app-debug.apk';
const FormData = require('form-data');

const form = new FormData();
form.append('f', fs.createReadStream(path));

const options = {
    method: 'POST',
    hostname: 'oshi.at',
    path: '/',
    headers: form.getHeaders()
};

const req = http.request(options, (res) => {
    let responseData = '';
    res.on('data', (chunk) => {
        responseData += chunk;
    });
    res.on('end', () => {
        console.log('Upload complete! URL:');
        console.log(responseData);
    });
});

req.on('error', (e) => {
    console.error('Upload failed:', e);
});

form.pipe(req);
