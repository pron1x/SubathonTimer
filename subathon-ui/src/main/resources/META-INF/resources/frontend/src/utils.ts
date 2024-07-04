export function millisToTimeString(ms: number) {
    const s = Math.floor((ms / 1000) % 60);
    const m = Math.floor((ms / 1000 / 60) % 60);
    const h = Math.floor((ms / 1000 / 3600));

    return [
        h.toString(),
        (m.toString().length < 2 ? "0" : "") + m.toString(),
        (s.toString().length < 2 ? "0" : "") + s.toString()
    ].join(":");
}