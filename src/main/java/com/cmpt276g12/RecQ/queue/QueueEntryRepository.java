package com.cmpt276g12.RecQ.queue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findBySportRunIdAndStatusInOrderByJoinedAtAscIdAsc(
            Long sportRunId, Collection<QueueStatus> statuses);

    boolean existsBySportRunIdAndUserIdAndStatusIn(
            Long sportRunId, Long userId, Collection<QueueStatus> statuses);

    boolean existsBySportRunIdAndStatusIn(Long sportRunId, Collection<QueueStatus> statuses);

    long countBySportRunIdAndStatus(Long sportRunId, QueueStatus status);
}