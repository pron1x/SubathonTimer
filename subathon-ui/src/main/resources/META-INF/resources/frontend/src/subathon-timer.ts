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

    _timeDelta: number = 0;

    @state()
    _timeLeftString: string;

    _clockInterval: number;
    _syncInterval: number;

    connectedCallback() : void {
        super.connectedCallback();
        this.syncWithServerTime();
        this._clockInterval = setInterval(() => {
            const ms = this.calculateTimeLeft();
            this._timeLeftString = this.createTimeLeftString(ms);
        }, 100);
        this._syncInterval = setInterval(() => {
            this.syncWithServerTime();
        }, 60000);
    }

    updateToNewTimerEvent(startDate: number, endDate: number, updateDate: number, timerState: string) {
        if(startDate) {
            console.log("Start: " + startDate);
            this._startTime = startDate;
        }
        if(endDate) {
            console.log("End: " + endDate);
            this._endTime = endDate;
        }
        if(updateDate) {
            console.log("Update: " + updateDate);
            this._lastUpdateTime = updateDate;
        }
        if(timerState) {
            console.log("State: " + timerState);
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
            return (this._endTime - this.getTime());
        } else {
            return 0;
        }
    }

    syncWithServerTime() {
        let serverTimePromise: Promise<number> = this.$server.getCurrentServerTimestamp();
        serverTimePromise.then(t => {
            this._timeDelta = Date.now() - t;
        });
    }

    getTime() {
        return Date.now() - this._timeDelta;
    }

    render() {
        return html`
            <span>${this._timeLeftString}</span>
        `;
    }

}