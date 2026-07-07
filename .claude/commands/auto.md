---
description: 全自动开发流程：依次执行 analyze → tdd → dev → santa → verify → quality，全程文档上传飞书，完成后更新标题
---

# Auto — 全自动开发流程

通过 **Workflow** 模式编排全部子命令，以引用模式依次执行 analyze → tdd → dev → santa → verify → quality，全程文档上传飞书归档。

## 参数

```
/auto <飞书文档链接>
```

## 执行流程

### Step 1: 读取飞书文档

1. 调用 **lark-doc** skill 读取文档内容，获取 `title` / `token` / `content`
2. 调用 **lark-drive** 读取该文档的评论（强制，遵循 feishu-doc.md 规则）

### Step 2: 创建 spec 目录

1. 从文档标题提取需求名称（如「建设元数据系统」→ `建设标准的元数据系统`）
2. 扫描 `spec/` 目录确定下一个序号（`feature-01` ~ `feature-N` → 下一个为 `feature-{N+1}`）
3. 创建目录 `spec/feature-{序号}/`

### Step 3: 启动 Workflow 编排

调用 `Workflow` 工具，传入以下内联脚本（参考 `.claude/workflows/auto.js` 中的完整脚本结构）：

Workflow 包含 4 个阶段，6 个步骤：

```
Phase 1: 📋 分析
  Step 1 → architect agent   方案设计 + TODO 拆分
  Step 2 → testcase-generator 测试用例生成

Phase 2: 🔧 开发
  Step 3 → dev                启动开发环境

Phase 3: 🛡️ 质量
  Step 4 → santa              Santa 质量门禁循环
  Step 5 → verify             项目验证（编译 + 测试）
  Step 6 → quality            代码质量检查

Phase 4: 📤 归档
         上传所有文档到飞书 + 更新标题
```

#### Workflow 脚本结构

```javascript
export const meta = {
  name: 'auto-dev-flow',
  description: '全自动开发流程编排',
  phases: [
    { title: '分析', detail: '方案设计 + TODO + 测试用例' },
    { title: '开发', detail: '启动后端 + 前端' },
    { title: '质量', detail: 'Santa + Verify + Quality' },
    { title: '归档', detail: '上传飞书 + 更新标题' },
  ],
};

const specDir = args.specDir;
const docTitle = args.docTitle;
const docToken = args.docToken;
const reqContent = args.reqContent;

// ────────── Phase 1: 分析 ──────────

phase('分析');

log('→ Step 1: 方案设计 + TODO');
const designOut = await agent(`
  项目: 股票策略回测系统（Spring Boot + React）
  需求文档: ${reqContent}
  工作目录: ${specDir}

  请产出：
    1. 方案设计文档 → ${specDir}/方案设计-${docTitle}.md
       - 使用 Spec 模板（背景与目标、方案设计、备选方案、测试计划、迁移发布、影响面分析）
    2. TODO 拆分 → ${specDir}/TODO-${docTitle}.md
       - 按阶段拆分，每任务 0.5~2 人天
`, { label: '方案设计', agentType: 'architect' });

log('→ Step 2: 测试用例生成');
const testcaseOut = await agent(`
  基于方案设计文档生成测试用例：
  → ${specDir}/测试用例-${docTitle}.md

  要求：
  - 每个接口至少 3 个用例：正常 + 边界 + 异常
  - curl 命令完整可执行
  - 包含端到端场景
`, { label: '测试用例', agentType: 'testcase-generator' });

// ────────── Phase 2: 开发 ──────────

phase('开发');

log('→ Step 3: 启动开发环境');
await agent(`
  启动本地开发环境：
  1. 启动后端 (port 8080): mvn spring-boot:run -Dspring-boot.run.profiles=test -pl stock-web
  2. 启动前端 (port 8000): cd stock-frontend && npm run start:no-mock
  3. 确认后端健康: curl http://localhost:8080/api/stock-strategy/list.json
`, { label: '启动环境' });

// ────────── Phase 3: 质量 ──────────

phase('质量');

log('→ Step 4: Santa 质量门禁循环');
await agent(`
  执行 Santa 质量门禁循环（最大 3 轮）：
  每轮: 扫描问题 → 修复 → 验证
  直到无 CRITICAL/HIGH 问题或达到轮数上限
`, { label: 'Santa 门禁', agentType: 'reviewer' });

log('→ Step 5: 项目验证');
await agent(`
  执行 full 模式验证：
  - 后端编译 + 运行全部测试
  - 架构守卫检查 (ArchitectureTest)
  - 核心模块单元测试
  - 集成测试 (MetadataIT)
`, { label: '验证', agentType: 'tester' });

log('→ Step 6: 代码质量检查');
await agent(`
  执行 check 模式质量检查：
  - 静态代码分析
  - 安全扫描
  - 编码规范检查
  - 输出问题清单
`, { label: '质量检查', agentType: 'reviewer' });

// ────────── Phase 4: 归档（由主流程完成）─────────

log('→ 开发流程已完成，等待主流程归档到飞书');
return { specDir, docTitle, docToken };
```

### Step 4: 处理 Workflow 结果

Workflow 返回后，获取产物文件路径：

```
spec/feature-{序号}/
├── 方案设计-{需求名称}.md
├── TODO-{需求名称}.md
├── 测试用例-{需求名称}.md
```

### Step 5: 上传飞书归档

调用 **lark-wiki** skill 将上述文件作为子文档上传到原始飞书文档下。

### Step 6: 更新文档标题

调用 **lark-doc** skill 将原始文档标题修改为：

```
{序号}.【已完成】{原标题}
```

### Step 7: 输出摘要

```
✅ Auto 全自动开发流程已完成！

📋 产出清单（spec/feature-{序号}/）
  ├── 方案设计-{需求名称}.md
  ├── TODO-{需求名称}.md
  └── 测试用例-{需求名称}.md

🏷️ 飞书文档: {序号}.【已完成】{原标题}

📊 流程统计
  ├── 分析阶段: ✅ 方案设计 + TODO + 测试用例
  ├── 开发阶段: ✅ 开发环境已启动
  ├── 质量阶段: ✅ Santa + Verify + Quality
  └── 归档阶段: ✅ 子文档已上传

💡 后续步骤
  ├── 查看方案设计 → spec/feature-{序号}/方案设计-{需求名称}.md
  ├── 查看任务拆分 → spec/feature-{序号}/TODO-{需求名称}.md
  ├── 查看测试用例 → spec/feature-{序号}/测试用例-{需求名称}.md
  └── 开始开发 → /dev backend
```

---

## 经验采集

<!-- 命令执行结束时，如有错误请记录以下信息： -->
> - 错误描述（error）
> - 修正方式（correction）
> - 问题组件（component）
>
> 记录方式：写入 `~/.claude/evolve/pending/` 目录下的时间戳文件。
