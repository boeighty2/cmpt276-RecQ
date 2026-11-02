package com.cmpt276g12.RecQ.facility;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRunRepository extends JpaRepository<SportRun, Long> {
    Optional<SportRun> findFirstByGymNameIgnoreCaseAndSportNameIgnoreCaseAndClosedAtIsNullOrderByOpenedAtDesc(
        String gymName, String sportName);
}
