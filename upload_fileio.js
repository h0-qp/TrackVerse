const https = require('https');
const fs = require('fs');

const FormData = require('form-data'); // Maybe not available? Let's use fetch if it's node 18+

async function upload() {
  try {
    const fileStream = fs.createReadStream('app/build/outputs/apk/debug/app-debug.apk');
    
    // We will use standard Fetch API available in Node 18+
    const formData = new FormData();
    formData.append('file', fileStream);

    console.log("Uploading to file.io...");
    const res = await fetch('https://file.io', {
      method: 'POST',
      body: formData
    });
    const json = await res.json();
    console.log(json);
    if(json.success) {
      console.log("Upload Success: " + json.link);
    }
  } catch(e) {
    console.error(e);
  }
}
upload();
