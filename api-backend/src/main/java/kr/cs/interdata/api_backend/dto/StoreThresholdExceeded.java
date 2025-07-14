package kr.cs.interdata.api_backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreThresholdExceeded {

    private String type;    // 메세지를 보낸 머신의 타입: 호스트, 컨테이너
    private String machineId;    // 메시지를 보낸 호스트/컨테이너의 머신 id
    private String machineName;
    private String metricName;  // 메트릭 이름
    private String threshold; // 임계값을 넘은 당시의 기준임계값
    private String value;   // 임계값을 넘은 값

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime timestamp; // 임계값을 넘은 시각
}
