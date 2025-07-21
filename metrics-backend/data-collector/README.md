# Crowdquake Infra Monitoring Data-Collector

## 목차

- [주요 기능 및 데이터 구조](#주요-기능-및-데이터-구조)
  - [데이터 예시](#데이터-예시)
  - [데이터 항목 및 단위 정리](#데이터-항목-및-단위-정리)
- [빌드 및 이미지 생성 방법](#빌드-및-이미지-생성-방법)
  - [프로젝트 소스 준비](#프로젝트-소스-준비)
  - [환경설정 파일env-생성](#환경설정-파일env-생성)
  - [gradle-빌드](#gradle-빌드)
  - [docker-이미지-만들기](#docker-이미지-만들기)
  - [docker-composeyml-예시-및-경로](#docker-composeyml-예시-및-경로)
  - [컨테이너-실행](#컨테이너-실행)
- [시스템 파일 경로 설명 리눅스 리소스 모니터링](#시스템-파일-경로-설명-리눅스-리소스-모니터링)
  - [주요 시스템 경로와 쓰임새](#주요-시스템-경로와-쓰임새)
  - [각 경로의 용도와 활용 요약](#각-경로의-용도와-활용-요약)
- [참고사항](#참고사항)
- [참고 및 운영 팁](#참고-및-운영-팁)
- [문의 및 이슈](#문의-및-이슈)

## 주요 기능 및 데이터 구조

이 시스템은 리눅스 호스트 및 컨테이너의 **실시간 리소스 상태**(CPU, 메모리, 디스크, 네트워크, 온도 등)와 세부 컨테이너 리소스를 통합적으로 수집하여 JSON 형태로 제공합니다.

### 데이터 예시

```json
{
  "type": "host",
  "hostId": "host-001",
  "name": "호스트 이름",
  "timeStamp": "yyyy-mm-dd HH:MM:SS",
  "cpuUsagePercent": 5.9,
  "memoryUsedBytes": 15519293440,
  "diskReadBytesDelta": 212992,
  "diskWriteBytesDelta": 605184,
  "networkDelta": {
    "eth0": {
      "txBytesDelta": 0,
      "rxBytesDelta": 0
    }
    // ...
  },
  "temperatures": {
    "x86_pkg_temp (thermal_zone0)": 46.0,
    "coretemp/Package id 0": 46.0,
    "coretemp/Core 0": 31.0,
    // ...
  },
  "containers": {
    "b3428d56af9f...": {
      "name": "app1",
      "cpuUsagePercent": 39.89,
      "memoryUsedBytes": 47386624,
      "diskReadBytesDelta": 212992,
      "diskWriteBytesDelta": 605184,
      "networkDelta": {
        "eth0": {
          "txBytesDelta": 0,
          "rxBytesDelta": 0
        }
      }
    }
    // ...
  }
}
```

### 데이터 항목 및 단위 정리

| 필드                          | 설명                                          | 단위/예시                       |
|-------------------------------|-----------------------------------------------|----------------------------------|
| `type`                        | 호스트/컨테이너 구분                          | `"host"`                         |
| `hostId`                      | 고유 호스트 식별자                            | `"host-001"`                     |
| `name`                        | 시스템(서버) 호스트 이름                      | `"knusslab-System-Product-Name"` |
| `timeStamp`                   | 데이터 측정 시각                              | `"2024-07-15 18:12:00"`          |
| `cpuUsagePercent`             | CPU 사용률 (전체)                             | `% (float)`                      |
| `memoryUsedBytes`             | 사용 중인 메모리                              | Byte (long)                      |
| `diskReadBytesDelta`          | 최근 읽은 디스크 바이트(누적값 변화량)        | Byte (long)                      |
| `diskWriteBytesDelta`         | 최근 쓴 디스크 바이트(누적값 변화량)          | Byte (long)                      |
| `networkDelta`                | 네트워크 송수신량 변화 (인터페이스별)         | 하위 map (아래 참고)             |
| `temperatures`                | 온도 센서별 측정값 (센서명: 온도)             | 섭씨 °C (float/map)              |
| `containers`                  | 컨테이너별 리소스 정보 (컨테이너ID: 세부정보) | map                              |

- **networkDelta 구조**  
  `eth0`, `enp0s3` 등 네트워크 인터페이스명별로  
  - `txBytesDelta`: 전송(byte, 기간별 변화량)
  - `rxBytesDelta`: 수신(byte, 기간별 변화량)

- **temperatures 구조**  
  `"센서명"`: 센서명을 key로, 섭씨(`°C`)로 변환된 값이 value  
  예: `"coretemp/Core 0": 31.0`  
  센서명은 시스템별로 다르고, label/type 값이 정확히 매핑됨

- **containers 구조**  
  Docker 컨테이너별로 고유 ID 하위에 각종 리소스 값이 별도 저장  
  컨테이너 네트워크 및 디스크 I/O도 host와 동일 구조 제공

## 빌드 및 이미지 생성 방법

### 프로젝트 소스 준비

```bash
git clone https://github.com/knusslab/crowdquake-infra-monitoring.git
cd crowdquake-infra-monitoring
```

### 환경설정 파일env-생성

```bash
nano .env
```
- 필요한 변수들을 실제 환경에 맞게 작성

```
BOOTSTRAP_SERVER=your-kafka:9092
KAFKA_TOPIC_NAME=crowdquake-metrics
HOST_ID=host-001
HOST_NAME=knusslab-System-Product-Name
```

### gradle-빌드

```bash
./gradlew clean build -x test
```
- 빌드 후, `build/libs` 또는 하위 모듈 jar 확인

### docker-이미지-만들기

```bash
docker build -t knusslab/infra-data-collector:latest .
```
- Dockerfile 및 빌드 경로 주의  
- 커스텀 이미지 이름 사용 가능

### docker-composeyml-예시-및-경로

```yaml
services:
  data-collector:
    image: knusslab/infra-data-collector:latest
    container_name: data-collector
    user: root
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /dev:/host/dev:ro
      - /etc/hostname:/host/etc/hostname:ro
      - /sys/class/thermal:/host/sys/class/thermal:ro
      - /sys/class/hwmon:/host/sys/class/hwmon:ro
      - /proc/acpi/thermal_zone:/host/proc/acpi/thermal_zone:ro # (구형시스템 옵션)
    env_file:
      - .env
    restart: unless-stopped
```

- 필요시 볼륨, 경로 추가하여 서버 환경에 맞게 설정

### 컨테이너-실행

```bash
docker compose up -d   # 또는 docker-compose up -d
```

## 시스템-파일-경로-설명-리눅스-리소스-모니터링

### 주요-시스템-경로와-쓰임새

| 컨테이너 내부 경로                       | 호스트의 실제 경로             | 주요 데이터/역할                | 설명                                   |
|------------------------------------------|-------------------------------|---------------------------------|----------------------------------------|
| `/host/proc/stat`                        | `/proc/stat`                  | CPU 사용률 지표                  | 시스템 전체 CPU 통계(누적)             |
| `/host/proc/meminfo`                     | `/proc/meminfo`               | 메모리 전체/가용/사용량         | 시스템 메모리 정보                     |
| `/host/proc/mounts`                      | `/proc/mounts`                | 마운트된 파일시스템, 디스크 용량 | 전체 디스크와 사용량 정보              |
| `/host/proc/diskstats`                   | `/proc/diskstats`             | 디스크 I/O통계                  | 각 디스크의 입출력 통계                |
| `/host/sys/class/thermal/thermal_zone*/temp` | `/sys/class/thermal/thermal_zone*/temp` | 온도(thermal zone별)      | 커널 추상화 온도 센서                  |
| `/host/sys/class/thermal/thermal_zone*/type` | `/sys/class/thermal/thermal_zone*/type` | 센서 종류(이름 등)        | thermal zone 종류 확인                 |
| `/host/sys/class/hwmon/hwmon*/temp*_input`   | `/sys/class/hwmon/hwmon*/temp*_input`   | 온도(hw monitoring chip)   | 다양한 하드웨어 센서의 온도            |
| `/host/sys/class/hwmon/hwmon*/name`          | `/sys/class/hwmon/hwmon*/name`          | 센서 칩 이름                 | hwmon 디바이스의 고유 명칭             |
| `/host/sys/class/hwmon/hwmon*/temp*_label`   | `/sys/class/hwmon/hwmon*/temp*_label`   | 센서 온도별 라벨             | 센서별 이름(예: Core 0, Composite 등)  |
| `/host/proc/acpi/thermal_zone/*/temperature` | `/proc/acpi/thermal_zone/*/temperature` | ACPI 구형 시스템 온도        | 서버/노트북 등 구형 하드웨어 지원       |
| `/host/proc/net/dev`                        | `/proc/net/dev`                | 네트워크 인터페이스별 RX/TX   | 네트워크 트래픽 정보                   |
| `/host/etc/hostname`                        | `/etc/hostname`                 | 시스템 호스트네임              | 서버 고유 이름                         |

### 각-경로의-용도와-활용-요약

- **/host/proc/stat**: CPU 총 사용량, idle 시간 등 ("cpu 사용률" 계산에 사용)
- **/host/proc/meminfo**: 메모리 전체, 사용 가능, 사용 중 바이트
- **/host/proc/mounts**: 각 마운트 포인트(디스크) 용량/사용량/남은 용량
- **/host/proc/diskstats**: 디스크별 읽기/쓰기 누적 섹터(바이트 변환), I/O 횟수
- **/host/sys/class/thermal/**: CPU/메인보드 등 다양한 열 영역 온도 센서
- **/host/sys/class/hwmon/**: 더욱 다양한 하드웨어 온도(코어별, NVMe, GPU 등)
- **/host/proc/acpi/thermal_zone/**: ACPI 기반 구형 시스템 온도
- **/host/proc/net/dev**: 네트워크 트래픽, 각 인터페이스별 RX/TX
- **/host/etc/hostname**: 호스트 이름(식별명)
- 기타, 실제 컨테이너 리소스는 `/var/run/docker.sock` 도커 API 마운트 필요

## 참고사항

- 컨테이너는 리눅스 호스트의 시스템 파일을 **읽기 전용 마운트**로 가져와 하드웨어/VM 상태를 안전하게 분석
- 모든 경로가 반드시 호스트에 존재하지는 않으나, 일반적인 환경에서는 정보 제공됨
- 마운트 누락 시 일부 데이터가 0 또는 미표시될 수 있으니 모든 볼륨을 사용하는 것이 호환성에 유리

## 참고-및-운영-팁

- 실시간(약 5초 주기) JSON 데이터 출력, 필요시 Kafka 연동
- 온도 센서 정보 및 센서명은 하드웨어/커널/드라이버에 따라 다를 수 있음
- 각 값의 단위와 의미는 표와 예시 참고
- 컨테이너 종료/재시작, 마운트 수정 시 항상 `down→up` 절차 권장
- 센서라벨, 네트워크 인터페이스 등 확장 개발도 용이

## 문의-및-이슈

설치/사용 중 문의사항은 이슈 트래커 또는 메일로 언제든 문의 바랍니다!
