import { writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

const apiUrl = (process.env.NG_APP_API_URL || 'http://localhost:8080/api').replace(/\/+$/, '');
const outputPath = fileURLToPath(new URL('../src/app/api-url.ts', import.meta.url));

await writeFile(outputPath, `/** URL de API establecida durante el build. */\nexport const API_URL = ${JSON.stringify(apiUrl)};\n`);
