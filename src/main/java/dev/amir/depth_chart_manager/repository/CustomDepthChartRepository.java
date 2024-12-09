package dev.amir.depth_chart_manager.repository;

import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.model.enums.Position;

public interface CustomDepthChartRepository {
    DepthChart deleteAndReturnByPositionAndPlayerNumber(Position position, Long playerNumber);
}

