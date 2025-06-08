package tools.subathon.timer.datamodel.user;

public class UserConfigurationModel {

    private Long id;
    private String channelId;
    private String seJwt;
    private Integer followerSeconds;
    private Integer raiderSeconds;
    private Integer tier1Seconds;
    private Integer tier2Seconds;
    private Integer tier3Seconds;
    private Integer tier1GiftSeconds;
    private Integer tier2GiftSeconds;
    private Integer tier3GiftSeconds;
    private Integer currencySeconds;
    private Integer bitsSeconds;
    private Integer initialSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getSeJwt() {
        return seJwt;
    }

    public void setSeJwt(String seJwt) {
        this.seJwt = seJwt;
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
}
