/** Einheitlicher Page-Wrapper des Backends (siehe docs/api-design.md). */
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  sort: { property: string; direction: string }[];
}
