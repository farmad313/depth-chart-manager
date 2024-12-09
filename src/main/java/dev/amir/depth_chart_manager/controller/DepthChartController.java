package dev.amir.depth_chart_manager.controller;

import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.entity.Player;
import dev.amir.depth_chart_manager.model.enums.Position;
import dev.amir.depth_chart_manager.service.DepthChartService;
import dev.amir.depth_chart_manager.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/sport/{sport}/team/{team}/depthchart")
public class DepthChartController {

    private final DepthChartService depthChartService;
    private final PlayerService playerService;

    public DepthChartController(DepthChartService depthChartService, PlayerService playerService) {
        this.depthChartService = depthChartService;
        this.playerService = playerService;
    }

    @PostMapping("/add")
    public void addPlayerToDepthChart(
            @PathVariable String sport,
            @PathVariable String team,
            @RequestBody Player player,
            @RequestParam(required = false) Integer positionDepth) {

        playerService.savePlayer(player);
        depthChartService.addPlayerToDepthChart(player.getPosition(), player, positionDepth);
    }

    @DeleteMapping("/remove")
    public Optional<DepthChart> removePlayerFromDepthChart(
            @PathVariable String sport,
            @PathVariable String team,
            @RequestBody Player player) {
        return depthChartService.removePlayerFromDepthChart(player.getPosition(), player);
    }

    @GetMapping("/backups")
    public List<Player> getBackups(
            @PathVariable String sport,
            @PathVariable String team,
            @RequestBody Player player) {
        return depthChartService.getBackups(player.getPosition(), player);
    }

    @GetMapping("/full")
    public Map<Position, List<Player>> getFullDepthChart(
            @PathVariable String sport,
            @PathVariable String team) {
        return depthChartService.getFullDepthChart();
    }
}
