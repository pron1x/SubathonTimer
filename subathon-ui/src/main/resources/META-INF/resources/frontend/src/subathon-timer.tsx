import { useEffect, useState } from 'react';

import {
    millisToTimeString,
    STATE_INITIALIZED,
    STATE_TICKING,
    STATE_PAUSED,
    STATE_ENDED
} from './utils';

interface SubathonTimerProps {
    endTime?: number;
    lastUpdateTime?: number;
    timerState?: string;
    serverTimeDelta?: number;
}

export function SubathonTimer({ endTime, lastUpdateTime, timerState, serverTimeDelta = 0 }: SubathonTimerProps) {
    const [now, setNow] = useState(() => Date.now());

    useEffect(() => {
        const interval = window.setInterval(() => {
            setNow(Date.now());
        }, 100);

        return () => window.clearInterval(interval);
    }, []);

    const timeLeft = calculateTimeLeft({
        endTime,
        lastUpdateTime,
        timerState,
        now,
        serverTimeDelta
    });

    const timeLeftString = createTimeLeftString(timeLeft);

    return <span>{timeLeftString}</span>;
}

function calculateTimeLeft({
    endTime,
    lastUpdateTime,
    timerState,
    now,
    serverTimeDelta
}: {
    endTime? :number;
    lastUpdateTime? :number;
    timerState? :string;
    now: number;
    serverTimeDelta: number;
}): number {
    if (!endTime) {
        return 0;
    }

    if (timerState === STATE_ENDED) {
        return Number.NEGATIVE_INFINITY;
    }

    if (timerState === STATE_INITIALIZED || timerState === STATE_PAUSED) {
        return endTime - (lastUpdateTime ?? 0);
    }

    if (timerState === STATE_TICKING) {
        const serverNow = now - serverTimeDelta;
        return endTime - serverNow;
    }

    return 0;
}

function createTimeLeftString(milliseconds: number): string {
    if (milliseconds === Number.NEGATIVE_INFINITY) {
        return 'ENDED';
    }

    return millisToTimeString(Math.max(0, milliseconds));
}