package dev.amir.depth_chart_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.entity.Player;
import dev.amir.depth_chart_manager.model.enums.Position;
import dev.amir.depth_chart_manager.service.DepthChartService;
import dev.amir.depth_chart_manager.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepthChartController.class)
class DepthChartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepthChartService depthChartService;

    @MockBean
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        player.setName("dev.amir");
        player.setNumber(10L);
        player.setPosition(Position.QB);
    }

    @Test
    void testAddPlayerToDepthChart_WhenPlayerNotFound() throws Exception {
        when(playerService.savePlayer(any(Player.class))).thenReturn(player);
        doNothing().when(depthChartService).addPlayerToDepthChart(any(Position.class), any(Player.class), any(Integer.class));

        mockMvc.perform(post("/sport/football/team/eagles/depthchart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk());
    }


    @Test
    void testRemovePlayerFromDepthChart_WhenPlayerFound() throws Exception {
        DepthChart depthChart = new DepthChart();
        depthChart.setId(1L);
        depthChart.setPlayer(player);
        depthChart.setPosition(Position.QB);
        depthChart.setPositionDepth(1);

        when(depthChartService.removePlayerFromDepthChart(any(Position.class), any(Player.class)))
                .thenReturn(Optional.of(depthChart));

        mockMvc.perform(delete("/sport/football/team/eagles/depthchart/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(depthChart.getId()))
                .andExpect(jsonPath("$.player.name").value(player.getName()))
                .andExpect(jsonPath("$.position").value(depthChart.getPosition().name()))
                .andExpect(jsonPath("$.positionDepth").value(depthChart.getPositionDepth()));
    }


    @Test
    void testRemovePlayerFromDepthChart() throws Exception {
        when(depthChartService.removePlayerFromDepthChart(any(Position.class), any(Player.class))).thenReturn(Optional.empty());

        mockMvc.perform(delete("/sport/football/team/eagles/depthchart/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk());
    }


    @Test
    void testGetBackups() throws Exception {
        when(depthChartService.getBackups(any(Position.class), any(Player.class))).thenReturn(Collections.singletonList(player));

        mockMvc.perform(get("/sport/football/team/eagles/depthchart/backups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(player.getName()))
                .andExpect(jsonPath("$[0].number").value(player.getNumber()));
    }

    @Test
    void testGetFullDepthChart() throws Exception {
        when(depthChartService.getFullDepthChart()).thenReturn(Collections.singletonMap(Position.QB, Arrays.asList(player)));

        mockMvc.perform(get("/sport/football/team/eagles/depthchart/full")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.QB[0].name").value(player.getName()))
                .andExpect(jsonPath("$.QB[0].number").value(player.getNumber()));
    }
}
