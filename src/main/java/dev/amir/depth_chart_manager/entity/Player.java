package dev.amir.depth_chart_manager.entity;


import dev.amir.depth_chart_manager.model.enums.Position;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player", uniqueConstraints = {
        @UniqueConstraint(name = "UNQ_NUMBER_TEAM", columnNames = {"number", "team_id"})
})
@Data
@NoArgsConstructor
public class Player {
    @Version
    @EqualsAndHashCode.Exclude
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long number;

    @Enumerated(EnumType.STRING)
    private Position position;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "team_id", referencedColumnName = "id", nullable = true)
    private Team team;
}
