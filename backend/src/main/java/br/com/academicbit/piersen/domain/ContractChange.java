package br.com.academicbit.piersen.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_change")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "previous_position", nullable = false, length = 80)
    private String previousPosition;

    @Column(name = "new_position", nullable = false, length = 80)
    private String newPosition;

    @Column(name = "previous_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousSalary;

    @Column(name = "new_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal newSalary;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", nullable = false, length = 150)
    private String changedBy;
}
