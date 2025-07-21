# Producer 모듈

Kafka Producer 모듈은 메트릭 데이터를 Kafka 클러스터의 지정된 토픽으로 전송하는 역할을 담당합니다.  
이 모듈은 Spring Boot 기반으로 작성되었으며, Gson을 활용해 JSON 구조 메시지를 처리합니다.

## 목차

1. [모듈 개요](#모듈-개요)  
2. [프로듀서 구성](#프로듀서-구성)  
3. [KafkaTemplate을 이용한 메시지 전송](#kafkatemplate을-이용한-메시지-전송)  
4. [사용되는 환경 변수](#사용되는-환경-변수)  
5. [의존성](#의존성)  
6. [주요 파일 구조](#주요-파일-구조)

## 1. 모듈 개요

이 모듈은 다음과 같은 역할을 수행합니다:

- 다른 컴포넌트(예: data-collector) 또는 내부 서비스에서 전달받은 메트릭 JSON 데이터를 Kafka로 전송
- 구성된 Kafka 토픽으로 실시간 데이터 전송 수행
- 환경 변수 기반 설정 관리 (Kafka 서버 주소, 토픽명 등)

## 2. 프로듀서 구성

### 관련 파일: `KafkaProducerConfig.java`

```java
@Value("${BOOTSTRAP_SERVER}")
private String bootstrapServers;
```

- Kafka Producer 설정을 위한 Bean 등록
- KafkaTemplate에 필요한 ProducerFactory 설정 포함

## 3. KafkaTemplate을 이용한 메시지 전송

### 관련 파일: `KafkaProducerService.java`

```java
@Value("${KAFKA_TOPIC_NAME}")
private String topic_name;
```

```java
public void routeMessageBasedOnType(String jsonPayload) {
    kafkaTemplate.send(topic_name, jsonPayload);
    kafkaTemplate.flush();
}
```

- 메시지 전송의 핵심 로직
- jsonPayload를 String 형태로 Kafka로 전송
- KafkaTemplate은 Spring Kafka에서 제공하는 추상화 도구

## 4. 사용되는 환경 변수

`.env` 또는 시스템 환경변수에서 아래 설정값을 주입받습니다:

| 환경 변수명         | 설명                         | 예시                                |
|----------------------|------------------------------|-------------------------------------|
| `BOOTSTRAP_SERVER`   | Kafka 부트스트랩 서버 주소   | `kafka-service:9092`                |
| `KAFKA_TOPIC_NAME`   | 메시지를 전송할 Kafka 토픽   | `crowdquake-metrics`                |


## 5. 의존성

### 관련 설정: `build.gradle`

주요 라이브러리 목록:

- Spring Kafka (`spring-kafka`)
- Spring Boot Web (`spring-boot-starter-web`)
- Gson (`com.google.code.gson`)
- Lombok
- JUnit + Mockito (테스트용)

```groovy
dependencies {
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.google.code.gson:gson:2.10.1'
    ...
}
```

## 6. 주요 파일 구조

```
producer/
├── config/
│   └── KafkaProducerConfig.java   # KafkaTemplate 및 ProducerFactory 설정
├── service/
│   └── KafkaProducerService.java # produce/send 로직
├── ProducerApplication.java      # SpringBoot main 함수
├── build.gradle                  # Gradle 구성 파일
└── ...
```

## 기타 참고 사항

- 현재 메시지 전송 형식은 `String` 형태의 JSON임.
- JSON 직렬화는 `Gson` 사용, Jackson으로 대체 가능

## 기여 및 이슈

본 모듈에 버그, 개선 의향, 또는 Pull Request가 있다면 언제든지 [Issues](https://github.com/knusslab/crowdquake-infra-monitoring/issues) 또는 PR로 남겨 주세요.
