package dev.amir.depth_chart_manager.repository;

import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepthChartRepository extends JpaRepository<DepthChart, Long> {
    List<DepthChart> findByPositionOrderByPositionDepth(Position position);

    Optional<DepthChart> findByPositionAndPlayerNumber(Position position, Long playerNumber);

    List<DepthChart> findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(Position position, int positionDepth);
}

