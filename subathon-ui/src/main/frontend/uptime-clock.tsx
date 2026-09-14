import { useEffect, useState } from 'react';

import {
    millisToTimeString,
    STATE_TICKING,
    STATE_PAUSED,
    STATE_ENDED
} from './utils'


interface UptimeClockProps {
    startTime?: number;
    endTime?: number
    timerState?: string;
    serverTimeDelta: number
}

export function UptimeClock({ startTime, endTime, timerState, serverTimeDelta }: UptimeClockProps) {

    const [now, setNow] = useState(() => Date.now());

    useEffect(() => {
        const interval = window.setInterval(() => {
            setNow(Date.now());
        }, 100);

        return () => window.clearInterval(interval);
    })

    const uptime = calculateUptime({
        startTime, endTime, timerState, now, serverTimeDelta
    });

    const uptimeString = millisToTimeString(uptime);

    return <span>{uptimeString}</span>;
}

function calculateUptime({
    startTime,
    endTime,
    timerState,
    now,
    serverTimeDelta
}: {
    startTime?: number,
    endTime?: number,
    timerState?: string,
    now: number,
    serverTimeDelta: number
}): number {

    if (!startTime) {
        return 0;
    }

    if (timerState === STATE_TICKING || timerState === STATE_PAUSED) {
        now = now - serverTimeDelta;
        return now - startTime;
    }
    if (endTime && timerState === STATE_ENDED) {
        return endTime - startTime;
    }
    return 0;
}