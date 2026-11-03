

package com.cmpt276g12.RecQ.turn;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnOfferRepository
        extends JpaRepository<TurnOffer, Long> {

    List<TurnOffer> findByExpiresAtLessThanEqual(Instant now);
}
