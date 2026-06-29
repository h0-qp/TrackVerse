const { execSync } = require('child_process');

try {
    console.log("Starting build...");
    execSync('gradle :app:assembleRelease', { stdio: 'inherit' });
    console.log("Build finished successfully!");
} catch (error) {
    console.error("Build failed:", error);
}
