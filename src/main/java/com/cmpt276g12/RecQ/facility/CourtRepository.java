package com.cmpt276g12.RecQ.facility;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findAllByOrderByGymNameAscSportNameAscSessionNameAscCourtNameAsc();
}
