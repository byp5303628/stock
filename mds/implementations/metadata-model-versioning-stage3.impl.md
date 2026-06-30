# 阶段3 实现报告: Controller + DTO 层

## 文件变更
| 文件 | 操作 |
|------|------|
| ModelVersionDTO.java | 新增 |
| SwitchVersionRequest.java | 新增 |
| PublishModelRequest.java | 新增 |
| MetadataModelDTO.java | 修改: 新增 currentVersion |
| ErrorCode.java | 修改: 新增 42006 |
| DtoConverter.java | 修改: 新增 toDto(MetadataModelVersion) |
| MetadataController.java | 修改: 3新端点 + 2增强端点 |

## 编译状态
- 全部模块通过

## 测试结果
- 57 tests passed (31+16+1+9)
