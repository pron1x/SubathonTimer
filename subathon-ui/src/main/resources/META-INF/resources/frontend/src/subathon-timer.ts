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
            this._timeLeftString = this.createTimeLeftString(ms);
        }, 100);
    }

    updateToNewTimerEvent(startDate: string, endDate: string, updateDate: string, timerState: string) {
        if(startDate) {
            console.log(startDate)
            this._startTime = new Date(startDate).getTime();
        }
        if(endDate) {
            console.log(endDate)
            this._endTime = new Date(endDate).getTime();
        }
        if(updateDate) {
            console.log(updateDate)
            this._lastUpdateTime = new Date(updateDate).getTime();
        }
        if(timerState) {
            console.log(timerState)
            this._timerState = timerState;
        }
        this._timeLeftString = this.createTimeLeftString(this.calculateTimeLeft());
    }

    createTimeLeftString(milliseconds) {
        return milliseconds < 0 ? "SUBATHON ENDED!" : millisToTimeString(milliseconds);
    }

    calculateTimeLeft(): number {
        if(this._timerState === this.STATE_ENDED) {
            return -1;
        } else if (this._timerState === this.STATE_INITIALIZED) {
            return (this._endTime - this._lastUpdateTime);
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