const { execSync } = require('child_process');
const fs = require('fs');

try {
    console.log("Setting up secrets...");
    const gemini = process.env.GEMINI_API_KEY || '';
    const tmdb = process.env.TMDB_API_KEY || '';
    fs.writeFileSync('.env', `GEMINI_API_KEY=${gemini}\nTMDB_API_KEY=${tmdb}\n`);

    console.log("Starting build...");
    execSync('gradle clean :app:assembleRelease', { stdio: 'inherit' });
    console.log("Build finished successfully!");
} catch (error) {
    console.error("Build failed:", error);
}
