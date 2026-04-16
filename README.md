# spring-boot-nexacro17

Nexacro 17 Grid 메타데이터를 받아 서버에서 Excel 파일을 생성하는 Spring Boot 샘플 프로젝트입니다.

## Runtime Baseline

- Java 8
- Spring Boot 2.7.18
- Maven
- MyBatis Spring Boot Starter 2.3.2
- H2 Database
- Apache POI 5.2.3

## Features

- Nexacro Grid 컬럼/밴드 메타데이터 기반 Excel 생성
- 콤보 코드/명칭 변환 지원
- 숫자/날짜 포맷 반영
- Apache POI 기반 `.xlsx` 생성

## Project Structure

- `src/main/java/com/example/nexacro`
  - `controller`: Excel 다운로드 엔드포인트
  - `service`: Excel 생성 orchestration
  - `excel`: Grid 메타데이터 기반 Excel 빌더
  - `mapper`: MyBatis mapper
  - `util`: 날짜 파싱 등 공통 유틸리티
- `src/main/resources/mapper`: MyBatis XML
- `src/main/resources/sql`: 초기 데이터 SQL
- `nexacro`: Nexacro 폼/스크립트 예제

## Build And Test

```bash
mvn test
```

특정 테스트만 실행할 때:

```bash
mvn -Dtest=DateParseUtilTest test
```

## Run

```bash
mvn spring-boot:run
```

기본 엔드포인트:

- `POST /excel/export`

## Compatibility Notes

- 이 저장소는 Java 8 호환을 기준으로 유지합니다.
- `jakarta.servlet` 대신 `javax.servlet` 기준입니다.
- Java 8 이상 문법이 아닌 `record`, switch expression, pattern matching `instanceof` 등은 사용하지 않습니다.

## Nexacro XAPI

현재 저장소에는 Nexacro 서버 라이브러리의 대체용 stub 클래스가 포함되어 있습니다. 실제 운영 환경에서는 `nexacro-xapi17.jar`를 별도로 배치해 연결해야 할 수 있습니다. 자세한 내용은 `libs/README.txt`를 참고하세요.
