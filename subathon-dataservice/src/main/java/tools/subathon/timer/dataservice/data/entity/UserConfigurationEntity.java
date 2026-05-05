package tools.subathon.timer.dataservice.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_configuration")
public class UserConfigurationEntity extends BaseEntity {

    @Column(name = "channel_id", nullable = false, unique = true, length = 30)
    private String channelId;

    @Column(name = "follower_seconds")
    private Integer followerSeconds;

    @Column(name = "raider_seconds")
    private Integer raiderSeconds;

    @Column(name = "tier_1_seconds")
    private Integer tier1Seconds;

    @Column(name = "tier_2_seconds")
    private Integer tier2Seconds;

    @Column(name = "tier_3_seconds")
    private Integer tier3Seconds;

    @Column(name = "tier_1_gift_seconds")
    private Integer tier1GiftSeconds;

    @Column(name = "tier_2_gift_seconds")
    private Integer tier2GiftSeconds;

    @Column(name = "tier_3_gift_seconds")
    private Integer tier3GiftSeconds;

    @Column(name = "currency_seconds")
    private Integer currencySeconds;

    @Column(name = "bits_seconds")
    private Integer bitsSeconds;

    @Column(name = "initial_seconds")
    private Integer initialSeconds;

    @Column(name = "donation_template_pattern")
    private String donationTemplatePattern;

    @Column(name = "donation_template_user")
    private String donationTemplateUser;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Integer getFollowerSeconds() {
        return followerSeconds;
    }

    public void setFollowerSeconds(Integer followerSeconds) {
        this.followerSeconds = followerSeconds;
    }

    public Integer getRaiderSeconds() {
        return raiderSeconds;
    }

    public void setRaiderSeconds(Integer raiderSeconds) {
        this.raiderSeconds = raiderSeconds;
    }

    public Integer getTier1Seconds() {
        return tier1Seconds;
    }

    public void setTier1Seconds(Integer tier1Seconds) {
        this.tier1Seconds = tier1Seconds;
    }

    public Integer getTier2Seconds() {
        return tier2Seconds;
    }

    public void setTier2Seconds(Integer tier2Seconds) {
        this.tier2Seconds = tier2Seconds;
    }

    public Integer getTier3Seconds() {
        return tier3Seconds;
    }

    public void setTier3Seconds(Integer tier3Seconds) {
        this.tier3Seconds = tier3Seconds;
    }

    public Integer getTier1GiftSeconds() {
        return tier1GiftSeconds;
    }

    public void setTier1GiftSeconds(Integer tier1GiftSeconds) {
        this.tier1GiftSeconds = tier1GiftSeconds;
    }

    public Integer getTier2GiftSeconds() {
        return tier2GiftSeconds;
    }

    public void setTier2GiftSeconds(Integer tier2GiftSeconds) {
        this.tier2GiftSeconds = tier2GiftSeconds;
    }

    public Integer getTier3GiftSeconds() {
        return tier3GiftSeconds;
    }

    public void setTier3GiftSeconds(Integer tier3GiftSeconds) {
        this.tier3GiftSeconds = tier3GiftSeconds;
    }

    public Integer getCurrencySeconds() {
        return currencySeconds;
    }

    public void setCurrencySeconds(Integer currencySeconds) {
        this.currencySeconds = currencySeconds;
    }

    public Integer getBitsSeconds() {
        return bitsSeconds;
    }

    public void setBitsSeconds(Integer bitsSeconds) {
        this.bitsSeconds = bitsSeconds;
    }

    public Integer getInitialSeconds() {
        return initialSeconds;
    }

    public void setInitialSeconds(Integer initialSeconds) {
        this.initialSeconds = initialSeconds;
    }

    public String getDonationTemplatePattern() {
        return donationTemplatePattern;
    }

    public void setDonationTemplatePattern(String donationTemplatePattern) {
        this.donationTemplatePattern = donationTemplatePattern;
    }

    public String getDonationTemplateUser() {
        return donationTemplateUser;
    }

    public void setDonationTemplateUser(String donationTemplateUser) {
        this.donationTemplateUser = donationTemplateUser;
    }
}
