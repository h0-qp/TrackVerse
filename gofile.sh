#!/bin/bash
SERVER=$(curl -s https://api.gofile.io/servers | grep -o '"name":"[^"]*"' | head -n 1 | cut -d'"' -f4)
if [ -z "$SERVER" ]; then SERVER="store-eu-par-1"; fi
curl -m 120 -s -F "file=@app-release.apk" https://${SERVER}.gofile.io/contents/uploadfile > gofile_url_final.log
