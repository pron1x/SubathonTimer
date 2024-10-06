import {millisToTimeString, STATE_TICKING, STATE_PAUSED, STATE_ENDED} from "./utils";

import {html, LitElement} from 'lit';
import {customElement, state} from 'lit/decorators.js';

@customElement('uptime-clock')
class UptimeClock extends LitElement {
    _startTimestamp: number;
    _endTimestamp: number;
    _timerState: string;

    @state()
    _uptimeString: string;

    _clockInterval: number;

    connectedCallback(): void {
        super.connectedCallback();
        this._clockInterval = setInterval(() => {
            if(this._startTimestamp && (this._timerState === STATE_TICKING || this._timerState === STATE_PAUSED)) {
                this._uptimeString = millisToTimeString(new Date().getTime() - this._startTimestamp);
            } else if(this._startTimestamp && this._endTimestamp && this._timerState === STATE_ENDED){
                this._uptimeString = millisToTimeString(this._endTimestamp - this._startTimestamp);
            } else {
                this._uptimeString = millisToTimeString(0);
            }
        }, 100)
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        clearInterval(this._clockInterval);
    }

    setState(start: number, end: number, state: string) {
        if(start) {
            this._startTimestamp = start;
        }
        if(end) {
            this._endTimestamp = end;
        }
        if(state) {
            this._timerState = state;
        }
    }

    render() {
        return html`
            <span>${this._uptimeString}</span>
        `;
    }
}