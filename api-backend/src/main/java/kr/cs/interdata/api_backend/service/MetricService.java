package kr.cs.interdata.api_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.cs.interdata.api_backend.infra.MetricWebsocketSender;
import kr.cs.interdata.api_backend.service.repository_service.MachineInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * MetricService는 Kafka Consumer로부터 수신된 메트릭 데이터를 처리하는 서비스입니다.
 * - 웹소켓으로 클라이언트에 전송
 * - 임계값(Threshold) 초과 여부 계산
 * - 수신된 메트릭 로그 출력
 */
@Service
public class MetricService {

    private final Logger logger = LoggerFactory.getLogger(MetricService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ThresholdService thresholdService;
    private final MetricWebsocketSender metricWebsocketSender;
    private final MachineInventoryService machineInventoryService;

    public MetricService(ThresholdService thresholdService,
                         MetricWebsocketSender metricWebsocketSender,
                         MachineInventoryService machineInventoryService) {
        this.thresholdService = thresholdService;
        this.metricWebsocketSender = metricWebsocketSender;
        this.machineInventoryService = machineInventoryService;
    }

    /**
     * Kafka Consumer에서 수신된 메트릭 JSON 문자열을 처리합니다.
     * 1. JSON 파싱
     * 2. 웹소켓으로 클라이언트에 전송
     * 3. 임계값 초과 여부 계산
     * 4. 로그 출력
     *
     * @param metric JSON 문자열 형태의 메트릭 데이터
     */
    public void sendMetric(String metric) {
        JsonNode metricsNode = parseJson(metric);

        // 프론트엔드에 실시간 메트릭 전송
        metricWebsocketSender.handleMessage(metricsNode);

        machineInventoryService.registerMachineIfAbsent(metric);

        // 임계값 초과 여부 계산 및 로그 전송
        thresholdService.calcThreshold(metric);

        // 메트릭의 대상 구분 후 로그 출력
        logger.info("Metrics sent to Websocket: {}", metric);
    }

    /**
     * JSON 문자열을 Jackson의 JsonNode 객체로 파싱합니다.
     * 유효하지 않은 JSON의 경우 사용자 정의 예외를 발생시킵니다.
     *
     * @param json 문자열 형태의 JSON
     * @return JsonNode 파싱 결과
     */
    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new InvalidJsonException("JSON 파싱 실패", e);
        }
    }

    /**
     * JSON 파싱 실패 시 사용되는 사용자 정의 런타임 예외
     */
    public static class InvalidJsonException extends RuntimeException {
        public InvalidJsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}