import type { ReactElement } from 'react';
import {
    ReactAdapterElement,
    type RenderHooks
} from 'Frontend/generated/flow/ReactAdapter';

import { UptimeClock } from './uptime-clock'

class UptimeClockElement extends ReactAdapterElement {
    protected override render(
        hooks: RenderHooks
    ): ReactElement | null {
        const [startTime] = hooks.useState<number>('startTime');
        const [endTime] = hooks.useState<number>('endTime');
        const [timerState] = hooks.useState<string>('timerState');
        const [serverTime] = hooks.useState<number>('serverTime');

        const serverTimeDelta = Date.now() - serverTime;

        return (
            <UptimeClock
                startTime={startTime}
                endTime={endTime}
                timerState={timerState}
                serverTimeDelta={serverTimeDelta}
            />
        );
    }
}

customElements.define('uptime-clock', UptimeClockElement);