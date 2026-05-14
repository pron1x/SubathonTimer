package tools.subathon.timer.dataservice.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.NonNull;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timer")
public class TimerEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "insert_time")
    protected Instant insertTime;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", columnDefinition = "ENUM('UNINITIALIZED', 'INITIALIZED', 'PAUSED', 'TICKING', 'ENDED')")
    private TimerState state;

    @Column(name = "update_time")
    private Instant updateTime;

    @Column(name = "monetized_seconds_per_point")
    private Integer monetizedSecondsPerPoint;

    @Column(name = "total_monetized_extension_seconds")
    private long totalMonetizedExtensionSeconds;

    @Column(name = "points")
    private long points;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public TimerState getState() {
        return state;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public Instant getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Instant insertTime) {
        this.insertTime = insertTime;
    }

    public Integer getMonetizedSecondsPerPoint() {
        return monetizedSecondsPerPoint;
    }

    public void setMonetizedSecondsPerPoint(Integer monetizedSecondsPerPoint) {
        this.monetizedSecondsPerPoint = monetizedSecondsPerPoint;
    }

    public long getTotalMonetizedExtensionSeconds() {
        return totalMonetizedExtensionSeconds;
    }

    public void setTotalMonetizedExtensionSeconds(long totalMonetizedExtensionSeconds) {
        this.totalMonetizedExtensionSeconds = totalMonetizedExtensionSeconds;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    @Override
    public @NonNull String toString() {
        return "TimerEntity{" +
                "id=" + id +
                ", insertTime=" + insertTime +
                ", channelName='" + channelName + '\'' +
                ", channelId='" + channelId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", state=" + state +
                ", updateTime=" + updateTime +
                ", monetizedSecondsPerPoint=" + monetizedSecondsPerPoint +
                ", totalMonetizedExtensionSeconds=" + totalMonetizedExtensionSeconds +
                ", points=" + points +
                '}';
    }
}
