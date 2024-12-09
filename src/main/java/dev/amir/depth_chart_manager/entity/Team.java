package dev.amir.depth_chart_manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Version
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String depthChart;

    @ManyToOne
    @JoinColumn(name = "sport_id", referencedColumnName = "id")
    private Sport sport;
}
