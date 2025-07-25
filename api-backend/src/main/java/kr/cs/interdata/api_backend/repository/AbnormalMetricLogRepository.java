package kr.cs.interdata.api_backend.repository;

import kr.cs.interdata.api_backend.entity.AbnormalMetricLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AbnormalMetricLogRepository extends JpaRepository<AbnormalMetricLog, Integer> {

    /**
     *  - 주어진 시작 시간(start)과 종료 시간(end) 사이에 발생한 임계값 초과(AbnormalMetricLog) 기록을 조회한다.
     *
     * @param start 조회 시작 시간 (포함)
     * @param end   조회 종료 시간 (포함)
     * @return 해당 기간에 발생한 AbnormalMetricLog 리스트
     */
    List<AbnormalMetricLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     *  - 주어진 machine ID로 필터링하여 가장 최근을 기준으로 최대 20개의 로그들을 리스트로 저장해 반환한다.
     *
     * @param targetId  필터링할 machine ID
     * @return  machineId = machineId인 최근 로그들 중 최대 20개를 저장한 리스트
     */
    List<AbnormalMetricLog> findTop20ByMachineIdOrderByTimestampDesc(String targetId);

    List<AbnormalMetricLog> findTop50ByMachineTypeOrderByTimestampDesc(String machineType);

    List<AbnormalMetricLog> findTop50ByMachineNameOrderByTimestampDesc(String machineName);

    List<AbnormalMetricLog> findTop50ByMessageTypeOrderByTimestampDesc(String messageType);

    List<AbnormalMetricLog> findTop50ByMetricNameOrderByTimestampDesc(String metricName);

    /**
     * [날짜 범위 내 이상 로그 조회 + 시간 내림차순 정렬]
     *
     * - 주어진 시작 시간(start)과 종료 시간(end) 사이에 저장된 모든 AbnormalMetricLog 엔티티를 조회합니다.
     * - timestamp 컬럼을 기준으로 '내림차순(최신 → 과거)' 정렬하여 반환합니다.
     *
     * <b>용도:</b>
     * - 특정 날짜(예: 오늘 00:00~23:59)만 선택한 경우, 그 기간 내에서 최신순 50개 로그 등 조회에 활용
     *
     * @param start 조회 시작 시간(포함)
     * @param end   조회 종료 시간(포함)
     * @return      해당 기간의 이상 기록을 최신순으로 정렬한 리스트
     */
    @Query("SELECT l FROM AbnormalMetricLog l WHERE l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
    List<AbnormalMetricLog> findByTimestampBetweenOrderByTimestampDesc(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * [날짜+다양한 복합 필터 조건 + 시간 내림차순 정렬 이상 로그 조회]
     *
     * - 날짜(start~end), machineType, hostName, machineName, messageType, metricName 등 복수 조건을 동적으로 필터링하여 이상 로그를 조회합니다.
     * - 각 파라미터가 null이면 해당 조건은 무시(null-safe 조건)
     * - timestamp 컬럼을 기준으로 '내림차순(최신 → 과거)' 정렬하여 반환합니다.
     *
     * <b>용도:</b>
     * - 여러 조건을 복합적으로 지정하여 최신 로그만 필터로 모으고 싶을 때, 프론트/운영 자기진단 대시보드에 적합
     *
     * @param start        조회 시작 시간(포함, null 허용)
     * @param end          조회 종료 시간(포함, null 허용)
     * @param machineType  머신 종류(예: container, host, null 가능)
     * @param hostName     호스트 이름(필요시 null 가능)
     * @param machineName  머신/컨테이너 이름(null 가능)
     * @param messageType  이벤트 타입(ex. thresholdExceeded 등, null 가능)
     * @param metricName   메트릭명(CPU, MEMORY 등, null 가능)
     * @return             복수 필터 조건에 맞는 이상 기록 최신순 리스트
     */
    @Query("SELECT l FROM AbnormalMetricLog l " +
            "WHERE (:start IS NULL OR l.timestamp >= :start) " +
            "AND (:end IS NULL OR l.timestamp <= :end) " +
            "AND (:machineType IS NULL OR l.machineType = :machineType) " +
            "AND (:hostName IS NULL OR l.hostName = :hostName) " +
            "AND (:machineName IS NULL OR l.machineName = :machineName) " +
            "AND (:messageType IS NULL OR l.messageType = :messageType) " +
            "AND (:metricName IS NULL OR l.metricName = :metricName) " +
            "ORDER BY l.timestamp DESC")
    List<AbnormalMetricLog> findFilteredLogs(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("machineType") String machineType,
            @Param("hostName") String hostName,
            @Param("machineName") String machineName,
            @Param("messageType") String messageType,
            @Param("metricName") String metricName
    );


    @Query("SELECT a FROM AbnormalMetricLog a " +
            "WHERE a.machineType = 'host' AND a.machineName = :machineName " +
            "ORDER BY a.timestamp DESC")
    List<AbnormalMetricLog> findTop50HostLogsByMachineName(@Param("machineName") String machineName);

    /**
     *  - host machine과 container의 모든 머신에서 가장 최근을 기준으로 최대 50개의 로그들을 리스트로 저장해 반환한다.
     * 
     * @return  최근 모든 로그들 중 최대 50개를 저장한 리스트
     */
    List<AbnormalMetricLog> findTop50ByOrderByTimestampDesc();

    @Modifying
    @Transactional
    @Query("DELETE FROM AbnormalMetricLog l WHERE l.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}
