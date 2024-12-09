package dev.amir.depth_chart_manager.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.amir.depth_chart_manager.entity.DepthChart;
import dev.amir.depth_chart_manager.entity.Player;
import dev.amir.depth_chart_manager.model.enums.Position;
import dev.amir.depth_chart_manager.repository.CustomDepthChartRepository;
import dev.amir.depth_chart_manager.repository.DepthChartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;


class DepthChartServiceImplTest {

    @Mock
    private DepthChartRepository depthChartRepository;

    @Mock
    private CustomDepthChartRepository customDepthChartRepository;

    @InjectMocks
    private DepthChartServiceImpl depthChartService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup in-memory appender for capturing logs
        Logger logger = (Logger) LoggerFactory.getLogger(DepthChartServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void testAddPlayerToDepthChart_WithSpecificDepthPosition_PlayerIsAddedToSpecificDepthAndOthersShifted() {
        // Given
        Position position = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);

        List<DepthChart> existingDepthCharts = new ArrayList<>();
        existingDepthCharts.add(depthChartBuilder(position, player1, 1));
        existingDepthCharts.add(depthChartBuilder(position, player2, 2));
        existingDepthCharts.add(depthChartBuilder(position, player3, 3));

        when(depthChartRepository.findByPositionOrderByPositionDepth(position)).thenReturn(existingDepthCharts);

        // When
        Player player4 = playerBuilder(4L, "Player4", 4L);
        depthChartService.addPlayerToDepthChart(position, player4, 1);

        // Then
        verify(depthChartRepository, times(1)).saveAll(anyList());

        // Verify the log message
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals("Player added to depthChart. List of adjusted depthPositions for QB position: (PositionDepth=1,PlayerNumber=4), (PositionDepth=2,PlayerNumber=1), (PositionDepth=3,PlayerNumber=2), (PositionDepth=4,PlayerNumber=3)", logsList.get(0).getFormattedMessage());
    }


    @Test
    void addPlayerToDepthChart_WithoutSpecificDepth_PlayerIsAddedToEndPosition() {
        // Given
        Position position = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);


        List<DepthChart> existingDepthCharts = new ArrayList<>();
        existingDepthCharts.add(depthChartBuilder(position, player1, 1));
        existingDepthCharts.add(depthChartBuilder(position, player2, 2));
        existingDepthCharts.add(depthChartBuilder(position, player3, 3));


        when(depthChartRepository.findByPositionOrderByPositionDepth(position)).thenReturn(existingDepthCharts);

        // When
        Player player4 = playerBuilder(4L, "Player4", 4L);
        // Call the method without specifying the depth position
        depthChartService.addPlayerToDepthChart(position, player4, null);

        // Then
        verify(depthChartRepository, times(1)).saveAll(anyList());

        // Verify the log message
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals("Player added to depthChart. List of adjusted depthPositions for QB position: (PositionDepth=4,PlayerNumber=4)", logsList.get(0).getFormattedMessage());

    }


    @Test
    void removePlayerFromDepthChart_WhenPlayerExists_PlayerIsRemovedAndOthersShifted() {
        // Given
        Position position = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);

        List<DepthChart> existingDepthCharts = new ArrayList<>();
        existingDepthCharts.add(depthChartBuilder(position, player1, 1));
        existingDepthCharts.add(depthChartBuilder(position, player3, 3));

        DepthChart depthChartToBeRemoved = depthChartBuilder(position, player2, 2);

        when(customDepthChartRepository.deleteAndReturnByPositionAndPlayerNumber(position, player1.getNumber())).thenReturn(depthChartToBeRemoved);
        when(depthChartRepository.findByPositionOrderByPositionDepth(position)).thenReturn(existingDepthCharts);

        // When
        Optional<DepthChart> result = depthChartService.removePlayerFromDepthChart(position, player1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(depthChartToBeRemoved, result.get());

        // Verify interactions
        verify(customDepthChartRepository, times(1))
                .deleteAndReturnByPositionAndPlayerNumber(position, player1.getNumber());
        verify(depthChartRepository, times(1)).findByPositionOrderByPositionDepth(position);

        // Verify the log message
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertEquals("Player removed from depthChart. List of adjusted depthPositions for QB position: (PositionDepth=0,PlayerNumber=1), (PositionDepth=1,PlayerNumber=3)", logsList.get(0).getFormattedMessage());

    }


    @Test
    void removePlayerFromDepthChart_PlayerNotExists() {
        // Given
        Position position = Position.QB;
        Player player1 = playerBuilder(1L, "Non-Existent Player", 1L);

        when(customDepthChartRepository.deleteAndReturnByPositionAndPlayerNumber(position, player1.getNumber())).thenReturn(null);

        // When
        Optional<DepthChart> result = depthChartService.removePlayerFromDepthChart(position, player1);

        // Then
        assertFalse(result.isPresent());

        verify(customDepthChartRepository, times(1)).deleteAndReturnByPositionAndPlayerNumber(position, player1.getNumber());
        verify(depthChartRepository, times(0)).findByPositionOrderByPositionDepth(position);
    }


    @Test
    void getBackups_PlayerExists() {
        // Given
        Position position = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);
        Player player4 = playerBuilder(4L, "Player4", 4L);

        List<DepthChart> existingDepthCharts = new ArrayList<>();
        existingDepthCharts.add(depthChartBuilder(position, player1, 1));
        existingDepthCharts.add(depthChartBuilder(position, player2, 2));
        existingDepthCharts.add(depthChartBuilder(position, player3, 3));
        existingDepthCharts.add(depthChartBuilder(position, player4, 4));

        DepthChart depthChart = existingDepthCharts.get(1); // player2 is at position depth 2

        List<DepthChart> backups = new ArrayList<>();
        backups.add(depthChartBuilder(position, player3, 3)); // player3 as backup
        backups.add(depthChartBuilder(position, player4, 4)); // player4 as backup

        when(depthChartRepository.findByPositionAndPlayerNumber(position, player2.getNumber())).thenReturn(Optional.of(depthChart));
        when(depthChartRepository.findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(position, depthChart.getPositionDepth())).thenReturn(backups);

        // When
        List<Player> result = depthChartService.getBackups(position, player2);


        // Then
        assertAll("Check all conditions",
                () -> assertEquals(2, result.size(), "player2 has 2 backups"),

                () -> assertEquals("Player3", result.get(0).getName(), "The name of the first backup player should be Player3"),
                () -> assertEquals(3L, result.get(0).getNumber(), "The number of the first backup player should be 3"),

                () -> assertEquals("Player4", result.get(1).getName(), "The name of the second backup player should be Player4"),
                () -> assertEquals(4L, result.get(1).getNumber(), "The number of the second backup player should be 4")
        );


        // Verify interactions with the mocks
        verify(depthChartRepository, times(1)).findByPositionAndPlayerNumber(position, player2.getNumber());
        verify(depthChartRepository, times(1)).findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(position, depthChart.getPositionDepth());
    }


    @Test
    void getBackups_PlayerNotExists() {
        // Given
        Position position = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);

        List<DepthChart> existingDepthCharts = new ArrayList<>();
        existingDepthCharts.add(depthChartBuilder(position, player1, 1));
        existingDepthCharts.add(depthChartBuilder(position, player2, 2));
        existingDepthCharts.add(depthChartBuilder(position, player3, 3));

        when(depthChartRepository.findByPositionAndPlayerNumber(position, player2.getNumber())).thenReturn(Optional.empty());

        // When
        List<Player> result = depthChartService.getBackups(position, player2);

        // Then
        assertTrue(result.isEmpty(), "Backup player list should be empty when player does not exist");

        verify(depthChartRepository, times(1)).findByPositionAndPlayerNumber(position, player2.getNumber());
        verify(depthChartRepository, never()).findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(any(), anyInt());
    }

    @Test
    void getBackups_NoBackups() {
        // Given
        Position position = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);

        List<DepthChart> existingDepthCharts = new ArrayList<>();
        existingDepthCharts.add(depthChartBuilder(position, player1, 1));
        existingDepthCharts.add(depthChartBuilder(position, player2, 2));
        existingDepthCharts.add(depthChartBuilder(position, player3, 3));

        DepthChart depthChart = existingDepthCharts.get(1); // player2 is at position depth 2

        List<DepthChart> noBackups = new ArrayList<>(); // Empty list for no backups

        when(depthChartRepository.findByPositionAndPlayerNumber(position, player2.getNumber())).thenReturn(Optional.of(depthChart));
        when(depthChartRepository.findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(position, depthChart.getPositionDepth())).thenReturn(noBackups);

        // When
        List<Player> result = depthChartService.getBackups(position, player2);

        // Then
        assertTrue(result.isEmpty(), "Backup player list should be empty when there are no backups");

        verify(depthChartRepository, times(1)).findByPositionAndPlayerNumber(position, player2.getNumber());
        verify(depthChartRepository, times(1)).findByPositionAndPositionDepthGreaterThanOrderByPositionDepth(position, depthChart.getPositionDepth());
    }

    @Test
    void getFullDepthChart_WhenPlayersAddedUnordered_ThenShowDepthChartGroupedAndOrdered() {
        // Given
        Position positionQB = Position.QB;

        Player player1 = playerBuilder(1L, "Player1", 1L);
        Player player2 = playerBuilder(2L, "Player2", 2L);
        Player player3 = playerBuilder(3L, "Player3", 3L);

        List<DepthChart> depthCharts = new ArrayList<>();
        depthCharts.add(depthChartBuilder(positionQB, player1, 1));
        depthCharts.add(depthChartBuilder(positionQB, player3, 3)); // Unordered
        depthCharts.add(depthChartBuilder(positionQB, player2, 2));

        when(depthChartRepository.findAll()).thenReturn(depthCharts);

        // When
        Map<Position, List<Player>> result = depthChartService.getFullDepthChart();

        // Then
        assertEquals(1, result.size(), "The depth chart should contain 1 position group");
        assertTrue(result.containsKey(positionQB), "The depth chart should contain the QB position");

        List<Player> players = result.get(positionQB);
        assertEquals(3, players.size(), "The QB depth chart should contain 3 players");
        assertEquals(player1, players.get(0), "The first player should be Player1");
        assertEquals(player2, players.get(1), "The second player should be Player2");
        assertEquals(player3, players.get(2), "The third player should be Player3");

        // Verify log messages
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size(), "There should be one log entry");
        String expectedLogMessage = "QB - (#1, Player1), (#2, Player2), (#3, Player3)";
        assertEquals(expectedLogMessage, logsList.get(0).getFormattedMessage(), "The log message should match the expected format");

        verify(depthChartRepository, times(1)).findAll();
    }


    @Test
    void getFullDepthChart_WhenPlayersAddedInTwoDifferentPositions_ThenShowDepthChartGroupedAndOrdered() {
        // Given
        Position positionQB = Position.QB;
        Position positionDT = Position.DT;

        Player player1QB = playerBuilder(1L, "Player1", 1L);
        Player player2QB = playerBuilder(2L, "Player2", 2L);
        Player player3QB = playerBuilder(3L, "Player3", 3L);

        Player player1DT = playerBuilder(4L, "Player4", 4L);
        Player player2DT = playerBuilder(5L, "Player5", 5L);
        Player player3DT = playerBuilder(6L, "Player6", 6L);

        List<DepthChart> depthCharts = new ArrayList<>();
        depthCharts.add(depthChartBuilder(positionQB, player1QB, 1));
        depthCharts.add(depthChartBuilder(positionQB, player2QB, 2));
        depthCharts.add(depthChartBuilder(positionQB, player3QB, 3));
        depthCharts.add(depthChartBuilder(positionDT, player1DT, 1));
        depthCharts.add(depthChartBuilder(positionDT, player2DT, 2));
        depthCharts.add(depthChartBuilder(positionDT, player3DT, 3));

        when(depthChartRepository.findAll()).thenReturn(depthCharts);

        // When
        Map<Position, List<Player>> result = depthChartService.getFullDepthChart();

        // Then
        assertEquals(2, result.size(), "The depth chart should contain 2 position groups");

        // Validate QB position group
        assertTrue(result.containsKey(positionQB), "The depth chart should contain the QB position");
        List<Player> playersQB = result.get(positionQB);
        assertEquals(3, playersQB.size(), "The QB depth chart should contain 3 players");
        assertEquals(player1QB, playersQB.get(0), "The first QB should be Player1");
        assertEquals(player2QB, playersQB.get(1), "The second QB should be Player2");
        assertEquals(player3QB, playersQB.get(2), "The third QB should be Player3");

        // Validate DT position group
        assertTrue(result.containsKey(positionDT), "The depth chart should contain the DT position");
        List<Player> playersDT = result.get(positionDT);
        assertEquals(3, playersDT.size(), "The DT depth chart should contain 3 players");
        assertEquals(player1DT, playersDT.get(0), "The first DT should be Player4");
        assertEquals(player2DT, playersDT.get(1), "The second DT should be Player5");
        assertEquals(player3DT, playersDT.get(2), "The third DT should be Player6");

        // Verify log messages
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(2, logsList.size(), "There should be two log entries");

        String expectedLogMessageQB = "QB - (#1, Player1), (#2, Player2), (#3, Player3)";
        String expectedLogMessageDT = "DT - (#4, Player4), (#5, Player5), (#6, Player6)";

        assertTrue(logsList.stream().anyMatch(log -> log.getFormattedMessage().equals(expectedLogMessageQB)), "The log message for QB should match the expected format");
        assertTrue(logsList.stream().anyMatch(log -> log.getFormattedMessage().equals(expectedLogMessageDT)), "The log message for DT should match the expected format");

        verify(depthChartRepository, times(1)).findAll();
    }


    // Helper methods to create Player and DepthChart instances
    private static DepthChart depthChartBuilder(Position position, Player player, int depthPosition) {
        DepthChart depthChartRecord = new DepthChart();
        depthChartRecord.setPosition(position);
        depthChartRecord.setPlayer(player);
        depthChartRecord.setPositionDepth(depthPosition);
        return depthChartRecord;
    }

    private static Player playerBuilder(Long id, String name, Long number) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setNumber(number);
        return player;
    }
}
