package com.cmpt276g12.RecQ.queue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class QueueService {

    // active - still in line
    private static final List<QueueStatus> ACTIVE =
            List.of(QueueStatus.WAITING, QueueStatus.OFFERED);

    private final QueueEntryRepository repo;

    public QueueService(QueueEntryRepository repo) {
        this.repo = repo;
    }

    public QueueEntry joinQueue(Long sportRunId, Long userId) {
        if (repo.existsBySportRunIdAndUserIdAndStatusIn(sportRunId, userId, ACTIVE)) {
            throw new IllegalStateException("User is already in this queue");
        }
        return repo.save(new QueueEntry(sportRunId, userId));
    }

    public void leaveQueue(Long entryId) {
        setIfActive(entryId, QueueStatus.LEFT);
    }

    public void removeEntry(Long entryId) {
        setIfActive(entryId, QueueStatus.REMOVED);
    }

    @Transactional(readOnly = true)
    public List<QueueEntry> getQueue(Long sportRunId) {
        return repo.findBySportRunIdAndStatusInOrderByJoinedAtAscIdAsc(sportRunId, ACTIVE);
    }

    @Transactional(readOnly = true)
    public int getPosition(Long sportRunId, Long userId) {
        List<QueueEntry> queue = getQueue(sportRunId);
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUserId().equals(userId)) {
                return i + 1;
            }
        }
        throw new NoSuchElementException("User is not in this queue");
    }

    @Transactional(readOnly = true)
    public long getWaitingCount(Long sportRunId) {
        return repo.countBySportRunIdAndStatus(sportRunId, QueueStatus.WAITING);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEntries(Long sportRunId) {
        return repo.existsBySportRunIdAndStatusIn(sportRunId, ACTIVE);
    }

    // epic 5 hook: first entry still waiting
    @Transactional(readOnly = true)
    public Optional<QueueEntry> getNextWaiting(Long sportRunId) {
        return getQueue(sportRunId).stream()
                .filter(e -> e.getStatus() == QueueStatus.WAITING)
                .findFirst();
    }

    // epic 5 hook: court was offered for this entry
    public void markOffered(Long entryId) {
        find(entryId).setStatus(QueueStatus.OFFERED);
    }

    // everyone still in line is dropped if a run is over
    public void closeQueue(Long sportRunId) {
        for (QueueEntry e : getQueue(sportRunId)) {
            e.setStatus(QueueStatus.REMOVED);
        }
    }

    private QueueEntry find(Long entryId) {
        return repo.findById(entryId)
                .orElseThrow(() -> new NoSuchElementException("Queue entry not found"));
    }

    private void setIfActive(Long entryId, QueueStatus newStatus) {
        QueueEntry e = find(entryId);
        if (!ACTIVE.contains(e.getStatus())) {
            throw new IllegalStateException("Entry is no longer active");
        }
        e.setStatus(newStatus);
    }
}