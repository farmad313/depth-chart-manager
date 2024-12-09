package dev.amir.depth_chart_manager.service.impl;


import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.entity.Player;
import dev.amir.depth_chart_manager.model.enums.Position;
import dev.amir.depth_chart_manager.repository.CustomDepthChartRepository;
import dev.amir.depth_chart_manager.repository.DepthChartRepository;
import dev.amir.depth_chart_manager.service.DepthChartService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DepthChartServiceImpl implements DepthChartService {

    private final DepthChartRepository depthChartRepository;
    private final CustomDepthChartRepository customDepthChartRepository;

    public DepthChartServiceImpl(DepthChartRepository depthChartRepository, CustomDepthChartRepository customDepthChartRepository) {
        this.depthChartRepository = depthChartRepository;
        this.customDepthChartRepository = customDepthChartRepository;
    }

    @Transactional
    public synchronized void addPlayerToDepthChart(Position position, Player player, Integer positionDepth) {
        List<DepthChart> depthCharts = depthChartRepository.findByPositionOrderByPositionDepth(position);

        if (positionDepth == null) {
            positionDepth = depthCharts.size() + 1;
        }

        List<DepthChart> updatedDepthCharts = adjustDepthPositionsByShiftingForward(positionDepth, depthCharts);

        updatedDepthCharts.add(getNewDepthChart(position, player, positionDepth));

        logUpdatedDepthCharts("Player added to depthChart. List of adjusted depthPositions for {} position: {}", position, updatedDepthCharts);
        depthChartRepository.saveAll(updatedDepthCharts);
    }

    @Transactional
    public synchronized Optional<DepthChart> removePlayerFromDepthChart(Position position, Player player) {
        Optional<DepthChart> depthChart = Optional.ofNullable(customDepthChartRepository.deleteAndReturnByPositionAndPlayerNumber(position, player.getNumber()));

        if (depthChart.isEmpty()) {
            return Optional.empty();
        }

        List<DepthChart> updatedDepthCharts = adjustDepthPositionsByShiftingBack(position);
        logUpdatedDepthCharts("Player removed from depthChart. List of adjusted depthPositions for {} position: {}", position, updatedDepthCharts);
        return depthChart;
    }


    private List<DepthChart> adjustDepthPositionsByShiftingBack(Position position) {
        List<DepthChart> updatedDepthCharts = depthChartRepository.findByPositionOrderByPositionDepth(position);

        AtomicInteger currentPosition = new AtomicInteger(0);
        updatedDepthCharts.forEach(dc -> {
            if (dc.getPositionDepth() != currentPosition.get()) {
                dc.setPositionDepth(currentPosition.get());
            }
            currentPosition.incrementAndGet();
        });

        depthChartRepository.saveAll(updatedDepthCharts);
        return updatedDepthCharts;
    }


    public List<Player> getBackups(Position position, Player player) {
        Optional<DepthChart> depthChart = depthChartRepository.findByPositionAndPlayerNumber(position, player.getNumber());

        if (depthChart.isEmpty()) {
            log.info("Player {} is not in the depth chart for {} position", player.getNumber(), position);
        }

        return depthChart.<List<Player>>map(chart -> depthChartRepository.findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(position, chart.getPositionDepth())
                        .stream()
                        .map(DepthChart::getPlayer)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll))
                .orElseGet(() -> {
                    log.info("No backups found for player {} in position {}", player.getNumber(), position);
                    return new ArrayList<>();
                });
    }


    public Map<Position, List<Player>> getFullDepthChart() {
        var fullDepthChart = groupDepthChart();

        fullDepthChart.forEach((position, players) -> {
            String formattedPlayers = players.stream()
                    .map(player -> String.format("(#%d, %s)", player.getNumber(), player.getName()))
                    .collect(Collectors.joining(", "));
            log.info("{} - {}", position, formattedPlayers);
        });

        return fullDepthChart;
    }

    private Map<Position, List<Player>> groupDepthChart() {
        List<DepthChart> depthCharts = depthChartRepository.findAll();
        return depthCharts.stream()
                .collect(Collectors.groupingBy(DepthChart::getPosition,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(DepthChart::getPositionDepth))
                                        .map(DepthChart::getPlayer)
                                        .toList()
                        )));
    }

    private static void logUpdatedDepthCharts(String s, Position position, List<DepthChart> updatedDepthCharts) {
        log.info(s, position,
                updatedDepthCharts.stream()
                        .sorted(Comparator.comparingInt(DepthChart::getPositionDepth))
                        .map(dc -> String.format("(PositionDepth=%d,PlayerNumber=%d)", dc.getPositionDepth(), dc.getPlayer().getNumber()))
                        .collect(Collectors.joining(", ")));
    }

    private static DepthChart getNewDepthChart(Position position, Player player, Integer positionDepth) {
        DepthChart newDepthChart = new DepthChart();
        newDepthChart.setPosition(position);
        newDepthChart.setPlayer(player);
        newDepthChart.setPositionDepth(positionDepth);
        return newDepthChart;
    }

    private static List<DepthChart> adjustDepthPositionsByShiftingForward(Integer positionDepth, List<DepthChart> depthCharts) {
        return depthCharts.stream()
                .filter(dc -> dc.getPositionDepth() >= positionDepth)
                .map(dc -> {
                    dc.setPositionDepth(dc.getPositionDepth() + 1);
                    return dc;
                })
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
