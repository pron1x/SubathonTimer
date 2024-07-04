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
                this._uptimeString = this.convertMillisToTimeString(new Date().getTime() - this._startTimestamp.getTime());
                console.log(this._uptimeString);
            } else {
                console.log("Shit's fucked!");
                this._uptimeString = "No start time!";
            }
        }, 1000)
    }

    setStartTime(date: string) {
        console.log("Setting new start timestamp!", date);
        this._startTimestamp = new Date(date);
    }

    convertMillisToTimeString(milliseconds: number): string {
        const seconds = Math.floor((milliseconds / 1000) % 60);
        const minutes = Math.floor((milliseconds / 1000 / 60) % 60);
        const hours = Math.floor((milliseconds / 1000 / 3600) % 24);

        return [
            hours.toString(),
            (minutes.toString().length < 2 ? "0" : "") + minutes.toString(),
            (seconds.toString().length < 2 ? "0" : "") + seconds.toString()
        ].join(':');
    }


    render() {
        return html`
            <span .textContent="${this._uptimeString}"></span>
        `;
    }
}