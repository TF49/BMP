/**
 * 极简、安全的 Markdown 渲染器。
 *
 * 支持经营分析与客服 Agent 常用输出：
 * - `## 标题` / `### 标题`
 * - `**加粗**`
 * - `` `行内代码` ``
 * - `- 列表项`
 * - `| 列1 | 列2 |` Markdown 表格
 * - 空行分段与换行
 *
 * 安全策略：先对整段文本做 HTML 转义，再在转义后的安全文本上做结构与行内标记替换，
 * 因此不会渲染任何原始 HTML 或脚本，避免 v-html 带来的 XSS 风险。
 */

/** 转义 HTML 特殊字符。 */
function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/** 处理行内标记：加粗与行内代码。输入必须已完成 HTML 转义。 */
function renderInline(escapedText) {
  return escapedText
    .replace(/`([^`]+)`/g, '<code class="agent-md-code">$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

/** 拆分表格行单元格并做 HTML 转义与行内渲染。 */
function parseTableCells(rowStr) {
  let s = rowStr.trim()
  if (s.startsWith('|')) s = s.slice(1)
  if (s.endsWith('|')) s = s.slice(0, -1)
  return s.split('|').map((c) => renderInline(escapeHtml(c.trim())))
}

/** 判断是否为 Markdown 表格分隔行，如 `|---|---|` 或 `| :--- | ---: |` */
function isTableSeparator(line) {
  const trimmed = line.trim()
  return /^\|?\s*:?-+:?\s*(\|?\s*:?-+:?\s*)+\|?$/.test(trimmed)
}

/**
 * 将 Markdown 文本渲染为安全的 HTML 字符串。
 * @param {string} markdown 原始 Markdown 文本
 * @returns {string} 可用于 v-html 的安全 HTML
 */
export function renderAgentMarkdown(markdown) {
  if (!markdown || typeof markdown !== 'string') return ''

  const lines = markdown.replace(/\r\n/g, '\n').split('\n')
  const html = []
  let listOpen = false
  let tableOpen = false
  let inTbody = false

  const closeList = () => {
    if (listOpen) {
      html.push('</ul>')
      listOpen = false
    }
  }

  const closeTable = () => {
    if (tableOpen) {
      if (inTbody) {
        html.push('</tbody>')
        inTbody = false
      }
      html.push('</table></div>')
      tableOpen = false
    }
  }

  const closeBlocks = () => {
    closeList()
    closeTable()
  }

  for (const rawLine of lines) {
    const line = rawLine.trimEnd()
    const trimmed = line.trim()
    const escaped = escapeHtml(trimmed)

    // 空行：结束列表/表格并作为分段
    if (!trimmed) {
      closeBlocks()
      continue
    }

    // Markdown 表格处理
    if (trimmed.startsWith('|') || (tableOpen && trimmed.includes('|'))) {
      closeList()
      if (isTableSeparator(trimmed)) {
        if (tableOpen && !inTbody) {
          html.push('</thead><tbody>')
          inTbody = true
        }
        continue
      }

      const cells = parseTableCells(trimmed)
      if (!tableOpen) {
        tableOpen = true
        inTbody = false
        html.push('<div class="agent-md-table-wrap"><table class="agent-md-table"><thead><tr>')
        cells.forEach((c) => html.push(`<th>${c}</th>`))
        html.push('</tr>')
      } else if (inTbody) {
        html.push('<tr>')
        cells.forEach((c) => html.push(`<td>${c}</td>`))
        html.push('</tr>')
      } else {
        // 多表头情况防御
        html.push('<tr>')
        cells.forEach((c) => html.push(`<th>${c}</th>`))
        html.push('</tr>')
      }
      continue
    }

    // 三级标题
    if (trimmed.startsWith('### ')) {
      closeBlocks()
      html.push(`<h4 class="agent-md-h4">${renderInline(escapeHtml(trimmed.slice(4)))}</h4>`)
      continue
    }

    // 二级标题
    if (trimmed.startsWith('## ')) {
      closeBlocks()
      html.push(`<h3 class="agent-md-h3">${renderInline(escapeHtml(trimmed.slice(3)))}</h3>`)
      continue
    }

    // 无序列表项
    if (trimmed.startsWith('- ')) {
      closeTable()
      if (!listOpen) {
        html.push('<ul class="agent-md-ul">')
        listOpen = true
      }
      html.push(`<li>${renderInline(escapeHtml(trimmed.slice(2)))}</li>`)
      continue
    }

    // 普通段落
    closeBlocks()
    html.push(`<p class="agent-md-p">${renderInline(escaped)}</p>`)
  }

  closeBlocks()
  return html.join('')
}

export default renderAgentMarkdown

