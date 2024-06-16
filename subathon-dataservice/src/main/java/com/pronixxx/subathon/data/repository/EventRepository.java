package com.pronixxx.subathon.data.repository;

import com.pronixxx.subathon.data.entity.EventEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface EventRepository extends Repository<EventEntity, Long> {

    EventEntity save(EventEntity event);

    Optional<EventEntity> findById(long id);
}
