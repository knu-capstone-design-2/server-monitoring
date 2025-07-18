package kr.cs.interdata.api_backend.dto.history_dto;

import lombok.Getter;
import lombok.Setter;

/**
 * [임계값 이력 조회 요청용 DTO]
 * - 특정 타입(호스트/컨테이너)의 임계값 변경 이력을 조회할 때
 *   요청 본문에 넘겨주는 데이터 객체입니다.
 */
@Getter
@Setter
public class HistoryForType {
    private String type;
}
