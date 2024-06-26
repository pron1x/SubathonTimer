package com.pronixxx.subathon.data.entity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
@Cacheable(false)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Integer id;

    @Column(name = "insert_time")
    private LocalDateTime insertTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(LocalDateTime insertTime) {
        this.insertTime = insertTime;
    }
}
