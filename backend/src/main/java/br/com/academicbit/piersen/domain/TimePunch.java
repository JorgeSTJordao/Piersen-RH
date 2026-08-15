package br.com.academicbit.piersen.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_punch")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePunch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "punched_at", nullable = false)
    private LocalDateTime punchedAt;

    @Column(name = "reference_day", nullable = false)
    private LocalDate referenceDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PunchType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PunchStatus status;
}
