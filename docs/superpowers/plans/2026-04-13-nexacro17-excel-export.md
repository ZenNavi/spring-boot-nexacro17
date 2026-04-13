# Nexacro 17 Grid Format Excel Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Nexacro 17 Grid 메타데이터(컬럼, 밴드, 콤보, 스타일)를 클라이언트에서 수집해 서버로 전송하면, 서버가 Oracle DB에서 전체 데이터를 조회하고 Apache POI로 Grid 형식과 일치하는 Excel 파일을 생성·반환한다.

**Architecture:** 클라이언트는 PlatformData(ds_gridMeta + ds_bandMeta)만 전송한다. 서버는 SALES_DATA(데이터), CODE_MST(콤보)를 Oracle에서 직접 조회하고, NexacroGridExcelBuilder가 멀티헤더·ComboText·TextAlign·포맷·스타일을 적용해 xlsx를 생성한다. 데이터는 클라이언트로부터 일절 받지 않는다.

**Tech Stack:** Spring Boot 2.7.18, Java 8, Maven, Oracle DB, MyBatis 2.3.x, Apache POI 5.2.3, Nexacro xapi 17, Lombok

---

## File Map

```
spring-boot-nexacro17/
├── pom.xml
├── libs/
│   └── nexacro-xapi17.jar              ← Nexacro 17 서버 라이브러리 (수동 배치)
├── src/
│   ├── main/
│   │   ├── java/com/example/nexacro/
│   │   │   ├── NexacroApplication.java
│   │   │   ├── config/
│   │   │   │   └── NexacroConfig.java          ← NexacroServiceFilter Bean
│   │   │   ├── controller/
│   │   │   │   └── ExcelExportController.java  ← POST /excel/export
│   │   │   ├── service/
│   │   │   │   └── ExcelExportService.java     ← DB 조회 + Excel 빌드 조율
│   │   │   ├── excel/
│   │   │   │   ├── ColumnMeta.java             ← 컬럼 메타 DTO
│   │   │   │   ├── BandMeta.java               ← 밴드(멀티헤더) DTO
│   │   │   │   ├── ComboResolver.java          ← code→text 변환
│   │   │   │   └── NexacroGridExcelBuilder.java ← POI Excel 생성 엔진
│   │   │   ├── mapper/
│   │   │   │   ├── SalesMapper.java
│   │   │   │   └── ComboMapper.java
│   │   │   └── dto/
│   │   │       └── CodeMst.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── mapper/
│   │       │   ├── SalesMapper.xml
│   │       │   └── ComboMapper.xml
│   │       └── sql/
│   │           └── init.sql                    ← Oracle DDL + 샘플 데이터
│   └── test/
│       └── java/com/example/nexacro/
│           ├── excel/
│           │   ├── ComboResolverTest.java
│           │   └── NexacroGridExcelBuilderTest.java
│           └── service/
│               └── ExcelExportServiceTest.java
├── nexacro/
│   └── common/
│       └── ExcelExportUtil.xjs             ← Nexacro 17 클라이언트 공통 스크립트
└── docs/
    └── superpowers/
        ├── specs/
        └── plans/
```

---

## Task 1: Git 저장소 초기화 및 프로젝트 골격 생성

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/example/nexacro/NexacroApplication.java`
- Create: `src/main/resources/application.properties`
- Create: `.gitignore`

- [ ] **Step 1: Git 저장소 초기화**

```bash
cd /Users/dongju/agents/spring-boot-nexacro17
git init
```

- [ ] **Step 2: .gitignore 생성**

```
target/
*.class
*.jar
!libs/nexacro-xapi17.jar
.idea/
*.iml
*.log
```

- [ ] **Step 3: libs 디렉토리 생성 및 안내 파일 작성**

```bash
mkdir -p libs
```

`libs/README.txt` 내용:
```
nexacro-xapi17.jar 배치 방법:
1. Nexacro 17 설치 경로의 tool/server_lib/ 또는 nexacro17/lib/ 에서 복사
2. 이 libs/ 디렉토리에 nexacro-xapi17.jar 이름으로 저장
3. Maven 로컬 설치가 필요 없음 (pom.xml에서 system scope로 참조)
```

- [ ] **Step 4: pom.xml 생성**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>nexacro17-excel-export</artifactId>
    <version>1.0.0</version>
    <name>nexacro17-excel-export</name>

    <properties>
        <java.version>1.8</java.version>
        <poi.version>5.2.3</poi.version>
        <mybatis.version>2.3.1</mybatis.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MyBatis -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis.version}</version>
        </dependency>

        <!-- Oracle JDBC -->
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc8</artifactId>
            <version>21.9.0.0</version>
        </dependency>

        <!-- Apache POI (xlsx) -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>${poi.version}</version>
        </dependency>

        <!-- Nexacro xapi (로컬 JAR) -->
        <dependency>
            <groupId>com.nexacro</groupId>
            <artifactId>xapi</artifactId>
            <version>17.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/nexacro-xapi17.jar</systemPath>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                    <includeSystemScope>true</includeSystemScope>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 5: NexacroApplication.java 생성**

`src/main/java/com/example/nexacro/NexacroApplication.java`:
```java
package com.example.nexacro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.nexacro.mapper")
public class NexacroApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexacroApplication.class, args);
    }
}
```

- [ ] **Step 6: application.properties 생성**

`src/main/resources/application.properties`:
```properties
# Server
server.port=8080

# Oracle DataSource
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=nexacro
spring.datasource.password=nexacro

# MyBatis
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true

# Log
logging.level.com.example.nexacro=DEBUG
logging.level.org.apache.poi=WARN
```

- [ ] **Step 7: 첫 번째 커밋**

```bash
git add .
git commit -m "chore: initialize Spring Boot 2.7 project with Maven, Oracle, POI, Nexacro xapi"
```

---

## Task 2: Oracle DB 스키마 및 샘플 데이터

**Files:**
- Create: `src/main/resources/sql/init.sql`

- [ ] **Step 1: init.sql 생성**

`src/main/resources/sql/init.sql`:
```sql
-- 콤보 코드 마스터
CREATE TABLE CODE_MST (
    GROUP_CD   VARCHAR2(20)  NOT NULL,
    CODE       VARCHAR2(20)  NOT NULL,
    CODE_NM    VARCHAR2(100) NOT NULL,
    SORT_ORDER NUMBER        DEFAULT 0,
    CONSTRAINT PK_CODE_MST PRIMARY KEY (GROUP_CD, CODE)
);

-- 매출 데이터
CREATE TABLE SALES_DATA (
    ID          NUMBER        NOT NULL,
    REGION_CD   VARCHAR2(10),
    CATEGORY_CD VARCHAR2(10),
    Q1_SALES    NUMBER(15),
    Q1_QTY      NUMBER(10),
    Q2_SALES    NUMBER(15),
    Q2_QTY      NUMBER(10),
    TOTAL_SALES NUMBER(15),
    REG_DATE    DATE,
    CONSTRAINT PK_SALES_DATA PRIMARY KEY (ID)
);

CREATE SEQUENCE SEQ_SALES_DATA START WITH 1 INCREMENT BY 1;

-- 지역 코드
INSERT INTO CODE_MST VALUES ('REGION', 'SEO', '서울', 1);
INSERT INTO CODE_MST VALUES ('REGION', 'BUS', '부산', 2);
INSERT INTO CODE_MST VALUES ('REGION', 'DAE', '대구', 3);
INSERT INTO CODE_MST VALUES ('REGION', 'ICH', '인천', 4);
INSERT INTO CODE_MST VALUES ('REGION', 'GWA', '광주', 5);

-- 카테고리 코드
INSERT INTO CODE_MST VALUES ('CATEGORY', 'ELEC', '전자제품', 1);
INSERT INTO CODE_MST VALUES ('CATEGORY', 'CLOT', '의류',    2);
INSERT INTO CODE_MST VALUES ('CATEGORY', 'FOOD', '식품',    3);
INSERT INTO CODE_MST VALUES ('CATEGORY', 'FURN', '가구',    4);

-- 매출 샘플 데이터
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'SEO', 'ELEC', 12000000, 150, 9800000,  120, 21800000, SYSDATE);
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'SEO', 'CLOT',  8500000, 200, 7600000,  180, 16100000, SYSDATE);
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'BUS', 'ELEC',  6300000, 100, 5200000,   90, 11500000, SYSDATE);
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'BUS', 'FOOD',  3200000, 400, 2800000,  350,  6000000, SYSDATE);
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'DAE', 'FURN',  9100000,  80, 8500000,   70, 17600000, SYSDATE);
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'ICH', 'CLOT',  4500000, 250, 3900000,  210,  8400000, SYSDATE);
INSERT INTO SALES_DATA VALUES (SEQ_SALES_DATA.NEXTVAL, 'GWA', 'FOOD',  2700000, 500, 2100000,  420,  4800000, SYSDATE);

COMMIT;
```

- [ ] **Step 2: 커밋**

```bash
git add src/main/resources/sql/
git commit -m "feat: add Oracle DDL and sample data for SALES_DATA, CODE_MST"
```

---

## Task 3: DTO 및 Excel 메타 클래스

**Files:**
- Create: `src/main/java/com/example/nexacro/excel/ColumnMeta.java`
- Create: `src/main/java/com/example/nexacro/excel/BandMeta.java`
- Create: `src/main/java/com/example/nexacro/dto/CodeMst.java`

- [ ] **Step 1: ColumnMeta.java 생성**

`src/main/java/com/example/nexacro/excel/ColumnMeta.java`:
```java
package com.example.nexacro.excel;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMeta {
    private String colId;          // Dataset 컬럼명 (DB 컬럼과 매핑)
    private String headerText;     // 헤더 표시 텍스트
    private int colWidth;          // 컬럼 너비 (px)
    private String textAlign;      // left / center / right
    private String colType;        // text / number / date
    private String numberFormat;   // #,##0 / #,##0.00 (colType=number)
    private String dateFormat;     // yyyy-MM-dd (colType=date)
    private String editType;       // text / combo
    private String comboGroupCd;   // CODE_MST.GROUP_CD (editType=combo)
    private String comboCodeCol;   // 기본값: CODE
    private String comboTextCol;   // 기본값: CODE_NM
    private String bandId;         // 소속 밴드 ID (없으면 null - 전체 헤더 높이에 rowspan)
    private String bgColor;        // HEX (#FFFFFF), null이면 기본색
    private boolean fontBold;
    private int fontSize;          // 0이면 기본 크기
    private String borderStyle;    // thin / medium / thick / none
}
```

- [ ] **Step 2: BandMeta.java 생성**

`src/main/java/com/example/nexacro/excel/BandMeta.java`:
```java
package com.example.nexacro.excel;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandMeta {
    private String bandId;      // 밴드 고유 ID (ColumnMeta.bandId와 연결)
    private String bandText;    // 밴드 헤더 표시 텍스트
    private int colSpan;        // 가로 병합 컬럼 수
    private int rowSpan;        // 세로 병합 행 수 (보통 1)
    private int bandOrder;      // 밴드 표시 순서 (오름차순)
    private String bgColor;     // HEX (#D9E1F2)
    private String textAlign;   // left / center / right
    private boolean fontBold;
}
```

- [ ] **Step 3: CodeMst.java 생성**

`src/main/java/com/example/nexacro/dto/CodeMst.java`:
```java
package com.example.nexacro.dto;

import lombok.Data;

@Data
public class CodeMst {
    private String groupCd;
    private String code;
    private String codeNm;
    private int sortOrder;
}
```

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/example/nexacro/excel/ src/main/java/com/example/nexacro/dto/
git commit -m "feat: add ColumnMeta, BandMeta, CodeMst DTOs"
```

---

## Task 4: Mapper 레이어 (MyBatis)

**Files:**
- Create: `src/main/java/com/example/nexacro/mapper/SalesMapper.java`
- Create: `src/main/resources/mapper/SalesMapper.xml`
- Create: `src/main/java/com/example/nexacro/mapper/ComboMapper.java`
- Create: `src/main/resources/mapper/ComboMapper.xml`

- [ ] **Step 1: SalesMapper.java 생성**

`src/main/java/com/example/nexacro/mapper/SalesMapper.java`:
```java
package com.example.nexacro.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesMapper {
    // 전체 조회 - 페이징 없음, 결과는 컬럼명→값 Map
    List<Map<String, Object>> selectAll();
}
```

- [ ] **Step 2: SalesMapper.xml 생성**

`src/main/resources/mapper/SalesMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.nexacro.mapper.SalesMapper">

    <!--
        col_id(ds_gridMeta)와 매핑되도록 alias를 소문자로 통일.
        MyBatis map-underscore-to-camel-case가 Map에는 적용 안 되므로 alias 사용.
    -->
    <select id="selectAll" resultType="java.util.LinkedHashMap">
        SELECT
            ID              AS id,
            REGION_CD       AS regionCd,
            CATEGORY_CD     AS categoryCd,
            Q1_SALES        AS q1Sales,
            Q1_QTY          AS q1Qty,
            Q2_SALES        AS q2Sales,
            Q2_QTY          AS q2Qty,
            TOTAL_SALES     AS totalSales,
            TO_CHAR(REG_DATE, 'YYYY-MM-DD') AS regDate
        FROM SALES_DATA
        ORDER BY ID
    </select>

</mapper>
```

- [ ] **Step 3: ComboMapper.java 생성**

`src/main/java/com/example/nexacro/mapper/ComboMapper.java`:
```java
package com.example.nexacro.mapper;

import com.example.nexacro.dto.CodeMst;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ComboMapper {
    // 특정 그룹 코드 목록 조회
    List<CodeMst> selectByGroupCds(@Param("groupCds") List<String> groupCds);
}
```

- [ ] **Step 4: ComboMapper.xml 생성**

`src/main/resources/mapper/ComboMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.nexacro.mapper.ComboMapper">

    <select id="selectByGroupCds" resultType="com.example.nexacro.dto.CodeMst">
        SELECT
            GROUP_CD   AS groupCd,
            CODE       AS code,
            CODE_NM    AS codeNm,
            SORT_ORDER AS sortOrder
        FROM CODE_MST
        WHERE GROUP_CD IN
        <foreach collection="groupCds" item="gcd" open="(" separator="," close=")">
            #{gcd}
        </foreach>
        ORDER BY GROUP_CD, SORT_ORDER
    </select>

</mapper>
```

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/example/nexacro/mapper/ src/main/resources/mapper/
git commit -m "feat: add SalesMapper and ComboMapper with MyBatis XML"
```

---

## Task 5: ComboResolver 구현 및 테스트

**Files:**
- Create: `src/main/java/com/example/nexacro/excel/ComboResolver.java`
- Create: `src/test/java/com/example/nexacro/excel/ComboResolverTest.java`

- [ ] **Step 1: ComboResolverTest.java 작성 (TDD - 먼저 실패 확인)**

`src/test/java/com/example/nexacro/excel/ComboResolverTest.java`:
```java
package com.example.nexacro.excel;

import com.example.nexacro.dto.CodeMst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ComboResolverTest {

    private ComboResolver resolver;

    @BeforeEach
    void setUp() {
        List<CodeMst> codes = Arrays.asList(
            code("REGION", "SEO", "서울"),
            code("REGION", "BUS", "부산"),
            code("CATEGORY", "ELEC", "전자제품"),
            code("CATEGORY", "CLOT", "의류")
        );
        resolver = new ComboResolver(codes);
    }

    @Test
    void resolves_known_code_to_text() {
        assertThat(resolver.resolve("REGION", "SEO")).isEqualTo("서울");
        assertThat(resolver.resolve("CATEGORY", "ELEC")).isEqualTo("전자제품");
    }

    @Test
    void returns_code_as_fallback_when_not_found() {
        assertThat(resolver.resolve("REGION", "UNKNOWN")).isEqualTo("UNKNOWN");
        assertThat(resolver.resolve("NO_GROUP", "SEO")).isEqualTo("SEO");
    }

    @Test
    void returns_null_when_code_value_is_null() {
        assertThat(resolver.resolve("REGION", null)).isNull();
    }

    @Test
    void returns_code_when_group_is_null() {
        assertThat(resolver.resolve(null, "SEO")).isEqualTo("SEO");
    }

    private CodeMst code(String groupCd, String code, String codeNm) {
        CodeMst c = new CodeMst();
        c.setGroupCd(groupCd);
        c.setCode(code);
        c.setCodeNm(codeNm);
        return c;
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
cd /Users/dongju/agents/spring-boot-nexacro17
mvn test -pl . -Dtest=ComboResolverTest -q 2>&1 | tail -5
```
Expected: `COMPILATION ERROR` (클래스 없음)

- [ ] **Step 3: ComboResolver.java 구현**

`src/main/java/com/example/nexacro/excel/ComboResolver.java`:
```java
package com.example.nexacro.excel;

import com.example.nexacro.dto.CodeMst;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComboResolver {

    // Map<groupCd, Map<code, codeNm>>
    private final Map<String, Map<String, String>> comboMap;

    public ComboResolver(List<CodeMst> codeList) {
        comboMap = new HashMap<>();
        if (codeList == null) return;
        for (CodeMst item : codeList) {
            comboMap
                .computeIfAbsent(item.getGroupCd(), k -> new LinkedHashMap<>())
                .put(item.getCode(), item.getCodeNm());
        }
    }

    /**
     * groupCd + codeValue로 표시 텍스트 반환.
     * 매핑 없으면 codeValue 그대로 반환 (graceful fallback).
     */
    public String resolve(String groupCd, String codeValue) {
        if (codeValue == null) return null;
        if (groupCd == null) return codeValue;
        Map<String, String> codeMap = comboMap.get(groupCd);
        if (codeMap == null) return codeValue;
        return codeMap.getOrDefault(codeValue, codeValue);
    }
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
mvn test -Dtest=ComboResolverTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/example/nexacro/excel/ComboResolver.java \
        src/test/java/com/example/nexacro/excel/ComboResolverTest.java
git commit -m "feat: add ComboResolver with code→text mapping from CODE_MST"
```

---

## Task 6: NexacroGridExcelBuilder - 스타일 헬퍼 및 헤더 생성

**Files:**
- Create: `src/main/java/com/example/nexacro/excel/NexacroGridExcelBuilder.java`
- Create: `src/test/java/com/example/nexacro/excel/NexacroGridExcelBuilderTest.java`

- [ ] **Step 1: NexacroGridExcelBuilderTest.java 작성 (헤더 검증)**

`src/test/java/com/example/nexacro/excel/NexacroGridExcelBuilderTest.java`:
```java
package com.example.nexacro.excel;

import com.example.nexacro.dto.CodeMst;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class NexacroGridExcelBuilderTest {

    private final NexacroGridExcelBuilder builder = new NexacroGridExcelBuilder();

    @Test
    void single_header_row_when_no_bands() throws Exception {
        List<ColumnMeta> columns = Arrays.asList(
            column("name", "이름", "left"),
            column("age",  "나이", "center")
        );
        ComboResolver resolver = new ComboResolver(Collections.emptyList());

        byte[] bytes = builder.build(columns, Collections.emptyList(),
                                     Collections.emptyList(), resolver);

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("이름");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("나이");
        assertThat(sheet.getRow(1)).isNull(); // 데이터 없음, 헤더 1행만
        wb.close();
    }

    @Test
    void two_header_rows_when_bands_present() throws Exception {
        List<BandMeta> bands = Collections.singletonList(
            band("B1", "1분기", 2, 1)
        );
        List<ColumnMeta> columns = Arrays.asList(
            columnWithBand("q1Sales", "매출액", "right", "B1"),
            columnWithBand("q1Qty",   "수량",   "center", "B1")
        );
        ComboResolver resolver = new ComboResolver(Collections.emptyList());

        byte[] bytes = builder.build(columns, bands, Collections.emptyList(), resolver);

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        // Row 0: 밴드 헤더
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("1분기");
        // Row 1: 컬럼 헤더
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("매출액");
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("수량");
        wb.close();
    }

    @Test
    void combo_text_is_resolved_in_data_rows() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            comboColumn("regionCd", "지역", "REGION")
        );
        List<CodeMst> codes = Collections.singletonList(code("REGION", "SEO", "서울"));
        ComboResolver resolver = new ComboResolver(codes);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("regionCd", "SEO");
        byte[] bytes = builder.build(columns, Collections.emptyList(),
                                     Collections.singletonList(row), resolver);

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        // Row 0: 헤더, Row 1: 데이터
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("서울");
        wb.close();
    }

    @Test
    void number_column_uses_numeric_cell_type() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            numberColumn("q1Sales", "매출액")
        );
        ComboResolver resolver = new ComboResolver(Collections.emptyList());

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("q1Sales", 12000000L);
        byte[] bytes = builder.build(columns, Collections.emptyList(),
                                     Collections.singletonList(row), resolver);

        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = wb.getSheetAt(0);
        Cell cell = sheet.getRow(1).getCell(0);
        assertThat(cell.getCellType()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(12000000.0);
        wb.close();
    }

    // --- 헬퍼 ---

    private ColumnMeta column(String id, String header, String align) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign(align).colType("text").colWidth(100).fontSize(10)
                .borderStyle("thin").build();
    }

    private ColumnMeta columnWithBand(String id, String header, String align, String bandId) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign(align).colType("text").colWidth(100)
                .bandId(bandId).fontSize(10).borderStyle("thin").build();
    }

    private ColumnMeta comboColumn(String id, String header, String groupCd) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign("left").colType("text").editType("combo")
                .comboGroupCd(groupCd).colWidth(100).fontSize(10)
                .borderStyle("thin").build();
    }

    private ColumnMeta numberColumn(String id, String header) {
        return ColumnMeta.builder().colId(id).headerText(header)
                .textAlign("right").colType("number").numberFormat("#,##0")
                .colWidth(120).fontSize(10).borderStyle("thin").build();
    }

    private BandMeta band(String id, String text, int colSpan, int rowSpan) {
        return BandMeta.builder().bandId(id).bandText(text)
                .colSpan(colSpan).rowSpan(rowSpan).bandOrder(1)
                .bgColor("#D9E1F2").textAlign("center").fontBold(true).build();
    }

    private CodeMst code(String groupCd, String code, String codeNm) {
        CodeMst c = new CodeMst();
        c.setGroupCd(groupCd); c.setCode(code); c.setCodeNm(codeNm);
        return c;
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
mvn test -Dtest=NexacroGridExcelBuilderTest -q 2>&1 | tail -5
```
Expected: `COMPILATION ERROR`

- [ ] **Step 3: NexacroGridExcelBuilder.java 구현**

`src/main/java/com/example/nexacro/excel/NexacroGridExcelBuilder.java`:
```java
package com.example.nexacro.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Component
public class NexacroGridExcelBuilder {

    /**
     * Grid 메타데이터 + 데이터 → xlsx 바이너리
     */
    public byte[] build(List<ColumnMeta> columns, List<BandMeta> bands,
                        List<Map<String, Object>> dataRows,
                        ComboResolver comboResolver) throws Exception {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            boolean hasBands = bands != null && !bands.isEmpty();

            // 헤더 생성
            int dataStartRow = createHeader(workbook, sheet, columns, bands, hasBands);

            // 데이터 행 생성
            createDataRows(workbook, sheet, columns, dataRows, comboResolver, dataStartRow);

            // 컬럼 너비 설정
            setColumnWidths(sheet, columns);

            // 헤더 고정
            sheet.createFreezePane(0, dataStartRow);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── 헤더 ─────────────────────────────────────────────────────────

    private int createHeader(XSSFWorkbook wb, Sheet sheet,
                             List<ColumnMeta> columns, List<BandMeta> bands,
                             boolean hasBands) {
        if (!hasBands) {
            createSingleHeaderRow(wb, sheet, columns);
            return 1;
        }
        createBandHeaderRow(wb, sheet, columns, bands);
        createColumnHeaderRow(wb, sheet, columns);
        return 2;
    }

    private void createSingleHeaderRow(XSSFWorkbook wb, Sheet sheet,
                                       List<ColumnMeta> columns) {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(20);
        for (int i = 0; i < columns.size(); i++) {
            ColumnMeta col = columns.get(i);
            Cell cell = row.createCell(i);
            cell.setCellValue(col.getHeaderText());
            cell.setCellStyle(buildHeaderStyle(wb, col));
        }
    }

    private void createBandHeaderRow(XSSFWorkbook wb, Sheet sheet,
                                     List<ColumnMeta> columns, List<BandMeta> bands) {
        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(20);

        // band 순서 정렬
        List<BandMeta> sortedBands = new ArrayList<>(bands);
        sortedBands.sort(Comparator.comparingInt(BandMeta::getBandOrder));

        int colIdx = 0;
        for (ColumnMeta col : columns) {
            if (col.getBandId() == null) {
                // 밴드 없는 컬럼: 2행 병합
                Cell cell = row0.createCell(colIdx);
                cell.setCellValue(col.getHeaderText());
                cell.setCellStyle(buildHeaderStyle(wb, col));
                sheet.addMergedRegion(new CellRangeAddress(0, 1, colIdx, colIdx));
                colIdx++;
            } else {
                // 밴드 소속 컬럼: 밴드 헤더에서 처리
                BandMeta band = sortedBands.stream()
                    .filter(b -> b.getBandId().equals(col.getBandId()))
                    .findFirst().orElse(null);
                if (band != null && !isBandAlreadyPlaced(sheet, colIdx, row0)) {
                    Cell bandCell = row0.createCell(colIdx);
                    bandCell.setCellValue(band.getBandText());
                    bandCell.setCellStyle(buildBandStyle(wb, band));
                    if (band.getColSpan() > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(
                            0, 0, colIdx, colIdx + band.getColSpan() - 1));
                    }
                }
                colIdx++;
            }
        }
    }

    private boolean isBandAlreadyPlaced(Sheet sheet, int colIdx, Row row) {
        Cell cell = row.getCell(colIdx);
        return cell != null && !cell.getStringCellValue().isEmpty();
    }

    private void createColumnHeaderRow(XSSFWorkbook wb, Sheet sheet,
                                       List<ColumnMeta> columns) {
        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(18);
        for (int i = 0; i < columns.size(); i++) {
            ColumnMeta col = columns.get(i);
            if (col.getBandId() != null) {
                // 밴드 소속 컬럼만 2번째 헤더 행에 표시
                Cell cell = row1.createCell(i);
                cell.setCellValue(col.getHeaderText());
                cell.setCellStyle(buildHeaderStyle(wb, col));
            }
        }
    }

    // ── 데이터 행 ─────────────────────────────────────────────────────

    private void createDataRows(XSSFWorkbook wb, Sheet sheet,
                                List<ColumnMeta> columns,
                                List<Map<String, Object>> dataRows,
                                ComboResolver comboResolver,
                                int startRowIdx) {
        // 컬럼별 스타일 캐시 (CellStyle 재사용 - POI 제한 65536개)
        Map<String, CellStyle> styleCache = new HashMap<>();

        for (int r = 0; r < dataRows.size(); r++) {
            Row row = sheet.createRow(startRowIdx + r);
            row.setHeightInPoints(16);
            Map<String, Object> data = dataRows.get(r);

            for (int c = 0; c < columns.size(); c++) {
                ColumnMeta col = columns.get(c);
                Cell cell = row.createCell(c);
                Object value = data.get(col.getColId());

                setCellValue(wb, cell, col, value, comboResolver);
                cell.setCellStyle(getOrCreateDataStyle(wb, col, styleCache));
            }
        }
    }

    private void setCellValue(XSSFWorkbook wb, Cell cell, ColumnMeta col,
                              Object value, ComboResolver comboResolver) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        if ("combo".equals(col.getEditType())) {
            String text = comboResolver.resolve(col.getComboGroupCd(), value.toString());
            cell.setCellValue(text);
        } else if ("number".equals(col.getColType())) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                try { cell.setCellValue(Double.parseDouble(value.toString())); }
                catch (NumberFormatException e) { cell.setCellValue(value.toString()); }
            }
        } else {
            cell.setCellValue(value.toString());
        }
    }

    // ── 스타일 ──────────────────────────────────────────────────────

    private CellStyle buildHeaderStyle(XSSFWorkbook wb, ColumnMeta col) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) (col.getFontSize() > 0 ? col.getFontSize() : 10));
        style.setFont(font);
        style.setAlignment(toHAlign("center"));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, "thin");
        applyBgColor(style, col.getBgColor() != null ? col.getBgColor() : "#BDD7EE");
        style.setWrapText(false);
        return style;
    }

    private CellStyle buildBandStyle(XSSFWorkbook wb, BandMeta band) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(band.isFontBold());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(toHAlign(band.getTextAlign()));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, "thin");
        applyBgColor(style, band.getBgColor() != null ? band.getBgColor() : "#D9E1F2");
        return style;
    }

    private CellStyle getOrCreateDataStyle(XSSFWorkbook wb, ColumnMeta col,
                                           Map<String, CellStyle> cache) {
        String key = col.getColId() + "|" + col.getTextAlign() + "|"
                   + col.getNumberFormat() + "|" + col.getBgColor()
                   + "|" + col.isFontBold() + "|" + col.getBorderStyle();
        return cache.computeIfAbsent(key, k -> buildDataStyle(wb, col));
    }

    private CellStyle buildDataStyle(XSSFWorkbook wb, ColumnMeta col) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(col.isFontBold());
        font.setFontHeightInPoints((short) (col.getFontSize() > 0 ? col.getFontSize() : 10));
        style.setFont(font);
        style.setAlignment(toHAlign(col.getTextAlign()));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style, col.getBorderStyle());
        if (col.getBgColor() != null) applyBgColor(style, col.getBgColor());
        if ("number".equals(col.getColType()) && col.getNumberFormat() != null) {
            DataFormat dataFormat = wb.createDataFormat();
            style.setDataFormat(dataFormat.getFormat(col.getNumberFormat()));
        }
        return style;
    }

    private HorizontalAlignment toHAlign(String align) {
        if (align == null) return HorizontalAlignment.LEFT;
        switch (align.toLowerCase()) {
            case "center": return HorizontalAlignment.CENTER;
            case "right":  return HorizontalAlignment.RIGHT;
            default:       return HorizontalAlignment.LEFT;
        }
    }

    private void applyBorder(XSSFCellStyle style, String borderStyle) {
        BorderStyle bs = "none".equals(borderStyle) ? BorderStyle.NONE : BorderStyle.THIN;
        if ("medium".equals(borderStyle)) bs = BorderStyle.MEDIUM;
        if ("thick".equals(borderStyle))  bs = BorderStyle.THICK;
        style.setBorderTop(bs);
        style.setBorderBottom(bs);
        style.setBorderLeft(bs);
        style.setBorderRight(bs);
    }

    private void applyBgColor(XSSFCellStyle style, String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) return;
        try {
            Color c = Color.decode(hexColor);
            XSSFColor xssfColor = new XSSFColor(c, null);
            style.setFillForegroundColor(xssfColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } catch (NumberFormatException ignored) {}
    }

    // ── 컬럼 너비 ─────────────────────────────────────────────────────

    private void setColumnWidths(Sheet sheet, List<ColumnMeta> columns) {
        for (int i = 0; i < columns.size(); i++) {
            // Nexacro px * 36 ≈ POI 단위 (1/256 character width)
            int width = columns.get(i).getColWidth();
            sheet.setColumnWidth(i, width <= 0 ? 3000 : width * 36);
        }
    }
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
mvn test -Dtest=NexacroGridExcelBuilderTest -q 2>&1 | tail -10
```
Expected: `BUILD SUCCESS`, 4 tests passed

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/example/nexacro/excel/NexacroGridExcelBuilder.java \
        src/test/java/com/example/nexacro/excel/NexacroGridExcelBuilderTest.java
git commit -m "feat: implement NexacroGridExcelBuilder with multi-header, combo, style support"
```

---

## Task 7: ExcelExportService 구현

**Files:**
- Create: `src/main/java/com/example/nexacro/service/ExcelExportService.java`
- Create: `src/test/java/com/example/nexacro/service/ExcelExportServiceTest.java`

- [ ] **Step 1: ExcelExportServiceTest.java 작성**

`src/test/java/com/example/nexacro/service/ExcelExportServiceTest.java`:
```java
package com.example.nexacro.service;

import com.example.nexacro.dto.CodeMst;
import com.example.nexacro.excel.*;
import com.example.nexacro.mapper.ComboMapper;
import com.example.nexacro.mapper.SalesMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock SalesMapper salesMapper;
    @Mock ComboMapper comboMapper;
    @Mock NexacroGridExcelBuilder excelBuilder;
    @InjectMocks ExcelExportService service;

    @Test
    void exportExcel_returns_bytes_from_builder() throws Exception {
        List<ColumnMeta> columns = Collections.singletonList(
            ColumnMeta.builder().colId("regionCd").headerText("지역")
                .editType("combo").comboGroupCd("REGION")
                .colType("text").textAlign("left").colWidth(100)
                .fontSize(10).borderStyle("thin").build()
        );
        List<BandMeta> bands = Collections.emptyList();

        CodeMst code = new CodeMst();
        code.setGroupCd("REGION"); code.setCode("SEO"); code.setCodeNm("서울");

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("regionCd", "SEO");

        when(comboMapper.selectByGroupCds(anyList()))
            .thenReturn(Collections.singletonList(code));
        when(salesMapper.selectAll())
            .thenReturn(Collections.singletonList(row));
        when(excelBuilder.build(anyList(), anyList(), anyList(),
                                 org.mockito.ArgumentMatchers.any(ComboResolver.class)))
            .thenReturn(new byte[]{1, 2, 3});

        byte[] result = service.exportExcel(columns, bands);

        assertThat(result).isNotEmpty();
        assertThat(result).isEqualTo(new byte[]{1, 2, 3});
    }
}
```

- [ ] **Step 2: 테스트 실행 - 실패 확인**

```bash
mvn test -Dtest=ExcelExportServiceTest -q 2>&1 | tail -5
```
Expected: `COMPILATION ERROR`

- [ ] **Step 3: ExcelExportService.java 구현**

`src/main/java/com/example/nexacro/service/ExcelExportService.java`:
```java
package com.example.nexacro.service;

import com.example.nexacro.dto.CodeMst;
import com.example.nexacro.excel.*;
import com.example.nexacro.mapper.ComboMapper;
import com.example.nexacro.mapper.SalesMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final SalesMapper salesMapper;
    private final ComboMapper comboMapper;
    private final NexacroGridExcelBuilder excelBuilder;

    public byte[] exportExcel(List<ColumnMeta> columns, List<BandMeta> bands) throws Exception {
        // 1. combo 컬럼의 groupCd 목록 추출 (중복 제거)
        List<String> comboGroupCds = columns.stream()
            .filter(c -> "combo".equals(c.getEditType()) && c.getComboGroupCd() != null)
            .map(ColumnMeta::getComboGroupCd)
            .distinct()
            .collect(Collectors.toList());

        // 2. CODE_MST에서 콤보 전체 조회 → ComboResolver 초기화
        List<CodeMst> codeList = comboGroupCds.isEmpty()
            ? Collections.emptyList()
            : comboMapper.selectByGroupCds(comboGroupCds);
        ComboResolver comboResolver = new ComboResolver(codeList);
        log.debug("Combo groups loaded: {}, codes: {}", comboGroupCds.size(), codeList.size());

        // 3. SALES_DATA 전체 조회 (페이징 없음)
        List<Map<String, Object>> dataRows = salesMapper.selectAll();
        log.debug("Data rows fetched: {}", dataRows.size());

        // 4. Excel 생성
        return excelBuilder.build(columns, bands, dataRows, comboResolver);
    }
}
```

- [ ] **Step 4: 테스트 실행 - 통과 확인**

```bash
mvn test -Dtest=ExcelExportServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/example/nexacro/service/ \
        src/test/java/com/example/nexacro/service/
git commit -m "feat: add ExcelExportService orchestrating DB queries and Excel generation"
```

---

## Task 8: Nexacro PlatformData 파싱 및 ExcelExportController

**Files:**
- Create: `src/main/java/com/example/nexacro/config/NexacroConfig.java`
- Create: `src/main/java/com/example/nexacro/controller/ExcelExportController.java`

- [ ] **Step 1: NexacroConfig.java 생성**

`src/main/java/com/example/nexacro/config/NexacroConfig.java`:
```java
package com.example.nexacro.config;

import com.nexacro.xapi.spring.NexacroServiceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.servlet.Filter;

@Configuration
public class NexacroConfig {

    /**
     * Nexacro ServiceFilter: /excel/export 제외하고 등록
     * Excel Export는 binary response이므로 필터 바이패스
     */
    @Bean
    public FilterRegistrationBean<Filter> nexacroServiceFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new NexacroServiceFilter());
        registration.addUrlPatterns("/nexacro/*");
        registration.setName("nexacroServiceFilter");
        registration.setOrder(1);
        return registration;
    }
}
```

- [ ] **Step 2: ExcelExportController.java 생성**

`src/main/java/com/example/nexacro/controller/ExcelExportController.java`:
```java
package com.example.nexacro.controller;

import com.example.nexacro.excel.BandMeta;
import com.example.nexacro.excel.ColumnMeta;
import com.example.nexacro.service.ExcelExportService;
import com.nexacro.xapi.data.DataSet;
import com.nexacro.xapi.data.PlatformData;
import com.nexacro.xapi.tx.HttpPlatformRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    @PostMapping("/export")
    public void exportExcel(HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        // 1. PlatformData 파싱
        HttpPlatformRequest platformRequest = new HttpPlatformRequest(request);
        platformRequest.receiveData();
        PlatformData platformData = platformRequest.getData();

        DataSet dsGridMeta = platformData.getDataSet("ds_gridMeta");
        DataSet dsBandMeta = platformData.getDataSet("ds_bandMeta");

        if (dsGridMeta == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ds_gridMeta is required");
            return;
        }

        // 2. DataSet → DTO 변환
        List<ColumnMeta> columns = parseColumnMeta(dsGridMeta);
        List<BandMeta> bands = dsBandMeta != null ? parseBandMeta(dsBandMeta) : new ArrayList<>();

        log.debug("Export request - columns: {}, bands: {}", columns.size(), bands.size());

        // 3. Excel 생성
        byte[] excelBytes = excelExportService.exportExcel(columns, bands);

        // 4. 바이너리 응답
        String filename = URLEncoder.encode("sales_export_" + LocalDate.now() + ".xlsx",
                                            StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    private List<ColumnMeta> parseColumnMeta(DataSet ds) {
        List<ColumnMeta> list = new ArrayList<>();
        for (int r = 0; r < ds.getRowCount(); r++) {
            list.add(ColumnMeta.builder()
                .colId(ds.getString(r, "col_id"))
                .headerText(ds.getString(r, "header_text"))
                .colWidth(ds.getInt(r, "col_width"))
                .textAlign(ds.getString(r, "text_align"))
                .colType(ds.getString(r, "col_type"))
                .numberFormat(ds.getString(r, "number_format"))
                .dateFormat(ds.getString(r, "date_format"))
                .editType(ds.getString(r, "edit_type"))
                .comboGroupCd(ds.getString(r, "combo_group_cd"))
                .comboCodeCol(ds.getString(r, "combo_code_col"))
                .comboTextCol(ds.getString(r, "combo_text_col"))
                .bandId(ds.getString(r, "band_id"))
                .bgColor(ds.getString(r, "bg_color"))
                .fontBold("true".equalsIgnoreCase(ds.getString(r, "font_bold")))
                .fontSize(ds.getInt(r, "font_size"))
                .borderStyle(ds.getString(r, "border_style"))
                .build());
        }
        return list;
    }

    private List<BandMeta> parseBandMeta(DataSet ds) {
        List<BandMeta> list = new ArrayList<>();
        for (int r = 0; r < ds.getRowCount(); r++) {
            list.add(BandMeta.builder()
                .bandId(ds.getString(r, "band_id"))
                .bandText(ds.getString(r, "band_text"))
                .colSpan(ds.getInt(r, "col_span"))
                .rowSpan(ds.getInt(r, "row_span"))
                .bandOrder(ds.getInt(r, "band_order"))
                .bgColor(ds.getString(r, "bg_color"))
                .textAlign(ds.getString(r, "text_align"))
                .fontBold("true".equalsIgnoreCase(ds.getString(r, "font_bold")))
                .build());
        }
        return list;
    }
}
```

- [ ] **Step 3: 커밋**

```bash
git add src/main/java/com/example/nexacro/config/ \
        src/main/java/com/example/nexacro/controller/
git commit -m "feat: add ExcelExportController with PlatformData parsing and binary response"
```

---

## Task 9: Nexacro 17 클라이언트 공통 스크립트

**Files:**
- Create: `nexacro/common/ExcelExportUtil.xjs`
- Create: `nexacro/ExcelExport.xfdl` (샘플 화면 구조 주석)

- [ ] **Step 1: ExcelExportUtil.xjs 생성**

`nexacro/common/ExcelExportUtil.xjs`:
```javascript
/**
 * ExcelExportUtil.xjs
 * Nexacro 17 Grid → Excel Export 공통 유틸리티
 *
 * 사용법:
 *   fn_exportExcel(this.grdSales, "/excel/export");
 */

/**
 * Grid 메타데이터를 수집해 서버로 PlatformData 전송 후 Excel 다운로드.
 * @param {nexacro.Grid} grd - 대상 Grid 컴포넌트
 * @param {string} svcUrl   - 서버 URL (예: "/excel/export")
 */
function fn_exportExcel(grd, svcUrl) {
    if (!grd) {
        alert("Grid 객체가 없습니다.");
        return;
    }

    var oPlatformData = new nexacro.PlatformData();

    // ds_gridMeta 구성
    var dsGridMeta = _buildGridMetaDataset(grd);
    oPlatformData.addDataSet(dsGridMeta);

    // ds_bandMeta 구성 (Band가 있을 때만)
    var dsBandMeta = _buildBandMetaDataset(grd);
    oPlatformData.addDataSet(dsBandMeta);

    // HTTP POST 전송 (PlatformData binary → binary 응답)
    _sendExcelRequest(svcUrl, oPlatformData);
}

/**
 * Grid 컬럼 메타데이터 Dataset 구성
 */
function _buildGridMetaDataset(grd) {
    var ds = new nexacro.Dataset();
    ds.id = "ds_gridMeta";
    ds.addColumn("col_id",         "STRING", 256);
    ds.addColumn("header_text",    "STRING", 256);
    ds.addColumn("col_width",      "INT");
    ds.addColumn("text_align",     "STRING", 10);
    ds.addColumn("col_type",       "STRING", 20);
    ds.addColumn("number_format",  "STRING", 50);
    ds.addColumn("date_format",    "STRING", 50);
    ds.addColumn("edit_type",      "STRING", 20);
    ds.addColumn("combo_group_cd", "STRING", 50);
    ds.addColumn("combo_code_col", "STRING", 50);
    ds.addColumn("combo_text_col", "STRING", 50);
    ds.addColumn("band_id",        "STRING", 50);
    ds.addColumn("bg_color",       "STRING", 10);
    ds.addColumn("font_bold",      "STRING", 5);
    ds.addColumn("font_size",      "INT");
    ds.addColumn("border_style",   "STRING", 10);

    var nBodyRowCount = grd.getBodyRowCount();  // 보통 1
    var nColCount     = grd.getColCount();

    for (var col = 0; col < nColCount; col++) {
        var nRow = ds.addRow();

        // body 셀에서 bind 컬럼 ID 추출
        var sColId = grd.getCellProperty("body", 0, col, "text");
        // "expr:dataset.column" 형태에서 컬럼명만 추출
        if (sColId && sColId.indexOf(".") > -1) {
            sColId = sColId.substring(sColId.lastIndexOf(".") + 1);
        }
        ds.setColumn(nRow, "col_id",   sColId);

        // 헤더 텍스트
        var sHeader = grd.getCellProperty("head", 0, col, "text");
        ds.setColumn(nRow, "header_text", sHeader || sColId);

        // 너비
        ds.setColumn(nRow, "col_width", grd.getCellProperty("body", 0, col, "size").split(",")[0]);

        // 텍스트 정렬
        var sHAlign = grd.getCellProperty("body", 0, col, "halign") || "left";
        ds.setColumn(nRow, "text_align", sHAlign);

        // editType (combo 여부)
        var sEditType = grd.getCellProperty("body", 0, col, "edittype") || "text";
        ds.setColumn(nRow, "edit_type", sEditType);

        if (sEditType === "combo") {
            // 콤보 정보
            var sCodeDs   = grd.getCellProperty("body", 0, col, "codecolumn");
            var sCodeCol  = grd.getCellProperty("body", 0, col, "codeitem") || "CODE";
            var sTextCol  = grd.getCellProperty("body", 0, col, "textitem") || "CODE_NM";
            // codeDs에서 group_cd 추출 (Dataset ID가 group_cd와 동일한 규칙 사용)
            ds.setColumn(nRow, "combo_group_cd", sCodeDs);
            ds.setColumn(nRow, "combo_code_col", sCodeCol);
            ds.setColumn(nRow, "combo_text_col", sTextCol);
            ds.setColumn(nRow, "col_type",  "text");
        } else {
            // 숫자/날짜 포맷
            var sMask = grd.getCellProperty("body", 0, col, "mask") || "";
            if (sMask.indexOf("#") > -1 || sMask.indexOf("0") > -1) {
                ds.setColumn(nRow, "col_type",      "number");
                ds.setColumn(nRow, "number_format", sMask || "#,##0");
            } else if (sMask.indexOf("yyyy") > -1) {
                ds.setColumn(nRow, "col_type",    "date");
                ds.setColumn(nRow, "date_format", sMask);
            } else {
                ds.setColumn(nRow, "col_type", "text");
            }
        }

        // Band ID
        var sBandId = grd.getCellProperty("body", 0, col, "bandid") || "";
        ds.setColumn(nRow, "band_id", sBandId);

        // 스타일
        ds.setColumn(nRow, "bg_color",     grd.getCellProperty("body", 0, col, "background") || "");
        ds.setColumn(nRow, "font_bold",    grd.getCellProperty("body", 0, col, "font-weight") === "bold" ? "true" : "false");
        ds.setColumn(nRow, "font_size",    11);
        ds.setColumn(nRow, "border_style", "thin");
    }
    return ds;
}

/**
 * 밴드(멀티헤더) 메타데이터 Dataset 구성
 */
function _buildBandMetaDataset(grd) {
    var ds = new nexacro.Dataset();
    ds.id = "ds_bandMeta";
    ds.addColumn("band_id",    "STRING", 50);
    ds.addColumn("band_text",  "STRING", 256);
    ds.addColumn("col_span",   "INT");
    ds.addColumn("row_span",   "INT");
    ds.addColumn("band_order", "INT");
    ds.addColumn("bg_color",   "STRING", 10);
    ds.addColumn("text_align", "STRING", 10);
    ds.addColumn("font_bold",  "STRING", 5);

    // head 행이 2행이면 밴드 존재로 판단
    // 0번 행: 밴드 헤더, 1번 행: 컬럼 헤더
    if (grd.getHeadRowCount() < 2) return ds;

    var processedBands = {};
    var nBandOrder = 1;
    var nColCount = grd.getColCount();

    for (var col = 0; col < nColCount; col++) {
        var sBandId   = grd.getCellProperty("body", 0, col, "bandid") || "";
        var sBandText = grd.getCellProperty("head", 0, col, "text")   || "";

        if (sBandId && !processedBands[sBandId]) {
            var nColSpan = grd.getCellProperty("head", 0, col, "colspan") || 1;
            var nRow = ds.addRow();
            ds.setColumn(nRow, "band_id",    sBandId);
            ds.setColumn(nRow, "band_text",  sBandText);
            ds.setColumn(nRow, "col_span",   nColSpan);
            ds.setColumn(nRow, "row_span",   1);
            ds.setColumn(nRow, "band_order", nBandOrder++);
            ds.setColumn(nRow, "bg_color",   "#D9E1F2");
            ds.setColumn(nRow, "text_align", "center");
            ds.setColumn(nRow, "font_bold",  "true");
            processedBands[sBandId] = true;
        }
    }
    return ds;
}

/**
 * PlatformData를 서버로 POST → 바이너리 응답을 파일 다운로드
 */
function _sendExcelRequest(svcUrl, oPlatformData) {
    // PlatformData를 텍스트로 직렬화
    var sPlatformText = oPlatformData.saveToString();

    // Nexacro HttpRequestObject로 전송
    var oHttp = nexacro.util.createXMLHttpRequest();
    oHttp.open("POST", svcUrl, false);  // 동기 요청
    oHttp.setRequestHeader("Content-Type", "application/xml; charset=utf-8");
    oHttp.setRequestHeader("X-Requested-With", "XMLHttpRequest");
    oHttp.send(sPlatformText);

    if (oHttp.status === 200) {
        // 바이너리 응답을 Blob으로 변환 후 다운로드
        var blob = new Blob([oHttp.response], {
            type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        });
        var url = URL.createObjectURL(blob);
        var a   = document.createElement("a");
        a.href  = url;
        a.download = "export.xlsx";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    } else {
        alert("Excel 다운로드 실패: " + oHttp.status);
    }
}
```

- [ ] **Step 2: ExcelExport.xfdl 샘플 구조 주석 파일 생성**

`nexacro/ExcelExport.xfdl.txt` (Nexacro IDE에서 xfdl로 열기):
```
Nexacro 17 샘플 화면 구조 (ExcelExport.xfdl)

Dataset: ds_sales
  bind: grdSales

Grid: grdSales
  head row count: 2 (멀티헤더)
  Band 설정:
    Band "BAND_Q1" : head row 0, col 2~3, text="1분기"
    Band "BAND_Q2" : head row 0, col 4~5, text="2분기"

  Columns:
    Col 0: bind=regionCd,   head="지역",    editType=combo, codeDs=REGION, halign=left
    Col 1: bind=categoryCd, head="카테고리", editType=combo, codeDs=CATEGORY, halign=left
    Col 2: bind=q1Sales,    head="매출액",   mask=#,##0, halign=right, bandId=BAND_Q1
    Col 3: bind=q1Qty,      head="수량",     mask=#,##0, halign=center, bandId=BAND_Q1
    Col 4: bind=q2Sales,    head="매출액",   mask=#,##0, halign=right, bandId=BAND_Q2
    Col 5: bind=q2Qty,      head="수량",     mask=#,##0, halign=center, bandId=BAND_Q2
    Col 6: bind=totalSales, head="합계",     mask=#,##0, halign=right, font-weight=bold

Button: btnExportExcel
  text: "Excel Export"
  onClick:
    fn_exportExcel(this.grdSales, "/excel/export");
```

- [ ] **Step 3: 커밋**

```bash
git add nexacro/
git commit -m "feat: add Nexacro 17 client utility script for Grid metadata collection and Excel download"
```

---

## Task 10: 전체 빌드 검증 및 최종 커밋

**Files:** 없음 (기존 파일 검증)

- [ ] **Step 1: 전체 테스트 실행**

```bash
mvn test -q 2>&1 | tail -15
```
Expected:
```
Tests run: X, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 2: 컴파일 검증**

```bash
mvn compile -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: 최종 커밋**

```bash
git add .
git commit -m "chore: finalize Nexacro 17 Grid Format Excel Export sample project"
```

- [ ] **Step 4: GitHub 저장소 생성 및 Push (gh CLI 사용)**

```bash
gh repo create spring-boot-nexacro17 --public --description "Nexacro 17 Grid Format Excel Export with Spring Boot" --source=. --push
```

gh CLI가 없으면:
```bash
# 1. GitHub에서 저장소 생성 후
git remote add origin https://github.com/<username>/spring-boot-nexacro17.git
git push -u origin main
```

---

## 셀프 리뷰 결과

| 스펙 항목 | 구현 Task |
|---|---|
| Spring Boot 2.x, Java 8, Maven | Task 1 (pom.xml) |
| Oracle DB (SALES_DATA, CODE_MST) | Task 2 (init.sql) |
| ColumnMeta, BandMeta, CodeMst DTO | Task 3 |
| SalesMapper (전체조회), ComboMapper | Task 4 |
| ComboResolver (code→text) | Task 5 |
| NexacroGridExcelBuilder (멀티헤더/TextAlign/ComboText/스타일) | Task 6 |
| ExcelExportService (조율) | Task 7 |
| ExcelExportController (PlatformData 파싱, binary 응답) | Task 8 |
| Nexacro 17 클라이언트 유틸 | Task 9 |
| Git 커밋/푸시 | Task 1, 각 Task |
| 클라이언트 데이터 미전송 (서버 DB 직접조회) | Task 4, 7, 8 |
