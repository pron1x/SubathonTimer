package tools.subathon.timer.datamodel;

import tools.subathon.timer.datamodel.enums.EventType;

public class SubathonRaidEvent extends SubathonEvent {
    private int amount;

    public SubathonRaidEvent() {
        this.setType(EventType.RAID);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "SubathonRaidEvent{" +
                "amount=" + amount +
                "} " + super.toString();
    }
}
