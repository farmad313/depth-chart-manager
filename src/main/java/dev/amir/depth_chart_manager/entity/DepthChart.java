package dev.amir.depth_chart_manager.entity;

import dev.amir.depth_chart_manager.model.enums.Position;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "depth_chart", uniqueConstraints = {
        @UniqueConstraint(name = "UNQ_TEAM_POSITION_PLAYERNUMBER", columnNames = {"team_id", "position", "player_id"})
})
@Data
@NoArgsConstructor
public class DepthChart {
    @Version
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private Player player;

    @Enumerated(EnumType.STRING)
    private Position position;

    private int positionDepth;


    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = true)
    private Team team;
}
