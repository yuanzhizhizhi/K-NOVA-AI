import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const inputRoot = process.argv[2];
const outputRoot = process.argv[3] || path.join(inputRoot, 'ai-chunks');
if (!inputRoot) throw new Error('Usage: node chunk-process-docs.mjs <docs-dir> [output-dir]');

const files = [
  path.join(inputRoot, 'stockin', 'inbound-process.md'),
  path.join(inputRoot, 'stockout', 'outbound-process.md'),
];
const MAX = 1800;
const MIN = 350;

function splitBlocks(body) {
  // Mermaid/代码围栏和 Markdown 表格必须作为整体保留，避免流程边关系被截断。
  const lines = body.split(/\r?\n/);
  const blocks = [];
  let buffer = [], fence = false, fenceMark = '';
  const flush = () => { if (buffer.join('\n').trim()) blocks.push(buffer.join('\n').trim()); buffer = []; };
  for (const line of lines) {
    const marker = line.match(/^\s*(```|~~~)/)?.[1];
    if (marker) {
      if (!fence) { flush(); fence = true; fenceMark = marker; buffer.push(line); }
      else { buffer.push(line); if (marker === fenceMark) { flush(); fence = false; fenceMark = ''; } }
      continue;
    }
    if (fence) { buffer.push(line); continue; }
    if (!line.trim()) flush(); else buffer.push(line);
  }
  flush();
  return blocks;
}

function blockType(text) {
  if (/^(```|~~~)mermaid/m.test(text)) return 'flowchart';
  if (/^(```|~~~)/m.test(text)) return 'code';
  if (/^\|.+\|/m.test(text) && /\|\s*:?-{3,}/m.test(text)) return 'table';
  if (/^(\d+\.|[-*])\s/m.test(text)) return 'list';
  return 'text';
}

function pack(section) {
  const blocks = splitBlocks(section.body);
  const result = [];
  let current = '';
  for (const block of blocks) {
    const type = blockType(block);
    // 图、表、代码块独立成片，确保图节点和表头上下文完整。
    if (type !== 'text' && type !== 'list') {
      if (current) result.push({ type: 'text', text: current });
      current = '';
      result.push({ type, text: block });
      continue;
    }
    if (current && current.length + block.length + 2 > MAX) {
      result.push({ type: 'text', text: current });
      current = block;
    } else current += (current ? '\n\n' : '') + block;
  }
  if (current) result.push({ type: 'text', text: current });
  // 过长纯文本按句号切分；不使用硬字符截断。
  return result.flatMap(item => {
    if (item.type !== 'text' || item.text.length <= MAX) return [item];
    const sentences = item.text.split(/(?<=[。！？；])\s*/);
    const parts = []; let value = '';
    for (const sentence of sentences) {
      // 没有中文标点的长索引/列表再按行切，最后才按字符安全截断。
      const units = sentence.length <= MAX ? [sentence] : sentence.split(/(?<=\n)/).flatMap(line => {
        if (line.length <= MAX) return [line];
        const values = [];
        for (let index = 0; index < line.length; index += MAX) values.push(line.slice(index, index + MAX));
        return values;
      });
      for (const unit of units) {
        if (value.length >= MIN && value.length + unit.length > MAX) { parts.push({type:'text', text:value.trim()}); value = unit; }
        else value += unit;
      }
    }
    if (value) parts.push({type:'text', text:value});
    return parts;
  });
}

function parseMarkdown(content) {
  const lines = content.split(/\r?\n/), sections = [];
  const hierarchy = [], intro = [];
  let current = null, fenced = false;
  for (const line of lines) {
    if (/^\s*(```|~~~)/.test(line)) fenced = !fenced;
    const heading = !fenced && line.match(/^(#{1,6})\s+(.+)$/);
    if (heading) {
      if (current) sections.push(current); else if (intro.length) sections.push({path:['文档说明'], body:intro.join('\n')});
      const level = heading[1].length;
      hierarchy.length = level - 1;
      hierarchy[level - 1] = heading[2].trim();
      current = { path: hierarchy.filter(Boolean), body: '' };
    } else if (current) current.body += line + '\n'; else intro.push(line);
  }
  if (current) sections.push(current);
  return sections;
}

fs.mkdirSync(outputRoot, { recursive: true });
const all = [];
for (const file of files) {
  if (!fs.existsSync(file)) throw new Error(`Missing document: ${file}`);
  const direction = file.includes('stockin') ? 'inbound' : 'outbound';
  const directionName = direction === 'inbound' ? '入库流程' : '出库流程';
  const sections = parseMarkdown(fs.readFileSync(file, 'utf8'));
  let sequence = 0;
  for (const section of sections) {
    for (const part of pack(section)) {
      const titlePath = section.path.join(' > ');
      const content = `文档：${directionName}\n章节：${titlePath}\n\n${part.text}`.trim();
      const id = crypto.createHash('sha256').update(`${direction}:${sequence}:${content}`).digest('hex').slice(0, 24);
      all.push({
        id, document: path.basename(file), direction, directionName,
        sectionPath: section.path, chunkType: part.type, sequence: sequence++,
        charCount: content.length, content,
        metadata: { sourcePath: file, knowledgeDomain: 'GWMS', codeBaseline: 'gwall-halo-dev-v5.0.36.2' }
      });
    }
  }
}

fs.writeFileSync(path.join(outputRoot, 'process-chunks.jsonl'), all.map(x => JSON.stringify(x)).join('\n') + '\n', 'utf8');
fs.writeFileSync(path.join(outputRoot, 'process-chunks.json'), JSON.stringify(all, null, 2), 'utf8');
const stats = Object.groupBy(all, x => x.direction);
const manifest = {
  generatedAt: new Date().toISOString(), strategy: 'markdown-heading-semantic-v1',
  maxTextChars: MAX, totalChunks: all.length,
  documents: Object.entries(stats).map(([direction, chunks]) => ({direction, chunks: chunks.length, flowcharts: chunks.filter(x=>x.chunkType==='flowchart').length, tables: chunks.filter(x=>x.chunkType==='table').length}))
};
fs.writeFileSync(path.join(outputRoot, 'manifest.json'), JSON.stringify(manifest, null, 2), 'utf8');
console.log(JSON.stringify(manifest, null, 2));
