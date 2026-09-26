package com.pbtp1.repository;

import com.pbtp1.model.EventoOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;

public interface EventoOutboxRepository extends JpaRepository<EventoOutbox, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventoOutbox> findTop100ByPublicadoEmIsNullOrderByIdAsc();
}
