export interface CalendarDay { date: Date; key: string; inMonth: boolean; today: boolean; }

export function buildMonthGrid(anchor: Date, now = new Date()): CalendarDay[] {
  const first = new Date(anchor.getFullYear(), anchor.getMonth(), 1);
  const mondayOffset = (first.getDay() + 6) % 7;
  const start = new Date(first); start.setDate(first.getDate() - mondayOffset);
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start); date.setDate(start.getDate() + index);
    return { date, key: localDateKey(date), inMonth: date.getMonth() === anchor.getMonth(), today: localDateKey(date) === localDateKey(now) };
  });
}

export function localDateKey(date: Date): string {
  const y = date.getFullYear(); const m = String(date.getMonth() + 1).padStart(2, '0'); const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function toLocalInput(iso: string): string {
  const d = new Date(iso); const pad = (v: number) => String(v).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
