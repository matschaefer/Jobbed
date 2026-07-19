import { buildMonthGrid, localDateKey } from './calendar.util';

describe('calendar util', () => {
  it('builds a stable six-week Monday-first grid', () => {
    const days = buildMonthGrid(new Date(2026, 6, 1), new Date(2026, 6, 19));
    expect(days.length).toBe(42);
    expect(days[0].date.getDay()).toBe(1);
    expect(days.find((d) => d.today)?.key).toBe('2026-07-19');
  });
  it('formats local keys without UTC date shifts', () => expect(localDateKey(new Date(2026, 0, 2))).toBe('2026-01-02'));
});
