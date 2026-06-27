# Code Review — 建设标准的元数据系统 阶段二

## 审查摘要
- 审查文件数: 30
- 发现问题数: 12
- CRITICAL: 1 | HIGH: 4 | MEDIUM: 5 | LOW: 2
- 结论: **BLOCK** — 有 CRITICAL/HIGH 问题需修复

## CRITICAL
### 1. listEnums N+1 查询 — Controller 中对每个枚举调用 getEnumUsage
**修复**: 新增 `listEnumsWithUsage()` 批量查询方法

## HIGH
### 2. deleteEnum 软删除但 selectAll 未过滤 DELETED
**修复**: selectAll SQL 添加 `WHERE status != 'DELETED'`

### 3. saveModel/saveEnum 未校验 code 唯一性
**修复**: insert 前调用 selectByCode 检查重复

### 4. saveEnum 先删后插枚举值缺少事务
**修复**: `@Transactional` 注解

### 5. MetadataIntegrationController 路径冲突
**修复**: 改为 `@RequestMapping("/api/metadata/indicator")`
