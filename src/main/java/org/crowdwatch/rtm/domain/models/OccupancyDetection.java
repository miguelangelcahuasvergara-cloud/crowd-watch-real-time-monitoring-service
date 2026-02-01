package org.crowdwatch.rtm.domain.models;

import java.time.LocalDateTime;

import org.crowdwatch.rtm.domain.enums.BusEvent;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "occupancy_detections")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class OccupancyDetection extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_code", nullable = false)
    private String busCode;
    
    @Column(name = "center_control_operator_code")
    private String centerControlOperatorCode;

    @Column(name = "route_code")
    private String routeCode;

    @Column(name = "organization_code")
    private String organizationCode;

    @Column(name = "detection_type", nullable = false)
    private BusEvent detectionType;

    @Column(name = "detection_sended_at", nullable = false)
    private LocalDateTime detectionSendAt;

    @Column(name = "detection_received_at", nullable = false)
    private LocalDateTime detectionReceivedAt;

    @Column(name = "detection_processing_started_at", nullable = false)
    private LocalDateTime detectionProcessingStartedAt;

    @Column(name = "detection_processing_finished_at")
    private LocalDateTime detectionProcessingFinishedAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
