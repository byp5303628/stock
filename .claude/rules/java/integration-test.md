# Java Integration Test 规范

> 本规则扩展 [common/testing.md](../common/testing.md) 和 [java/testing.md](../java/testing.md)，定义集成测试（`*IT.java`）的组织标准和编写规范。

## 设计原则

集成测试应按照**业务场景**组织，而非按 API 端点或 Controller 方法组织。

### 反例 vs 正例

```
❌ 反例：按端点平铺
  testListModels
  testSaveModel
  testGetModel
  testSaveField
  testPublishModel
  testSwitchVersion

✅ 正例：按场景分组
  场景: 模型生命周期       → 创建模型 → 添加字段 → 发布 → 变更 → 再发布
  场景: Schema 查看与校验  → 查看 Schema → 校验通过 → 校验失败
  场景: 版本管理           → 版本切换 → 按版本查 Schema
```

## 规则

### 规则 1：场景分组（取代线性排列）

- 禁止 `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` 在顶层类使用
- 使用 `@Nested` 按业务场景分组，每个场景独立的 `@DisplayName`

```java
// ❌ 禁止 — 线性排列，静态变量传递状态
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MetadataIT {
    private static Long modelId;    // static 跨测试共享

    @Test @Order(1) void testStepA() { modelId = create(); }
    @Test @Order(2) void testStepB() { use(modelId); }
    @Test @Order(3) void testStepC() { use(modelId); }
    // 加新测试不知道该插在哪
}

// ✅ 正确 — 按场景分组
class MetadataIT {
    @Nested @DisplayName("场景：模型生命周期 — 创建→字段→发布→变更→再发布")
    class ModelLifecycle { ... }

    @Nested @DisplayName("场景：Schema 查看与校验")
    class SchemaView { ... }
}
```

### 规则 2：场景之间数据隔离

- 场景之间**不通过 `static` 字段**传递状态，static 字段仅限 `@Nested` 内部使用
- 每个 `@Nested` 类使用自己的字段维护状态，不跨场景共享

```java
class MetadataIT {
    // ——— 场景一 ———
    @Nested @DisplayName("场景：模型生命周期")
    class ModelLifecycle {
        private static Long modelId;    // static ✅ 仅在该 @Nested 内部共享
        // ...
    }

    // ——— 场景二 ———
    @Nested @DisplayName("场景：Schema 查看")
    class SchemaView {
        private static Long modelId;    // static ✅ 与该 @Nested 外的 modelId 无关
        // ...
    }
}
```

> **注意**：JUnit 5 对每个 `@Test` 方法都会创建新的 `@Nested` 实例，因此实例字段在 `@Order` 顺序测试间**不持久**。`@Nested` 内部的 `static` 字段用于同一个场景内多个顺序测试间的数据共享，这不违反"场景间隔离"原则。

### 规则 3：场景内顺序 vs 独立

根据场景特点选择测试策略：

| 场景类型 | 策略 | 说明 |
|---------|------|------|
| **流程型**（如创建→发布→变更） | `@TestMethodOrder(OrderAnnotation.class)` + `@Order` | 测试一个完整的业务流程，步骤之间有依赖 |
| **查询型**（如查看详情、Schema） | 独立测试 + `@BeforeEach` 准备数据 | 每个测试不依赖其他测试的执行结果 |

```java
// 流程型 — 有序
@Nested @DisplayName("场景：模型生命周期")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ModelLifecycle {
    @Test @Order(1) @DisplayName("创建模型") void createModel() { ... }
    @Test @Order(2) @DisplayName("添加字段") void addField() { ... }
    @Test @Order(3) @DisplayName("发布模型") void publishModel() { ... }
}

// 查询型 — 独立
@Nested @DisplayName("场景：Schema 查看")
class SchemaView {
    private static Long modelId;            // static: 跨 @Test 持久

    @BeforeEach
    void setUp() {
        if (modelId != null) return;        // 只初始化一次
        modelId = createModel();
    }

    @Test @DisplayName("查看 JSON Schema") void viewSchema() { ... }
    @Test @DisplayName("Schema 校验通过") void validatePass() { ... }
}
```

### 规则 4：提取共享 Helper

将重复的 HTTP 调用模式提取到外层类的 helper 方法中，减少 `@Nested` 类内的重复代码：

```java
class MetadataIT {
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;

    // ——— 共享 Helper ———
    <T> ResponseDTO<T> get(String url, ParameterizedTypeReference<ResponseDTO<T>> type) {
        return restTemplate.exchange(baseUrl() + url, HttpMethod.GET, null, type).getBody();
    }

    <T, R> ResponseDTO<R> post(String url, T body, ParameterizedTypeReference<ResponseDTO<R>> type) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(baseUrl() + url, HttpMethod.POST, entity, type).getBody();
    }

    // ...
}
```

### 规则 5：`@DisplayName` 格式

| 层级 | 格式 | 示例 |
|------|------|------|
| `@Nested` 类 | `"场景：<动词短语> — <子步骤>"` | `"场景：模型生命周期 — 创建→字段→发布"` |
| `@Test` 方法 | `"<动词><对象><预期>"` | `"创建模型并确认可查询"` |

命名聚焦业务意图，而不是实现细节：
- ✅ `"创建模型并确认可查询"` — 说明业务意图
- ❌ `"testSaveAndGetModel"` — 说明调用了 save 和 get 方法

## 文件大小红线

- 单个 `*IT.java` 文件不超过 600 行
- 如果超过，将 `@Nested` 场景提取为独立的 `*IT.java` 文件
- 提取时每个文件一个 `@SpringBootTest`，通过 `@TestInstance(Lifecycle.PER_CLASS)` 避免重复启动

## 检视清单

```markdown
- [ ] 是否按场景分组而非按端点平铺？
- [ ] 场景之间是否无静态变量依赖？
- [ ] 每个场景的 `@DisplayName` 是否说明业务意图？
- [ ] 流程型场景有 `@Order`，查询型场景独立？
- [ ] 重复的 HTTP 调用是否提取到 helper 方法？
- [ ] 文件是否 < 600 行？
```
