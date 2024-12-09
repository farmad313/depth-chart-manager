package dev.amir.depth_chart_manager.service.impl;

import dev.amir.depth_chart_manager.entity.Player;
import dev.amir.depth_chart_manager.repository.PlayerRepository;
import dev.amir.depth_chart_manager.service.PlayerService;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }
}
