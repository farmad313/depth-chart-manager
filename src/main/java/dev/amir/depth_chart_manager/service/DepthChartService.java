package dev.amir.depth_chart_manager.service;


import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.entity.Player;
import dev.amir.depth_chart_manager.model.enums.Position;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DepthChartService {
    void addPlayerToDepthChart(Position position, Player player, Integer positionDepth);

    Optional<DepthChart> removePlayerFromDepthChart(Position position, Player player);

    List<Player> getBackups(Position position, Player player);

    Map<Position, List<Player>> getFullDepthChart();
}
