import {millisToTimeString} from "./utils";

import {html, LitElement} from 'lit';
import {customElement, state} from 'lit/decorators.js';

@customElement('uptime-clock')
class UptimeClock extends LitElement {
    _startTimestamp: Date;

    @state()
    _uptimeString: string;

    _clockInterval: number;

    connectedCallback(): void {
        super.connectedCallback();
        this._clockInterval = setInterval(() => {
            if(this._startTimestamp) {
                this._uptimeString = millisToTimeString(new Date().getTime() - this._startTimestamp.getTime());
                console.log(this._uptimeString);
            } else {
                this._uptimeString = "00:00:00";
            }
        }, 1000)
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        clearInterval(this._clockInterval);
    }

    setStartTime(date: string) {
        this._startTimestamp = new Date(date);
    }


    render() {
        return html`
            <span .textContent="${this._uptimeString}"></span>
        `;
    }
}