package dev.amir.depth_chart_manager.repository;

import dev.amir.depth_chart_manager.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
