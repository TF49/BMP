#!/usr/bin/env node
import sharp from 'sharp';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(__dirname, '..', 'static', 'tabbar');
const SIZE = 81;

function svgCircle(color) {
  return `<svg width="${SIZE}" height="${SIZE}" xmlns="http://www.w3.org/2000/svg">
  <circle cx="${SIZE/2}" cy="${SIZE/2}" r="28" fill="${color}"/>
</svg>`;
}

async function createIcon(filepath, colorHex) {
  const buf = Buffer.from(svgCircle(colorHex));
  await sharp(buf)
    .resize(SIZE, SIZE)
    .png()
    .toFile(filepath);
}

const tabs = ['home', 'venue', 'course', 'tournament', 'profile'];
if (!fs.existsSync(OUT)) fs.mkdirSync(OUT, { recursive: true });

for (const name of tabs) {
  await createIcon(path.join(OUT, `tab-${name}.png`), '#475569');
  await createIcon(path.join(OUT, `tab-${name}-active.png`), '#3cc51f');
}
console.log('Tab icons generated in static/tabbar/');
