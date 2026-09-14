import type { ReactElement } from 'react';
import {
    ReactAdapterElement,
    type RenderHooks
} from 'Frontend/generated/flow/ReactAdapter';

import { SubathonTimer } from './subathon-timer'

class SubathonTimerElement extends ReactAdapterElement {
    protected override render(
        hooks: RenderHooks
    ): ReactElement | null {
        const [endTime] = hooks.useState<number>('endTime');
        const [lastUpdateTime] = hooks.useState<number>('lastUpdateTime');
        const [timerState] = hooks.useState<string>('timerState');
        const [serverTime] = hooks.useState<number>('serverTime');

        const serverTimeDelta = Date.now() - serverTime;

        return (
            <SubathonTimer
                endTime={endTime}
                lastUpdateTime={lastUpdateTime}
                timerState={timerState}
                serverTimeDelta={serverTimeDelta}
            />
        );
    }
}

customElements.define('subathon-timer', SubathonTimerElement);