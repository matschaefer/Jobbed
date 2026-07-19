/** Wandelt ein Date in ein ISO-Datum (YYYY-MM-DD) ohne Zeitzonenversatz. */
export function toIsoDate(value: Date | null | undefined): string | null {
  if (!value) {
    return null;
  }
  const year = value.getFullYear();
  const month = `${value.getMonth() + 1}`.padStart(2, '0');
  const day = `${value.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/** Parst ein ISO-Datum (YYYY-MM-DD) in ein lokales Date. */
export function fromIsoDate(value: string | null | undefined): Date | null {
  if (!value) {
    return null;
  }
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}
