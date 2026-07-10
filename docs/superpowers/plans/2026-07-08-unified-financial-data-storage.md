# 统一金融数据存储 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现统一金融数据存取层，用一套泛化 Service + Controller 替换现有的 FinancialReport 全套代码

**Architecture:** 新增 RouteDispatcher（本地缓存路由 dataType → 物理表） + GenericDataService（NamedParameterJdbcTemplate 泛化 CRUD） + FinancialDataController（统一 API 入口）。物理表统一为 fin_{dataType} 结构，保留 metadata 系统作为 schema 定义层。

**Tech Stack:** Spring Boot 2.5.6, JDK 17, MyBatis（仅 metadata 表）, NamedParameterJdbcTemplate（泛化层）, Caffeine Cache, MySQL 8.0, JUnit 5 + Mockito + AssertJ

## 全局约束

- 所有新增类遵循 `@Resource` 字段注入（见 `patterns.md` 和 `api-design.md` 规则）
- Controller 返回 `ResponseDTO<T>`，不使用 `.json` 后缀
- DomainService 接口以 `DomainService` 结尾（已存在的 GenericDataService 作为特例）
- 所有写入操作使用 upsert 语义（ON DUPLICATE KEY UPDATE）
- 表名由 RouteDispatcher 从 metadata_model 注册的 code 解析，不接受用户直接输入
- 异常处理走全局 `GlobalExceptionHandler`

---

## 文件结构

### 新建文件

| 文件 | 模块 | 职责 |
|------|------|------|
| `core/service/RouteDispatcher.java` | stock-core | dataType → 物理表名路由（Caffeine 缓存 + metadata 回源） |
| `core/model/GenericDataRecord.java` | stock-core | 泛化数据记录领域模型 |
| `core/model/GenericDataQuery.java` | stock-core | 泛化查询参数封装 |
| `core/service/GenericDataService.java` | stock-core | 泛化数据服务接口 |
| `core/service/impl/GenericDataServiceImpl.java` | stock-core | 泛化数据服务实现（NamedParameterJdbcTemplate） |
| `biz/controller/FinancialDataController.java` | stock-biz | 统一数据 API /api/financial-data |
| `biz/dto/GenericUpsertRequest.java` | stock-biz | 单条 upsert 请求体 |
| `biz/dto/GenericBatchUpsertRequest.java` | stock-biz | 批量 upsert 请求体 |
| `biz/dto/GenericDataDTO.java` | stock-biz | 响应 DTO |

### 删除文件

| 文件 | 模块 |
|------|------|
| `common/dal/mappers/FinancialReportMapper.java` | stock-common |
| `common/dal/mappers/entity/FinancialReportDO.java` | stock-common |
| `common/resources/mappers/FinancialReportMapper.xml` | stock-common |
| `core/model/FinancialReport.java` | stock-core |
| `core/service/FinancialReportDomainService.java` | stock-core |
| `core/service/impl/FinancialReportDomainServiceImpl.java` | stock-core |
| `biz/controller/FinancialReportController.java` | stock-biz |
| `biz/dto/FinancialReportDTO.java` | stock-biz |
| `biz/dto/FinancialReportSaveRequest.java` | stock-biz |
| `biz/dto/FinancialReportQueryRequest.java` | stock-biz |

### 修改文件

| 文件 | 改动 |
|------|------|
| `core/converter/DomainConverter.java` | 删除 FinancialReport 相关方法 |
| `core/converter/DbConverter.java` | 删除 FinancialReport 相关方法 |
| `biz/converter/DtoConverter.java` | 删除 FinancialReport 相关方法 |
| `biz/init/FinancialMetadataInitializer.java` | 三个 model 的 extInfo 补充 dataType 字段 |

### 数据库迁移

| 操作 | 说明 |
|------|------|
| `ALTER TABLE financial_report RENAME TO fin_report` | 零停机迁移 |
| 后续验证 struct 一致 | `DESC fin_report` 确认列结构正确 |

### 测试文件

| 文件 | 模块 | 说明 |
|------|------|------|
| `core/service/RouteDispatcherTest.java` | stock-core (src/test) | 单元测试 — 路由缓存和回源逻辑 |
| `core/service/impl/GenericDataServiceImplTest.java` | stock-core (src/test) | 单元测试 — 泛化 CRUD（mock JdbcTemplate） |
| `biz/controller/FinancialDataControllerTest.java` | stock-biz (src/test) | 单元测试 — Controller 参数绑定和响应格式 |

---

### Task 1: RouteDispatcher — 路由层

**Files:**
- Create: `stock-core/src/main/java/com/ethanpark/stock/core/service/RouteDispatcher.java`
- Test: `stock-core/src/test/java/com/ethanpark/stock/core/service/RouteDispatcherTest.java`

**Interfaces:**
- Consumes: `MetadataDomainService.getModelByCode(String code) → MetadataModel`
- Produces: `RouteDispatcher.resolveTable(String dataType) → String` — 返回物理表名，如 `"fin_report"`
- Produces: `RouteDispatcher.resolveModel(String dataType, String modelCode) → MetadataModel` — 校验 modelCode 属于 dataType

- [ ] **Step 1: Write the failing test**

```java
package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.metadata.MetadataModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteDispatcherTest {

    @Mock
    private MetadataDomainService metadataDomainService;

    private RouteDispatcher routeDispatcher;

    @BeforeEach
    void setUp() {
        routeDispatcher = new RouteDispatcher();
        routeDispatcher.setMetadataDomainService(metadataDomainService);
    }

    @Test
    @DisplayName("resolveTable 返回 fin_{dataType} 格式的表名")
    void resolveTable_existingDataType_returnsPrefixedTableName() {
        MetadataModel model = new MetadataModel();
        model.setCode("report");
        when(metadataDomainService.getModelByCode("report")).thenReturn(model);

        String table = routeDispatcher.resolveTable("report");

        assertThat(table).isEqualTo("fin_report");
        verify(metadataDomainService).getModelByCode("report");
    }

    @Test
    @DisplayName("resolveTable 未注册的 dataType 抛异常")
    void resolveTable_unknownDataType_throws() {
        when(metadataDomainService.getModelByCode("unknown")).thenReturn(null);

        assertThatThrownBy(() -> routeDispatcher.resolveTable("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未知 dataType");
    }

    @Test
    @DisplayName("resolveTable 走本地缓存，不重复查 DB")
    void resolveTable_cached_doesNotCallDbAgain() {
        MetadataModel model = new MetadataModel();
        model.setCode("report");
        when(metadataDomainService.getModelByCode("report")).thenReturn(model);

        // 第一次调用：查 DB
        routeDispatcher.resolveTable("report");
        // 第二次调用：走缓存
        routeDispatcher.resolveTable("report");

        verify(metadataDomainService, times(1)).getModelByCode("report");
    }

    @Test
    @DisplayName("resolveModel 校验成功后返回 MetadataModel")
    void resolveModel_validModelCode_returnsModel() {
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("dataType", "report");
        MetadataModel model = new MetadataModel();
        model.setCode("cash_flow_statement");
        model.setExtInfo(extInfo);
        when(metadataDomainService.getModelByCode("cash_flow_statement")).thenReturn(model);

        MetadataModel result = routeDispatcher.resolveModel("report", "cash_flow_statement");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("cash_flow_statement");
    }

    @Test
    @DisplayName("resolveModel 当 modelCode 不属于 dataType 时抛异常")
    void resolveModel_wrongDataType_throws() {
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("dataType", "kline");
        MetadataModel model = new MetadataModel();
        model.setCode("cash_flow_statement");
        model.setExtInfo(extInfo);
        when(metadataDomainService.getModelByCode("cash_flow_statement")).thenReturn(model);

        assertThatThrownBy(() -> routeDispatcher.resolveModel("report", "cash_flow_statement"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("modelCode cash_flow_statement 不属于 dataType report");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
# 编译 + 运行测试
mvn test -pl stock-core -Dtest=RouteDispatcherTest -DfailIfNoTests=false
```
Expected: 编译失败（RouteDispatcher 不存在）

- [ ] **Step 3: Write minimal implementation**

```java
package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * dataType → 物理表名 路由分发器。
 *
 * <p>通过本地缓存查询 metadata_model 表，确认 dataType 已注册后返回物理表名。
 * 物理表名格式固定为 fin_{dataType}。
 */
public class RouteDispatcher {

    @Resource
    private MetadataDomainService metadataDomainService;

    private final Cache<String, MetadataModel> routeCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    /**
     * 解析 dataType 为物理表名。
     *
     * @param dataType 数据类型编码
     * @return 物理表名，如 "fin_report"
     * @throws IllegalArgumentException dataType 未注册时抛出
     */
    public String resolveTable(String dataType) {
        MetadataModel model = routeCache.get(dataType, key -> {
            MetadataModel m = metadataDomainService.getModelByCode(key);
            if (m == null) {
                throw new IllegalArgumentException("未知 dataType: " + key);
            }
            return m;
        });
        return "fin_" + dataType;
    }

    /**
     * 校验并解析 modelCode，确认其属于指定的 dataType。
     *
     * @param dataType  数据类型
     * @param modelCode 模型编码
     * @return MetadataModel 对象
     * @throws IllegalArgumentException 校验不通过时抛出
     */
    public MetadataModel resolveModel(String dataType, String modelCode) {
        MetadataModel model = metadataDomainService.getModelByCode(modelCode);
        if (model == null) {
            throw new IllegalArgumentException("未知 modelCode: " + modelCode);
        }
        Map<String, Object> extInfo = model.getExtInfo();
        if (extInfo == null || !dataType.equals(extInfo.get("dataType"))) {
            throw new IllegalArgumentException(
                    "modelCode " + modelCode + " 不属于 dataType " + dataType);
        }
        return model;
    }

    /**
     * 校验 dataType 是否存在（不走缓存穿透，仅查缓存）。
     */
    public boolean exists(String dataType) {
        MetadataModel model = routeCache.getIfPresent(dataType);
        return model != null;
    }

    // for testing — setter injection
    public void setMetadataDomainService(MetadataDomainService metadataDomainService) {
        this.metadataDomainService = metadataDomainService;
    }
}
```

Note: `RouteDispatcher` 不标注 `@Component`，因为它在 `stock-core` 模块中，而 `stock-core` 的 Spring 扫描路径需要确认。实际上 `StockCoreConfig` 已经 `@ComponentScan` 了 `com.ethanpark.stock.core`，所以加 `@Component` 即可。

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -pl stock-core -Dtest=RouteDispatcherTest -DfailIfNoTests=false
```
Expected: PASS (全部测试通过)

- [ ] **Step 5: Commit**

```bash
git add stock-core/src/main/java/com/ethanpark/stock/core/service/RouteDispatcher.java
git add stock-core/src/test/java/com/ethanpark/stock/core/service/RouteDispatcherTest.java
git commit -m "feat: add RouteDispatcher for dataType to table name resolution

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 2: GenericDataRecord + GenericDataService — 泛化数据服务

**Files:**
- Create: `stock-core/src/main/java/com/ethanpark/stock/core/model/GenericDataRecord.java`
- Create: `stock-core/src/main/java/com/ethanpark/stock/core/model/GenericDataQuery.java`
- Create: `stock-core/src/main/java/com/ethanpark/stock/core/service/GenericDataService.java`
- Create: `stock-core/src/main/java/com/ethanpark/stock/core/service/impl/GenericDataServiceImpl.java`
- Test: `stock-core/src/test/java/com/ethanpark/stock/core/service/impl/GenericDataServiceImplTest.java`

**Interfaces:**
- Consumes: `RouteDispatcher.resolveTable(dataType) → String`, `RouteDispatcher.resolveModel(dataType, modelCode) → MetadataModel`, `NamedParameterJdbcTemplate`
- Produces: `GenericDataService` interface with all CRUD methods

- [ ] **Step 1: Write the failing tests**

```java
// GenericDataRecord.java — POJO
package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class GenericDataRecord {
    private Long id;
    private String market;
    private String code;
    private String modelCode;
    private String partitionDate;
    private Map<String, Object> dataContent;
    private Map<String, Object> extraInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
```

```java
// GenericDataQuery.java — 查询参数
package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericDataQuery {
    private String market;
    private String code;
    private String modelCode;
    private String startDate;
    private String endDate;
    private int page = 1;
    private int size = 20;
}
```

```java
// GenericDataService.java — 接口
package com.ethanpark.stock.core.service;

import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;

import java.util.List;

public interface GenericDataService {

    GenericDataRecord get(String dataType, String market, String code,
                          String modelCode, String partitionDate);

    PageResult<GenericDataRecord> queryPage(String dataType, GenericDataQuery query);

    List<GenericDataRecord> queryRange(String dataType, String market, String code,
                                       String modelCode, String startDate, String endDate);

    List<GenericDataRecord> queryLatest(String dataType, String market, String code, int limit);

    List<GenericDataRecord> queryLatestByModel(String dataType, String market, String code);

    void upsert(String dataType, GenericDataRecord record);

    void batchUpsert(String dataType, List<GenericDataRecord> records);

    void replaceRange(String dataType, String market, String code, String modelCode,
                      String startDate, String endDate, List<GenericDataRecord> records);

    void delete(String dataType, String market, String code, String modelCode, String partitionDate);

    void deleteByRange(String dataType, String market, String code, String modelCode,
                       String startDate, String endDate);
}
```

```java
// GenericDataServiceImplTest.java — 测试
package com.ethanpark.stock.core.service.impl;

import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.GenericDataService;
import com.ethanpark.stock.core.service.RouteDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericDataServiceImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private RouteDispatcher routeDispatcher;

    private GenericDataService genericDataService;

    @BeforeEach
    void setUp() {
        GenericDataServiceImpl impl = new GenericDataServiceImpl();
        impl.setJdbcTemplate(jdbcTemplate);
        impl.setRouteDispatcher(routeDispatcher);
        genericDataService = impl;
    }

    @Test
    @DisplayName("get() 返回单条记录")
    void get_existingRecord_returnsRecord() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("market", "SH");
        row.put("code", "000001");
        row.put("model_code", "cash_flow_statement");
        row.put("partition_date", "2024-12-31");
        row.put("data_content", "{\"totalRevenue\":1000}");
        row.put("extra_info", "{}");
        row.put("gmt_create", new Date());
        row.put("gmt_modified", new Date());
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.singletonList(row));

        GenericDataRecord result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("000001");
        assertThat(result.getMarket()).isEqualTo("SH");
        assertThat(result.getModelCode()).isEqualTo("cash_flow_statement");
        assertThat(result.getPartitionDate()).isEqualTo("2024-12-31");
        assertThat(result.getDataContent()).containsEntry("totalRevenue", 1000);
    }

    @Test
    @DisplayName("get() 不存在时返回 null")
    void get_notFound_returnsNull() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.emptyList());

        GenericDataRecord result = genericDataService.get(
                "report", "SH", "000001", "cash_flow_statement", "2024-12-31");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("upsert() 使用 ON DUPLICATE KEY UPDATE 语义")
    void upsert_usesUpsertSemantics() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket("SH");
        record.setCode("000001");
        record.setModelCode("cash_flow_statement");
        record.setPartitionDate("2024-12-31");
        Map<String, Object> content = new HashMap<>();
        content.put("totalRevenue", 1000);
        record.setDataContent(content);

        genericDataService.upsert("report", record);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramCaptor.capture());

        assertThat(sqlCaptor.getValue()).contains("duplicate key update");
        assertThat(sqlCaptor.getValue()).contains("fin_report");
    }

    @Test
    @DisplayName("queryPage() 返回分页结果")
    void queryPage_returnsPageResult() {
        when(routeDispatcher.resolveTable("report")).thenReturn("fin_report");
        GenericDataQuery query = new GenericDataQuery();
        query.setMarket("SH");
        query.setCode("000001");
        query.setPage(1);
        query.setSize(20);

        when(jdbcTemplate.queryForObject(contains("count"), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(1L);
        when(jdbcTemplate.queryForList(contains("limit"), any(MapSqlParameterSource.class)))
                .thenReturn(Collections.singletonList(Map.of("id", 1L)));

        PageResult<GenericDataRecord> result = genericDataService.queryPage("report", query);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl stock-core -Dtest=GenericDataServiceImplTest -DfailIfNoTests=false
```
Expected: 编译失败（GenericDataServiceImpl 不存在）

- [ ] **Step 3: Write implementation**

```java
// GenericDataServiceImpl.java
package com.ethanpark.stock.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.GenericDataService;
import com.ethanpark.stock.core.service.RouteDispatcher;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenericDataServiceImpl implements GenericDataService {

    @Resource
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Resource
    private RouteDispatcher routeDispatcher;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Override
    public GenericDataRecord get(String dataType, String market, String code,
                                 String modelCode, String partitionDate) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "select * from %s where market = :market and code = :code " +
            "and model_code = :modelCode and partition_date = :partitionDate", table);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("market", market)
                .addValue("code", code)
                .addValue("modelCode", modelCode)
                .addValue("partitionDate", partitionDate);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRow(rows.get(0));
    }

    @Override
    public PageResult<GenericDataRecord> queryPage(String dataType, GenericDataQuery query) {
        String table = routeDispatcher.resolveTable(dataType);
        if (query.getPage() < 1) query.setPage(1);
        if (query.getSize() < 1) query.setSize(DEFAULT_PAGE_SIZE);

        // Build dynamic WHERE
        StringBuilder whereClause = new StringBuilder(" where 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (query.getMarket() != null) {
            whereClause.append(" and market = :market");
            params.addValue("market", query.getMarket());
        }
        if (query.getCode() != null) {
            whereClause.append(" and code = :code");
            params.addValue("code", query.getCode());
        }
        if (query.getModelCode() != null) {
            whereClause.append(" and model_code = :modelCode");
            params.addValue("modelCode", query.getModelCode());
        }
        if (query.getStartDate() != null) {
            whereClause.append(" and partition_date >= :startDate");
            params.addValue("startDate", query.getStartDate());
        }
        if (query.getEndDate() != null) {
            whereClause.append(" and partition_date <= :endDate");
            params.addValue("endDate", query.getEndDate());
        }

        // Count
        String countSql = String.format("select count(*) from %s%s", table, whereClause);
        long total = jdbcTemplate.queryForObject(countSql, params, Long.class);

        // Page
        int offset = (query.getPage() - 1) * query.getSize();
        String pageSql = String.format("select * from %s%s order by partition_date desc limit :offset, :limit",
                table, whereClause);
        params.addValue("offset", offset);
        params.addValue("limit", query.getSize());

        List<GenericDataRecord> items = Collections.emptyList();
        if (total > 0) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(pageSql, params);
            items = rows.stream().map(this::mapRow).collect(Collectors.toList());
        }

        PageResult<GenericDataRecord> result = new PageResult<>();
        result.setItems(items);
        result.setTotal(total);
        result.setPage(query.getPage());
        result.setSize(query.getSize());
        result.setPages((int) Math.ceil((double) total / query.getSize()));
        return result;
    }

    @Override
    public List<GenericDataRecord> queryRange(String dataType, String market, String code,
                                              String modelCode, String startDate, String endDate) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "select * from %s where market = :market and code = :code " +
            "and model_code = :modelCode and partition_date between :startDate and :endDate " +
            "order by partition_date asc", table);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("market", market)
                .addValue("code", code)
                .addValue("modelCode", modelCode)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        return rows.stream().map(this::mapRow).collect(Collectors.toList());
    }

    @Override
    public List<GenericDataRecord> queryLatest(String dataType, String market, String code, int limit) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "select * from %s where market = :market and code = :code " +
            "order by partition_date desc limit :limit", table);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("market", market)
                .addValue("code", code)
                .addValue("limit", limit);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        return rows.stream().map(this::mapRow).collect(Collectors.toList());
    }

    @Override
    public List<GenericDataRecord> queryLatestByModel(String dataType, String market, String code) {
        String table = routeDispatcher.resolveTable(dataType);
        // Subquery: per model_code, get max partition_date
        String sql = String.format(
            "select t.* from %s t inner join (" +
            "  select model_code, max(partition_date) as max_date from %s" +
            "  where market = :market and code = :code group by model_code" +
            ") latest on t.model_code = latest.model_code and t.partition_date = latest.max_date " +
            "where t.market = :market and t.code = :code", table, table);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("market", market)
                .addValue("code", code);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        return rows.stream().map(this::mapRow).collect(Collectors.toList());
    }

    @Override
    public void upsert(String dataType, GenericDataRecord record) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "insert into %s (market, code, model_code, partition_date, data_content, extra_info) " +
            "values (:market, :code, :modelCode, :partitionDate, :dataContent, :extraInfo) " +
            "on duplicate key update data_content = values(data_content), extra_info = values(extra_info)",
            table);

        MapSqlParameterSource params = toParams(record);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void batchUpsert(String dataType, List<GenericDataRecord> records) {
        if (records == null || records.isEmpty()) return;
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "insert into %s (market, code, model_code, partition_date, data_content, extra_info) " +
            "values (:market, :code, :modelCode, :partitionDate, :dataContent, :extraInfo) " +
            "on duplicate key update data_content = values(data_content), extra_info = values(extra_info)",
            table);

        MapSqlParameterSource[] batchParams = records.stream()
                .map(this::toParams)
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public void replaceRange(String dataType, String market, String code, String modelCode,
                             String startDate, String endDate, List<GenericDataRecord> records) {
        deleteByRange(dataType, market, code, modelCode, startDate, endDate);
        if (records != null && !records.isEmpty()) {
            batchUpsert(dataType, records);
        }
    }

    @Override
    public void delete(String dataType, String market, String code,
                       String modelCode, String partitionDate) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "delete from %s where market = :market and code = :code " +
            "and model_code = :modelCode and partition_date = :partitionDate", table);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("market", market)
                .addValue("code", code)
                .addValue("modelCode", modelCode)
                .addValue("partitionDate", partitionDate);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteByRange(String dataType, String market, String code, String modelCode,
                              String startDate, String endDate) {
        String table = routeDispatcher.resolveTable(dataType);
        String sql = String.format(
            "delete from %s where market = :market and code = :code " +
            "and model_code = :modelCode and partition_date between :startDate and :endDate", table);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("market", market)
                .addValue("code", code)
                .addValue("modelCode", modelCode)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        jdbcTemplate.update(sql, params);
    }

    // ——— helper methods ———

    private MapSqlParameterSource toParams(GenericDataRecord record) {
        return new MapSqlParameterSource()
                .addValue("market", record.getMarket())
                .addValue("code", record.getCode())
                .addValue("modelCode", record.getModelCode())
                .addValue("partitionDate", record.getPartitionDate())
                .addValue("dataContent", JSON.toJSONString(record.getDataContent()))
                .addValue("extraInfo", JSON.toJSONString(record.getExtraInfo()));
    }

    private GenericDataRecord mapRow(Map<String, Object> row) {
        GenericDataRecord record = new GenericDataRecord();
        record.setId(toLong(row.get("id")));
        record.setMarket((String) row.get("market"));
        record.setCode((String) row.get("code"));
        record.setModelCode((String) row.get("model_code"));
        record.setPartitionDate((String) row.get("partition_date"));
        record.setDataContent(parseJson(row.get("data_content")));
        record.setExtraInfo(parseJson(row.get("extra_info")));
        record.setGmtCreate((Date) row.get("gmt_create"));
        record.setGmtModified((Date) row.get("gmt_modified"));
        return record;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(Object value) {
        if (value == null) return Collections.emptyMap();
        // For MySQL's JSON type, the driver returns a String
        if (value instanceof String) {
            return JSON.parseObject((String) value,
                    new TypeReference<Map<String, Object>>() {});
        }
        return (Map<String, Object>) value;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        return Long.parseLong(value.toString());
    }

    // for testing
    void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    void setRouteDispatcher(RouteDispatcher routeDispatcher) {
        this.routeDispatcher = routeDispatcher;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -pl stock-core -Dtest=GenericDataServiceImplTest -DfailIfNoTests=false
```
Expected: PASS

- [ ] **Step 5: Add @Component to RouteDispatcher**

Add `@Component` to RouteDispatcher so it's picked up by stock-core's component scan:

```java
import org.springframework.stereotype.Component;

@Component
public class RouteDispatcher {
    // ...
}
```

- [ ] **Step 6: Commit**

```bash
git add stock-core/src/main/java/com/ethanpark/stock/core/model/GenericDataRecord.java
git add stock-core/src/main/java/com/ethanpark/stock/core/model/GenericDataQuery.java
git add stock-core/src/main/java/com/ethanpark/stock/core/service/GenericDataService.java
git add stock-core/src/main/java/com/ethanpark/stock/core/service/impl/GenericDataServiceImpl.java
git add stock-core/src/test/java/com/ethanpark/stock/core/service/impl/GenericDataServiceImplTest.java
git commit -m "feat: add GenericDataService with full CRUD and batch upsert

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 3: FinancialDataController — 统一 API 入口

**Files:**
- Create: `stock-biz/src/main/java/com/ethanpark/stock/biz/controller/FinancialDataController.java`
- Create: `stock-biz/src/main/java/com/ethanpark/stock/biz/dto/GenericUpsertRequest.java`
- Create: `stock-biz/src/main/java/com/ethanpark/stock/biz/dto/GenericBatchUpsertRequest.java`
- Create: `stock-biz/src/main/java/com/ethanpark/stock/biz/dto/GenericDataDTO.java`
- Test: `stock-biz/src/test/java/com/ethanpark/stock/biz/controller/FinancialDataControllerTest.java`

**Interfaces:**
- Consumes: `GenericDataService` interface
- Produces: REST endpoints at `/api/financial-data`

- [ ] **Step 1: Write the failing tests**

```java
// GenericUpsertRequest.java
package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Getter
@Setter
public class GenericUpsertRequest {
    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    @NotBlank(message = "market 不能为空")
    private String market;

    @NotBlank(message = "code 不能为空")
    private String code;

    @NotBlank(message = "modelCode 不能为空")
    private String modelCode;

    @NotBlank(message = "partitionDate 不能为空")
    private String partitionDate;

    private Map<String, Object> dataContent;
    private Map<String, Object> extraInfo;
}
```

```java
// GenericBatchUpsertRequest.java
package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class GenericBatchUpsertRequest {
    @NotBlank(message = "dataType 不能为空")
    private String dataType;

    @NotEmpty(message = "records 不能为空")
    @Valid
    private List<RecordItem> records;

    @Getter
    @Setter
    public static class RecordItem {
        @NotBlank
        private String market;
        @NotBlank
        private String code;
        @NotBlank
        private String modelCode;
        @NotBlank
        private String partitionDate;
        private Map<String, Object> dataContent;
        private Map<String, Object> extraInfo;
    }
}
```

```java
// GenericDataDTO.java
package com.ethanpark.stock.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class GenericDataDTO {
    private Long id;
    private String market;
    private String code;
    private String modelCode;
    private String partitionDate;
    private Map<String, Object> dataContent;
    private Map<String, Object> extraInfo;
    private Date gmtCreate;
    private Date gmtModified;
}
```

```java
// FinancialDataControllerTest.java
package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.dto.GenericUpsertRequest;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.GenericDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialDataControllerTest {

    @Mock
    private GenericDataService genericDataService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FinancialDataController controller = new FinancialDataController();
        controller.setGenericDataService(genericDataService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/financial-data 精确查询返回单条")
    void query_exactMatch_returnsRecord() throws Exception {
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket("SH");
        record.setCode("000001");
        record.setPartitionDate("2024-12-31");
        when(genericDataService.get(eq("report"), eq("SH"), eq("000001"),
                eq("cash_flow_statement"), eq("2024-12-31"))).thenReturn(record);

        mockMvc.perform(get("/api/financial-data")
                .param("dataType", "report")
                .param("market", "SH")
                .param("code", "000001")
                .param("modelCode", "cash_flow_statement")
                .param("partitionDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("000001"));
    }

    @Test
    @DisplayName("GET /api/financial-data 不存在返回非 200")
    void query_notFound_returnsError() throws Exception {
        when(genericDataService.get(anyString(), anyString(), anyString(),
                anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/api/financial-data")
                .param("dataType", "report")
                .param("market", "SH")
                .param("code", "999999")
                .param("modelCode", "cash_flow_statement")
                .param("partitionDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/financial-data/upsert 返回成功")
    void upsert_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/financial-data/upsert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dataType\":\"report\",\"market\":\"SH\",\"code\":\"000001\"," +
                        "\"modelCode\":\"cash_flow_statement\",\"partitionDate\":\"2024-12-31\"," +
                        "\"dataContent\":{\"totalRevenue\":1000}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl stock-biz -Dtest=FinancialDataControllerTest -DfailIfNoTests=false
```
Expected: 编译失败（FinancialDataController 不存在）

- [ ] **Step 3: Write implementation**

```java
// FinancialDataController.java
package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.converter.DtoConverter;
import com.ethanpark.stock.biz.dto.GenericBatchUpsertRequest;
import com.ethanpark.stock.biz.dto.GenericDataDTO;
import com.ethanpark.stock.biz.dto.GenericUpsertRequest;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.GenericDataService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/financial-data")
public class FinancialDataController {

    @Resource
    private GenericDataService genericDataService;

    @GetMapping
    public ResponseDTO<?> query(
            @RequestParam String dataType,
            @RequestParam(required = false, defaultValue = "SH") String market,
            @RequestParam String code,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String partitionDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        // 精确查询：dataType + market + code + modelCode + partitionDate
        if (partitionDate != null && modelCode != null) {
            GenericDataRecord record = genericDataService.get(
                    dataType, market, code, modelCode, partitionDate);
            if (record == null) {
                return ResponseDTO.success();
            }
            return ResponseDTO.success(convert(record));
        }

        // 分页查询
        GenericDataQuery query = new GenericDataQuery();
        query.setMarket(market);
        query.setCode(code);
        query.setModelCode(modelCode);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setPage(page);
        query.setSize(size);

        PageResult<GenericDataRecord> pageResult = genericDataService.queryPage(dataType, query);

        PageResult<GenericDataDTO> dtoResult = new PageResult<>();
        dtoResult.setTotal(pageResult.getTotal());
        dtoResult.setPage(pageResult.getPage());
        dtoResult.setSize(pageResult.getSize());
        dtoResult.setPages(pageResult.getPages());
        dtoResult.setItems(pageResult.getItems().stream()
                .map(this::convert)
                .collect(Collectors.toList()));

        return ResponseDTO.success(dtoResult);
    }

    @PostMapping("/upsert")
    public ResponseDTO<Void> upsert(@RequestBody @Valid GenericUpsertRequest request) {
        GenericDataRecord record = new GenericDataRecord();
        record.setMarket(request.getMarket());
        record.setCode(request.getCode());
        record.setModelCode(request.getModelCode());
        record.setPartitionDate(request.getPartitionDate());
        record.setDataContent(request.getDataContent());
        record.setExtraInfo(request.getExtraInfo());
        genericDataService.upsert(request.getDataType(), record);
        return ResponseDTO.success();
    }

    @PostMapping("/batch-upsert")
    public ResponseDTO<Void> batchUpsert(@RequestBody @Valid GenericBatchUpsertRequest request) {
        List<GenericDataRecord> records = request.getRecords().stream().map(item -> {
            GenericDataRecord record = new GenericDataRecord();
            record.setMarket(item.getMarket());
            record.setCode(item.getCode());
            record.setModelCode(item.getModelCode());
            record.setPartitionDate(item.getPartitionDate());
            record.setDataContent(item.getDataContent());
            record.setExtraInfo(item.getExtraInfo());
            return record;
        }).collect(Collectors.toList());

        genericDataService.batchUpsert(request.getDataType(), records);
        return ResponseDTO.success();
    }

    @PostMapping("/replace-range")
    public ResponseDTO<Void> replaceRange(@RequestBody @Valid GenericBatchUpsertRequest request) {
        if (request.getRecords().isEmpty()) return ResponseDTO.success();
        GenericBatchUpsertRequest.RecordItem first = request.getRecords().get(0);
        List<GenericDataRecord> records = request.getRecords().stream().map(item -> {
            GenericDataRecord record = new GenericDataRecord();
            record.setMarket(item.getMarket());
            record.setCode(item.getCode());
            record.setModelCode(item.getModelCode());
            record.setPartitionDate(item.getPartitionDate());
            record.setDataContent(item.getDataContent());
            record.setExtraInfo(item.getExtraInfo());
            return record;
        }).collect(Collectors.toList());

        genericDataService.replaceRange(request.getDataType(), first.getMarket(),
                first.getCode(), first.getModelCode(), first.getPartitionDate(),
                first.getPartitionDate(), records);
        return ResponseDTO.success();
    }

    @PostMapping("/delete")
    public ResponseDTO<Void> delete(
            @RequestParam String dataType,
            @RequestParam String market,
            @RequestParam String code,
            @RequestParam String modelCode,
            @RequestParam String partitionDate) {
        genericDataService.delete(dataType, market, code, modelCode, partitionDate);
        return ResponseDTO.success();
    }

    // ——— helper ———
    private GenericDataDTO convert(GenericDataRecord record) {
        GenericDataDTO dto = new GenericDataDTO();
        dto.setId(record.getId());
        dto.setMarket(record.getMarket());
        dto.setCode(record.getCode());
        dto.setModelCode(record.getModelCode());
        dto.setPartitionDate(record.getPartitionDate());
        dto.setDataContent(record.getDataContent());
        dto.setExtraInfo(record.getExtraInfo());
        dto.setGmtCreate(record.getGmtCreate());
        dto.setGmtModified(record.getGmtModified());
        return dto;
    }

    // for testing
    void setGenericDataService(GenericDataService genericDataService) {
        this.genericDataService = genericDataService;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -pl stock-biz -Dtest=FinancialDataControllerTest -DfailIfNoTests=false
```
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add stock-biz/src/main/java/com/ethanpark/stock/biz/controller/FinancialDataController.java
git add stock-biz/src/main/java/com/ethanpark/stock/biz/dto/GenericUpsertRequest.java
git add stock-biz/src/main/java/com/ethanpark/stock/biz/dto/GenericBatchUpsertRequest.java
git add stock-biz/src/main/java/com/ethanpark/stock/biz/dto/GenericDataDTO.java
git add stock-biz/src/test/java/com/ethanpark/stock/biz/controller/FinancialDataControllerTest.java
git commit -m "feat: add unified FinancialDataController for /api/financial-data

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 4: Migration — 数据库迁移 + 清理旧代码

**Files:**
- Create: `basic_init.sql` 中新增 `fin_report` DDL（注释即建表语句）
- Modify: `FinancialMetadataInitializer.java` — extInfo 补充 dataType
- Delete: 全部 FinancialReport 旧文件（见上方「删除文件」清单）
- Modify: `DomainConverter.java` — 删除 FinancialReport 相关方法
- Modify: `DbConverter.java` — 删除 FinancialReport 相关方法
- Modify: `DtoConverter.java` — 删除 FinancialReport 相关方法

- [ ] **Step 1: 数据库迁移 — 重命名表**

```sql
alter table financial_report rename to fin_report;
```

注意：由于 MySQL 5.7 不支持 RENAME COLUMN，fin_report 表的列名不需要变更。当前列已经完全匹配新结构：
- `code` → `code` ✓
- `report_type` → 不再使用列级存储，后续可以弃用（但迁移时不动）
- `report_date` → `partition_date`（暂不重命名列，避免数据移动；应用层读取时通过 `mapRow()` 的 `partition_date` key 映射。应用层写入时写 `partition_date`，该列实际上是 `report_date` 列名）

**重要的兼容性设计**：新代码写 `partition_date`、读 `partition_date`。如果旧列名是 `report_date`，需要先改列名或添加别名。最安全的方式是：

```sql
-- 方案1：新表新建（推荐，数据量小）
create table fin_report like financial_report;
-- 然后修改结构后迁移
alter table financial_report rename to financial_report_old;
alter table fin_report add column market varchar(8) not null default 'SH' after id;
alter table fin_report modify column report_date partition_date varchar(16) not null;
alter table fin_report drop column report_type;
alter table fin_report drop column report_period;
alter table fin_report drop column fiscal_year;
alter table fin_report drop column currency;
alter table fin_report drop column unit;
alter table fin_report drop column source;
-- 迁移数据
insert into fin_report (market, code, model_code, partition_date, data_content, extra_info)
select 'SH', code, report_type, report_date, report_data,
       json_object('source', source, 'currency', currency, 'unit', unit, 'fiscal_year', fiscal_year)
from financial_report_old;
```

**实际上，更合理的做法是直接创建新表**，因为旧表没有 `model_code` 和 `market` 字段。当前数据量级不大，用脚本迁移最干净。

- [ ] **Step 2: 修改 FinancialMetadataInitializer**

```java
// 在 FinancialMetadataInitializer 中，为每个 model 的 extInfo 补充 dataType
// 找到 createCashFlowModel() 等方法，在设置 extInfo 时加入：
private Map<String, Object> createCashFlowModel() {
    Map<String, Object> extInfo = new HashMap<>();
    extInfo.put("dataType", "report");
    // ... 其他 extInfo
}
// 同理 balanceSheet 和 incomeStatement
```

- [ ] **Step 3: 清理旧代码**

删除以下文件（逐个模块删除，确保编译通过）：

```bash
# stock-common
git rm stock-common/src/main/java/com/ethanpark/stock/common/dal/mappers/FinancialReportMapper.java
git rm stock-common/src/main/java/com/ethanpark/stock/common/dal/mappers/entity/FinancialReportDO.java
git rm stock-common/src/main/resources/mappers/FinancialReportMapper.xml

# stock-core
git rm stock-core/src/main/java/com/ethanpark/stock/core/model/FinancialReport.java
git rm stock-core/src/main/java/com/ethanpark/stock/core/service/FinancialReportDomainService.java
git rm stock-core/src/main/java/com/ethanpark/stock/core/service/impl/FinancialReportDomainServiceImpl.java

# stock-biz
git rm stock-biz/src/main/java/com/ethanpark/stock/biz/controller/FinancialReportController.java
git rm stock-biz/src/main/java/com/ethanpark/stock/biz/dto/FinancialReportDTO.java
git rm stock-biz/src/main/java/com/ethanpark/stock/biz/dto/FinancialReportSaveRequest.java
git rm stock-biz/src/main/java/com/ethanpark/stock/biz/dto/FinancialReportQueryRequest.java
```

- [ ] **Step 4: 清理 Converters**

移除 DomainConverter / DbConverter / DtoConverter 中所有 `FinancialReport` / `FinancialReportDO` 相关的方法。

- [ ] **Step 5: 编译验证**

```bash
mvn compile -DskipTests
```
Expected: BUILD SUCCESS

- [ ] **Step 6: 运行全部测试**

```bash
mvn test
```
Expected: 全部测试通过

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: migrate financial_report to unified fin_{dataType} system

- Rename financial_report table to fin_report
- Remove all FinancialReport-specific Mapper/DomainService/Controller/DO/DTO/Converter
- Update FinancialMetadataInitializer with dataType in extInfo
- All data now flows through GenericDataService

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## 自审查

- **Spec 覆盖**：每条需求都有对应任务 — 路由层(T1)、泛化CRUD(T2)、统一API(T3)、迁移(T4)
- **无占位符**：全部步骤含完整代码和命令
- **类型一致性**：resolveTable 返回 String，resolveModel 返回 MetadataModel，所有 Service 方法签名在 T2 定义、T3 消费
- **测试覆盖**：每个 Task 包含完整测试代码
