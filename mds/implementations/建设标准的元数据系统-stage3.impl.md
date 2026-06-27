# 阶段三实现 — 前端页面 + 集成测试（T-8 ~ T-11）

## T-8: 元数据模型列表与管理页面

### 路径
- 列表页: `stock-frontend/src/pages/Metadata/ModelList/index.jsx`
- 编辑页: `stock-frontend/src/pages/Metadata/ModelEditor/index.jsx`

### 功能
- 模型列表页（Table）：展示 name/code/modelType/status/description，操作列有"查看详情"和"编辑"
- 新建/编辑表单：Modal 表单，字段：name/code/modelType/description
- 字段编辑在 ModelEditor 中：展示已有关联字段列表，支持新增字段的 Modal
- 新增字段时可选择绑定枚举，展示枚举列表供选择

## T-9: 枚举管理页面

### 路径
- `stock-frontend/src/pages/Metadata/EnumManager/index.jsx`

### 功能
- 枚举列表（Table）：展示 name/code/description/status/refModelCount/refFieldCount
- 每行的引用数可点击查看引用详情（用 Modal 展示 referencedBy）
- 新建/编辑枚举 Modal：编辑 name/code/description + 枚举值列表（可增删）
- 删除按钮：有引用时提示无法删除

## T-10: 模型详情展示页

### 路径
- `stock-frontend/src/pages/Metadata/ModelDetail/index.jsx`

### 功能
- 展示模型基本信息 + 字段列表（Table）+ 绑定的枚举信息
- Schema 校验按钮：点击后调用 validate API，展示校验结果

## T-11: 集成测试

### 路径
- `stock-web/src/test/java/com/ethanpark/stock/web/MetadataIntegrationTest.java`

### 测试内容
- TestRestTemplate 或 MockMvc 测试全部 14 个 API 端点
- 覆盖正常 + 异常场景
- 包含模型 CRUD、字段 CRUD、枚举 CRUD、Schema 校验、枚举绑定/解绑、枚举使用查询

## 前端关键模式

### API 服务层 (参考 strategy.js)
```javascript
import { request } from "@umijs/max";
export async function listModels() {
    return request("/api/metadata/model/list.json");
}
```

### 页面模式 (参考 StrategyList/index.jsx)
- PageContainer + Table + useEffect + useState
- Modal 用于新建/编辑表单
- 使用 ProTable 或 Ant Design Table

### 路由 (config/routes.js)
- 在 `/operation` 下添加元数据管理子路由
