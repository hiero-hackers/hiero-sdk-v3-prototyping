export function isStreamItem(item) {
    return (typeof item === 'object' &&
        item !== null &&
        'ok' in item &&
        typeof item.ok === 'boolean');
}
