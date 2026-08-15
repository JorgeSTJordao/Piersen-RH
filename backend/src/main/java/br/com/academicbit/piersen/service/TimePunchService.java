package br.com.academicbit.piersen.service;

import br.com.academicbit.piersen.domain.Employee;
import br.com.academicbit.piersen.domain.PunchStatus;
import br.com.academicbit.piersen.domain.PunchType;
import br.com.academicbit.piersen.domain.TimePunch;
import br.com.academicbit.piersen.dto.PunchResponse;
import br.com.academicbit.piersen.dto.TimeSheetResponse;
import br.com.academicbit.piersen.exception.BusinessException;
import br.com.academicbit.piersen.exception.NotFoundException;
import br.com.academicbit.piersen.repository.TimePunchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimePunchService {

    private static final Map<PunchType, PunchType> EXPECTED_PREVIOUS = expectedPrevious();

    private final TimePunchRepository timePunchRepository;
    private final EmployeeService employeeService;
    private final Clock clock;

    private static Map<PunchType, PunchType> expectedPrevious() {
        Map<PunchType, PunchType> map = new EnumMap<>(PunchType.class);
        map.put(PunchType.INICIO_INTERVALO, PunchType.ENTRADA);
        map.put(PunchType.FIM_INTERVALO, PunchType.INICIO_INTERVALO);
        map.put(PunchType.SAIDA, PunchType.ENTRADA);
        return map;
    }

    @Transactional
    public TimePunch punch(Long employeeId, PunchType type) {
        Employee employee = employeeService.requireActive(employeeId);
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate day = now.toLocalDate();
        List<TimePunch> today = timePunchRepository.findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(employeeId, day);
        validateSequence(type, today);
        return timePunchRepository.save(TimePunch.builder()
                .employee(employee)
                .punchedAt(now)
                .referenceDay(day)
                .type(type)
                .status(PunchStatus.REGISTRADO)
                .build());
    }

    private void validateSequence(PunchType type, List<TimePunch> today) {
        PunchType last = today.isEmpty() ? null : today.get(today.size() - 1).getType();
        if (type == PunchType.ENTRADA) {
            if (last != null && last != PunchType.SAIDA) {
                throw new BusinessException("Ja existe uma jornada aberta para hoje");
            }
            return;
        }
        if (last == null) {
            throw new BusinessException("Registre a entrada antes de qualquer outra marcacao");
        }
        if (last == PunchType.FIM_INTERVALO && type == PunchType.SAIDA) {
            return;
        }
        if (EXPECTED_PREVIOUS.get(type) != last) {
            throw new BusinessException("Sequencia de marcacao invalida: " + last + " -> " + type);
        }
    }

    @Transactional(readOnly = true)
    public TimeSheetResponse timeSheet(Long employeeId, LocalDate day) {
        List<TimePunch> punches = timePunchRepository.findByEmployeeIdAndReferenceDayOrderByPunchedAtAsc(employeeId, day);
        return new TimeSheetResponse(employeeId, day, punches.stream().map(PunchResponse::from).toList(),
                workedMinutes(punches));
    }

    long workedMinutes(List<TimePunch> punches) {
        long minutes = 0;
        LocalDateTime openedAt = null;
        for (TimePunch punch : punches) {
            if (punch.getType() == PunchType.ENTRADA || punch.getType() == PunchType.FIM_INTERVALO) {
                openedAt = punch.getPunchedAt();
            } else if (openedAt != null) {
                minutes += Duration.between(openedAt, punch.getPunchedAt()).toMinutes();
                openedAt = null;
            }
        }
        return minutes;
    }

    @Transactional(readOnly = true)
    public List<TimePunch> history(Long employeeId) {
        return timePunchRepository.findByEmployeeIdOrderByPunchedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<TimePunch> pendingReview() {
        return timePunchRepository.findByStatusOrderByPunchedAtAsc(PunchStatus.REGISTRADO);
    }

    @Transactional
    public TimePunch review(Long punchId, PunchStatus status) {
        TimePunch punch = timePunchRepository.findById(punchId)
                .orElseThrow(() -> new NotFoundException("Marcacao nao encontrada: " + punchId));
        if (status == PunchStatus.REGISTRADO) {
            throw new BusinessException("A conferencia deve resultar em CONFERIDO ou ABONADO");
        }
        punch.setStatus(status);
        return timePunchRepository.save(punch);
    }
}
