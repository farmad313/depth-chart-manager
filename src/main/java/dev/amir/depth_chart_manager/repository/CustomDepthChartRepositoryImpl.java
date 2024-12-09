package dev.amir.depth_chart_manager.repository;

import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.model.enums.Position;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomDepthChartRepositoryImpl implements CustomDepthChartRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final DepthChartRepository depthChartRepository;

    public CustomDepthChartRepositoryImpl(DepthChartRepository depthChartRepository) {
        this.depthChartRepository = depthChartRepository;
    }

    @Override
    @Transactional
    public DepthChart deleteAndReturnByPositionAndPlayerNumber(Position position, Long playerNumber) {
        Optional<DepthChart> depthChart = depthChartRepository.findByPositionAndPlayerNumber(position, playerNumber);
        depthChart.ifPresent(entityManager::remove);
        return depthChart.orElse(null);
    }
}
