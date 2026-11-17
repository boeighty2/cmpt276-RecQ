package com.cmpt276g12.RecQ.queue;

import java.time.Instant;

public record QueueEntryView(Long id, Long userId, String username,
                             QueueStatus status, Instant joinedAt) {}