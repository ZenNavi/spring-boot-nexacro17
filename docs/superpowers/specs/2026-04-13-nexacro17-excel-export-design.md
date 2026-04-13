# Nexacro 17 Grid Format Excel Export - Design Spec

**Date:** 2026-04-13  
**Status:** Approved

---

## 1. Overview

Nexacro 17 Grid의 속성(TextAlign, ComboText, 멀티헤더, 숫자/날짜 포맷, 배경색, 테두리, 폰트)을 그대로 반영한 Excel Export 기능을 구현한다.

클라이언트(Nexacro 17)가 Runtime에 Grid 메타데이터를 수집하여 서버에 전송하면, 서버(Spring Boot)는 Oracle DB에서 **전체 데이터를 직접 조회**하고(페이징 없음) 메타데이터 기반으로 Excel을 완전 동적으로 생성한다. 클라이언트는 데이터를 전송하지 않는다.

### 기술 스택

| 항목 | 선택 |
|------|------|
| Backend | Spring Boot 2.x, Java 8, Maven |
| DB | Oracle |
| Excel | Apache POI 5.x |
| 통신 | Nexacro PlatformData (NexacroServiceFilter) |
| Client | Nexacro 17 |

---

## 2. Architecture

```
[Nexacro 17 Client]
  └─ fn_exportExcel(grd)
       ├─ Grid 컬럼 메타데이터 수집 (colId, header, width, textAlign, editType, combo 정보)
       ├─ 밴드(멀티헤더) 메타데이터 수집 (bandText, colspan, rowspan)
       └─ (데이터 전송 없음 - 서버에서 DB 직접 조회)
           │
           │ PlatformData (ds_gridMeta + ds_bandMeta만 전송)
           ▼
[Spring Boot 2.x + Java 8]
  ├─ NexacroServiceFilter   ← PlatformData 파싱/직렬화
  ├─ ExcelExportController  ← POST /excel/export
  ├─ ExcelExportService     ← DB 전체 조회 + Excel 빌드 조율
  │    ├─ SalesMapper       ← SALES_DATA 전체 조회 (페이징 없음)
  │    └─ ComboMapper       ← CODE_MST 조회 (combo_group_cd 기반)
  └─ NexacroGridExcelBuilder ← Apache POI 기반 Excel 생성
       ├─ 멀티헤더 (CellRangeAddress merge)
       ├─ TextAlign → HorizontalAlignment 매핑
       ├─ ComboText 변환 (DB 조회한 콤보 데이터로 code→text)
       ├─ 숫자/날짜 포맷 적용
       └─ 폰트/배경색/테두리 스타일
           │
           │ .xlsx 바이너리 응답
           ▼
[Oracle DB]
  ├─ SALES_DATA 테이블 (샘플 매출 현황)
  └─ CODE_MST 테이블 (콤보 코드/텍스트 마스터)
```

---

## 3. Nexacro 17 Client

### 3.1 PlatformData 전송 Dataset 구성

클라이언트는 Excel Export 요청 시 아래 Dataset들을 하나의 PlatformData로 묶어 서버에 전송한다. **데이터(ds_mainData)와 콤보 Dataset은 전송하지 않는다** - 서버가 DB에서 직접 조회한다.

| Dataset ID | 내용 |
|---|---|
| `ds_gridMeta` | Grid 컬럼 메타데이터 |
| `ds_bandMeta` | 밴드(멀티헤더) 메타데이터 |

### 3.2 ds_gridMeta 컬럼 구조

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| col_id | STRING | 컬럼 ID (Dataset 컬럼명과 매핑) |
| header_text | STRING | 헤더 표시 텍스트 |
| col_width | INT | 컬럼 너비 (px) |
| text_align | STRING | left / center / right |
| col_type | STRING | text / number / date |
| number_format | STRING | #,##0 / #,##0.00 등 (숫자일 때) |
| date_format | STRING | yyyy-MM-dd 등 (날짜일 때) |
| edit_type | STRING | text / combo |
| combo_group_cd | STRING | 콤보 그룹 코드 (CODE_MST.GROUP_CD, edit_type=combo일 때) |
| combo_code_col | STRING | CODE_MST의 code 컬럼명 (기본: CODE) |
| combo_text_col | STRING | CODE_MST의 text 컬럼명 (기본: CODE_NM) |
| band_id | STRING | 소속 밴드 ID (멀티헤더 연결) |
| bg_color | STRING | 배경색 HEX (#FFFFFF) |
| font_bold | STRING | true / false |
| font_size | INT | 폰트 크기 (pt) |
| border_style | STRING | thin / medium / thick / none |

### 3.3 ds_bandMeta 컬럼 구조

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| band_id | STRING | 밴드 ID |
| band_text | STRING | 밴드 헤더 텍스트 |
| col_span | INT | 가로 병합 컬럼 수 |
| row_span | INT | 세로 병합 행 수 |
| band_order | INT | 밴드 표시 순서 |
| bg_color | STRING | 배경색 HEX |
| text_align | STRING | left / center / right |
| font_bold | STRING | true / false |

### 3.4 공통 유틸 함수 (ExcelExportUtil.xjs)

```javascript
// fn_exportExcel(grd) - Grid 객체를 받아 메타데이터 수집 후 서버 전송
function fn_exportExcel(grd) {
    // 1. 컬럼 메타데이터 수집 (ds_gridMeta 구성)
    // 2. 밴드 메타데이터 수집 (ds_bandMeta 구성)
    // 3. PlatformData 구성 후 POST /excel/export 전송
    //    - 데이터(Dataset)는 전송하지 않음 (서버 DB 직접 조회)
    // 4. 응답 바이너리를 파일로 다운로드
}
```

---

## 4. Spring Boot Server

### 4.1 프로젝트 구조

```
com.example.nexacro/
├── NexacroApplication.java
├── config/
│   └── NexacroConfig.java            ← NexacroServiceFilter Bean 등록
├── controller/
│   └── ExcelExportController.java    ← POST /excel/export
├── service/
│   └── ExcelExportService.java       ← DB 조회 + Excel 빌드 조율
├── excel/
│   ├── NexacroGridExcelBuilder.java  ← Apache POI Excel 생성 핵심 엔진
│   ├── ColumnMeta.java               ← 컬럼 메타 DTO
│   ├── BandMeta.java                 ← 밴드(멀티헤더) DTO
│   └── ComboResolver.java            ← code→text 변환 유틸
├── mapper/
│   ├── SalesMapper.java              ← SALES_DATA 전체 조회
│   ├── SalesMapper.xml
│   ├── ComboMapper.java              ← CODE_MST 콤보 조회
│   └── ComboMapper.xml
└── dto/
    ├── SalesData.java
    └── CodeMst.java
```

### 4.2 ExcelExportController

- **엔드포인트**: `POST /excel/export`
- **입력**: Nexacro PlatformData (ds_gridMeta, ds_bandMeta만 포함 - 데이터 없음)
- **출력**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 바이너리

### 4.3 NexacroGridExcelBuilder 처리 순서

1. BandMeta 파싱 → 멀티헤더 행 수 계산
2. ColumnMeta 파싱 → 컬럼 순서, 너비, 스타일 결정
3. 헤더 행 생성
   - 밴드 행: 병합(CellRangeAddress) + 텍스트 + 스타일
   - 컬럼 행: 텍스트 + 스타일
4. 데이터 행 생성
   - 각 셀: ComboResolver로 code→text 변환
   - 숫자/날짜: DataFormat 적용
   - TextAlign: HorizontalAlignment 매핑
   - 배경색/폰트/테두리 적용
5. 컬럼 너비 설정 (px → POI 단위 변환: `width * 36`)
6. ByteArrayOutputStream → HttpServletResponse 출력

### 4.4 ComboResolver

```
ComboResolver.resolve(comboGroupCd, codeValue)
→ String (display text)
```

서비스 초기화 시 `CODE_MST` 테이블에서 필요한 콤보 그룹을 일괄 조회하여 `Map<groupCd, Map<code, text>>` 형태로 보관한다. 셀 렌더링 시 groupCd + code 값으로 text를 즉시 조회한다. code가 없으면 code 값 그대로 반환한다.

---

## 5. Oracle DB 샘플 스키마

### 5.1 테이블: SALES_DATA

```sql
-- 매출 데이터 테이블
CREATE TABLE SALES_DATA (
    ID          NUMBER PRIMARY KEY,
    REGION_CD   VARCHAR2(10),    -- 지역 코드 (콤보, CODE_MST.GROUP_CD='REGION')
    CATEGORY_CD VARCHAR2(10),    -- 카테고리 코드 (콤보, CODE_MST.GROUP_CD='CATEGORY')
    Q1_SALES    NUMBER,          -- 1분기 매출액
    Q1_QTY      NUMBER,          -- 1분기 수량
    Q2_SALES    NUMBER,          -- 2분기 매출액
    Q2_QTY      NUMBER,          -- 2분기 수량
    TOTAL_SALES NUMBER,          -- 합계
    REG_DATE    DATE             -- 등록일
);

-- 콤보 코드 마스터 테이블
CREATE TABLE CODE_MST (
    GROUP_CD    VARCHAR2(20),    -- 그룹 코드 (예: REGION, CATEGORY)
    CODE        VARCHAR2(20),    -- 코드 값
    CODE_NM     VARCHAR2(100),   -- 코드 표시명
    SORT_ORDER  NUMBER,          -- 정렬 순서
    PRIMARY KEY (GROUP_CD, CODE)
);
```

### 5.2 Grid 샘플 구조 (멀티헤더)

```
┌────────┬──────────┬───────────────────┬───────────────────┬──────────┐
│        │          │      1분기        │      2분기        │          │
│  지역  │ 카테고리  ├─────────┬─────────┼─────────┬─────────┤  합계    │
│        │          │  매출액 │  수량   │  매출액 │  수량   │          │
├────────┼──────────┼─────────┼─────────┼─────────┼─────────┼──────────┤
│ 서울   │ 전자제품  │1,200,000│   150  │  980,000│   120  │2,180,000 │
│ 부산   │ 의류      │  850,000│   200  │  760,000│   180  │1,610,000 │
└────────┴──────────┴─────────┴─────────┴─────────┴─────────┴──────────┘
```

- 지역: ComboText (SEO→서울, BUS→부산, DAE→대구)
- 카테고리: ComboText (ELEC→전자제품, CLOT→의류, FOOD→식품)
- 매출액: `#,##0` 포맷, right align
- 수량: `#,##0` 포맷, center align
- 1분기/2분기 헤더: colspan=2 병합
- 합계: bold, right align

---

## 6. 데이터 흐름

```
1. 사용자가 Nexacro Grid에서 [Excel Export] 버튼 클릭
2. fn_exportExcel(grd) 호출
3. Grid 객체에서 컬럼(ds_gridMeta) + 밴드(ds_bandMeta) 메타데이터만 수집
   (데이터 Dataset은 포함하지 않음)
4. PlatformData 구성 → POST /excel/export
5. NexacroServiceFilter가 PlatformData 파싱
6. ExcelExportController → ExcelExportService 위임
7. ExcelExportService:
   a. ds_gridMeta에서 combo_group_cd 목록 추출
   b. CODE_MST에서 해당 그룹 코드 전체 조회 → ComboResolver 초기화
   c. SALES_DATA 전체 조회 (페이징 없음, ORDER BY ID)
8. NexacroGridExcelBuilder: Excel 파일 생성
   - 메타데이터 기반 헤더(밴드+컬럼) 생성
   - DB 조회 데이터로 데이터 행 생성
   - ComboResolver로 code→text 변환
9. HttpServletResponse로 xlsx 바이너리 스트리밍
10. 브라우저 파일 다운로드
```

---

## 7. 에러 처리

| 상황 | 처리 |
|---|---|
| 메타데이터 Dataset 누락 | 400 Bad Request + Nexacro ErrorCode 반환 |
| DB 조회 실패 | 500 + 에러 메시지 PlatformData 반환 |
| Excel 생성 실패 | 500 + 에러 메시지 PlatformData 반환 |
| 콤보 Dataset 없음 | code 값 그대로 셀에 표시 (graceful fallback) |

---

## 8. 미포함 범위 (Out of Scope)

- 페이지 나누기(Print Area) 설정
- 차트 삽입
- 암호화 보호
- 동시 다운로드 제한/큐잉
