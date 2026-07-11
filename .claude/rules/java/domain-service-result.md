# Domain Service 返回值规范 — Result 包裹

> 本规则定义 Domain 层（DomainService）返回类型的约束：所有返回值必须包裹在 `Result<T>` 或 `PageResult<T>` 中。

## 1. 原则

**Domain 层不得返回原始类型。** 上游调用方（Controller、其他 Service）需要通过 `Result.isSuccess()` 感知操作是否成功，而不是靠 null 判断或异常捕获。

```
✅ 正确：Result<GenericDataRecord>   → 上游检查 isSuccess() → 检查 getData()
✅ 正确：PageResult<GenericDataRecord> → 上游直接读取分页信息
❌ 错误：GenericDataRecord            → 上游靠 null 判断"不存在"
❌ 错误：List<GenericDataRecord>       → 上游无法区分"查不到"和"出错"
```

## 2. 规则

### 2.1 查询方法返回 `Result<T>`

```java
// ✅ 正确
Result<GenericDataRecord> get(String key);
Result<List<String>> listCodes(String market);

// ❌ 错误
GenericDataRecord get(String key);
List<String> listCodes(String market);
```

### 2.2 查询方法返回 `PageResult<T>`

分页查询可以直接返回 `PageResult<T>`，因为它本身包含了成功/失败的信息结构：

```java
// ✅ 正确
PageResult<GenericDataRecord> queryPage(String dataType, GenericDataQuery query);

// ❌ 错误
List<GenericDataRecord> queryPage(String dataType, GenericDataQuery query);
```

### 2.3 写入方法可以返回 `void` 或 `Result<Void>`

对于写入操作（upsert / delete），不返回数据的可以保持 `void`：

```java
// ✅ 正确 — 写入不返回数据
void upsert(String dataType, GenericDataRecord record);
void delete(String key);

// ✅ 正确 — 返回操作结果的写入
Result<Void> batchUpsert(String dataType, List<GenericDataRecord> records);
```

### 2.4 `Result<T>` 的使用

```java
// 返回成功（带数据）
return Result.ok(data);
// 返回成功（无数据 — 表示"不存在"等正常场景）
return Result.ok(null);
// 返回失败
return Result.fail("数据不存在");
```

### 2.5 上游调用方检查

```java
Result<GenericDataRecord> result = service.get(key);
if (!result.isSuccess()) {
    return ResponseDTO.error(500, result.getMsg());
}
if (result.getData() == null) {
    return ResponseDTO.success(); // not found
}
return ResponseDTO.success(convert(result.getData()));
```

---

## 3. 适用范围

| 层 | 是否适用 | 说明 |
|----|---------|------|
| `*DomainService` 接口 + impl | **适用** | 所有查询方法必须包裹 |
| Controller | 不适用 | Controller 返回 `ResponseDTO<T>` |
| Action（Process Engine） | 不适用 | Action 在 ProcessContext 中传递结果 |
| Mapper | 不适用 | Mapper 返回 DO，由 DomainService 转换 |

---

## 4. 检视清单

- [ ] 查询方法返回 `Result<T>` 而非原始类型？
- [ ] 分页查询返回 `PageResult<T>` 而非 `List<T>`？
- [ ] 上游调用方检查 `result.isSuccess()` 后再读取 `result.getData()`？
- [ ] `Result.fail()` 仅在业务可预期的失败时使用（非异常场景）？
