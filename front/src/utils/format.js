export function formatDate(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/**
 * Format budget value and append unit "万".
 * - If value is null/empty -> return "—"
 * - If value already contains non-numeric characters (e.g. "万", "元", "￥"), return trimmed value.
 * - Otherwise append "万" to the numeric value.
 */
export function formatBudget(value) {
  if (value === null || value === undefined || value === '') return '—'
  const s = String(value).trim()
  // if contains any non-digit (except dot and comma), assume it already has unit/formatting
  if (/[^\d.,\s]/.test(s)) return s
  // remove commas and normalize
  const cleaned = s.replace(/,/g, '')
  if (!cleaned) return '—'
  return `${cleaned}万`
}
