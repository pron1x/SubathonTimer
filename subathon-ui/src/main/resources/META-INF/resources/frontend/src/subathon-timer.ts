import {html, LitElement} from "lit";
import {customElement, state} from "lit/decorators.js";
import {millisToTimeString} from "./utils";

@customElement('subathon-timer')
class SubathonTimer extends LitElement {

    readonly STATE_TICKING: string = "TICKING";
    readonly STATE_PAUSED: string = "PAUSED";
    readonly STATE_INITIALIZED: string = "INITIALIZED";
    readonly STATE_ENDED: string = "ENDED";

    _startTime: number;
    _endTime: number;
    _lastUpdateTime: number;
    _timerState: string;

    @state()
    _timeLeftString: string;

    _clockInterval: number;

    connectedCallback() : void {
        super.connectedCallback();
        this._clockInterval = setInterval(() => {
            const ms = this.calculateTimeLeft();
            if(ms < 0) {
                this._timeLeftString = "SUBATHON ENDED!";
            } else {
                this._timeLeftString = millisToTimeString(ms);
            }
        }, 1000);
    }

    setStartTime(date: string) {
        if(date) {
            this._startTime = new Date(date).getTime();
        }
    }

    setEndTime(date: string) {
        if(date) {
            this._endTime = new Date(date).getTime();
        }
    }

    setLastUpdateTime(date: string) {
        if(date) {
            this._lastUpdateTime = new Date(date).getTime();
        }
    }

    setTimerState(state: string) {
        if(state) {
            this._timerState = state;
        }
    }

    calculateTimeLeft(): number {
        if(this._timerState === this.STATE_ENDED) {
            return -1;
        } else if (this._timerState === this.STATE_INITIALIZED) {
            return (3600 * 1000);
        } else if (this._timerState === this.STATE_PAUSED) {
            return (this._endTime - this._lastUpdateTime);
        } else if (this._timerState === this.STATE_TICKING) {
            return (this._endTime - new Date().getTime());
        } else {
            return 0;
        }
    }

    render() {
        return html`
            <span .textContent="${this._timeLeftString}"></span>
        `;
    }

}