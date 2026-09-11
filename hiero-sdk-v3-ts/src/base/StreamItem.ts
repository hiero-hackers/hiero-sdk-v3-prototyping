export type StreamItem<T, E = Error> =
  | { ok: true; value: T }
  | { ok: false; error: E };

export function isStreamItem(item: any): item is StreamItem<unknown, unknown> {
  return (
    typeof item === 'object' &&
    item !== null &&
    'ok' in item &&
    typeof item.ok === 'boolean'
  );
}
