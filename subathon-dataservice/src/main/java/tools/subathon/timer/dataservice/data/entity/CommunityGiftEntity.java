package tools.subathon.timer.dataservice.data.entity;

import tools.subathon.timer.datamodel.enums.SubTier;
import jakarta.persistence.*;

@Entity
@Table(name = "community_gift_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "GIFT")
public class CommunityGiftEntity extends EventEntity {

    @Column(name = "amount")
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", columnDefinition = "ENUM('TIER_1', 'TIER_2', 'TIER_3')")
    private SubTier tier;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public SubTier getTier() {
        return tier;
    }

    public void setTier(SubTier tier) {
        this.tier = tier;
    }

    @Override
    public String toString() {
        return "CommunityGiftEntity{" +
                "amount=" + amount +
                ", tier=" + tier +
                "} " + super.toString();
    }
}
