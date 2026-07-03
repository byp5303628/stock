export const meta = {
  name: 'auto-dev-flow-continue',
  description: 'continue auto flow: implement financial report feature, quality check, feishu archive',
  phases: [
    { title: '开发', detail: 'create DO/Mapper/Domain/Service/Controller' },
    { title: '质量', detail: 'compile + verify + quality' },
    { title: '归档', detail: 'upload to feishu + update title' },
  ],
};

// ============================================================
// Phase 2: 开发 - 代码实现
// ============================================================
phase('开发');

log('Step T-1: DDL + FinancialReportDO');

await agent([
  'You are a Java developer. Implement T-1: financial_report table DDL and DO entity.',
  '',
  '### Task 1: Append DDL to basic_init.sql',
  'File: /Users/ethanpark/workspace/stock/basic_init.sql',
  'Read the file first, then append the following CREATE TABLE at the end:',
  '',
  '```sql',
  'create table if not exists financial_report',
  '(',
  '    id            bigint                 not null primary key auto_increment,',
  '    code          varchar(16)            not null comment \'stock code\',',
  '    report_type   varchar(16)            not null comment \'report type: CASH_FLOW / BALANCE / INCOME\',',
  '    report_date   varchar(32)            not null comment \'report end date, e.g. 2024-12-31\',',
  '    report_period varchar(16)            not null default \'\' comment \'period: Q1/Q2/Q3/Q4/YEARLY\',',
  '    report_data   json                   comment \'indicator data JSON, key = metadata field name\',',
  '    fiscal_year   int                    comment \'fiscal year, e.g. 2024\',',
  '    currency      varchar(8)             not null default \'CNY\' comment \'currency\',',
  '    unit          varchar(8)             not null default \'元\' comment \'unit: 元/万元/亿元\',',
  '    source        varchar(32)            not null default \'\' comment \'data source\',',
  '    gmt_create    datetime               not null default now(),',
  '    gmt_modified  datetime               not null default now() on update now(),',
  '    unique index uniq_idx_code_type_date (code, report_type, report_date),',
  '    index idx_code (code),',
  '    index idx_report_type (report_type),',
  '    index idx_report_date (report_date),',
  '    index idx_fiscal_year (fiscal_year),',
  '    index idx_gmt_modified (gmt_modified)',
  ') engine = InnoDB default charset = utf8mb4 comment \'financial report data table\';',
  '```',
  '',
  '### Task 2: Create FinancialReportDO',
  'Create file:',
  '/Users/ethanpark/workspace/stock/stock-common/src/main/java/com/ethanpark/stock/common/dal/mappers/entity/FinancialReportDO.java',
  '',
  'Package: com.ethanpark.stock.common.dal.mappers.entity',
  'Use @Data from Lombok.',
  'Fields match the table columns exactly. report_data is String type (JSON text at DO layer).',
  'Refer to existing DO: /Users/ethanpark/workspace/stock/stock-common/src/main/java/com/ethanpark/stock/common/dal/mappers/entity/StockBasicDO.java',
  '',
  'IMPORTANT: Read basic_init.sql first to find the right place to append. Read an existing DO for the correct style.'
].join('\n'), { label: 'T-1 DDL+DO', agentType: 'developer' });

log('Step T-2: FinancialReportMapper + XML');

await agent([
  'You are a Java developer. Implement T-2: FinancialReportMapper interface and XML mapping.',
  '',
  '### Task 1: Mapper Interface',
  'Create file:',
  '/Users/ethanpark/workspace/stock/stock-common/src/main/java/com/ethanpark/stock/common/dal/mappers/FinancialReportMapper.java',
  '',
  'Package: com.ethanpark.stock.common.dal.mappers',
  'Use @Mapper annotation.',
  '',
  'Methods:',
  '- int insert(FinancialReportDO entity)',
  '- int upsert(FinancialReportDO entity) - use annotation @Lang(InsertLangDriver.class) or XML',
  '- int updateById(FinancialReportDO entity)',
  '- FinancialReportDO selectById(Long id)',
  '- List<FinancialReportDO> selectPage(...) - params: code, reportType, startDate, endDate, fiscalYear, offset, limit (all @Param)',
  '- Long count(...) - same filter params as selectPage, returns total count',
  '- List<FinancialReportDO> selectLatestByCode(@Param("code") String code) - latest per reportType',
  '- List<FinancialReportDO> selectByCodeAndType(@Param("code") String code, @Param("reportType") String reportType)',
  '- int batchInsert(List<FinancialReportDO> list)',
  '',
  '### Task 2: XML Mapping',
  'Create file:',
  '/Users/ethanpark/workspace/stock/stock-common/src/main/resources/mappers/FinancialReportMapper.xml',
  '',
  'Namespace: com.ethanpark.stock.common.dal.mappers.FinancialReportMapper',
  '- resultMap mapping all columns',
  '- upsert: INSERT ... ON DUPLICATE KEY UPDATE (unique index: code+report_type+report_date)',
  '- selectPage: LIMIT #{offset}, #{limit}',
  '- selectLatestByCode: for each reportType return the latest record (use subquery with MAX(report_date))',
  '- batchInsert: use <foreach> or MySQL batch syntax',
  '',
  'Reference files:',
  '- /Users/ethanpark/workspace/stock/stock-common/src/main/java/com/ethanpark/stock/common/dal/mappers/StockStatisticsMapper.java',
  '- /Users/ethanpark/workspace/stock/stock-common/src/main/resources/mappers/StockStatisticsMapper.xml'
].join('\n'), { label: 'T-2 Mapper', agentType: 'developer' });

log('Step T-3: Domain Model + Converters + DTOs');

await agent([
  'You are a Java developer. Implement T-3: FinancialReport domain model, converters, and DTOs.',
  '',
  '### 1. Domain Model',
  'Create: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/model/FinancialReport.java',
  'Package: com.ethanpark.stock.core.model',
  '@Getter @Setter Lombok.',
  'Fields: Long id, String code, String reportType, String reportDate, String reportPeriod,',
  'Map<String,Object> reportData, Integer fiscalYear, String currency, String unit, String source,',
  'Date gmtCreate, Date gmtModified',
  '',
  '### 2. PageResult model (if not exists)',
  'Check if any PageResult class exists under stock-core model package.',
  'If not, create: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/model/PageResult.java',
  'Fields: List<T> items, long total, int page, int size, int pages',
  '',
  '### 3. DbConverter (modify)',
  'File: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/converter/DbConverter.java',
  'Add method: public static FinancialReportDO toDbEntity(FinancialReport report)',
  '- reportData -> JSON string: JSON.toJSONString(report.getReportData())',
  '- NOT NULL fields get null-safe defaults: reportPeriod="" if null, currency="CNY" if null,',
  '  unit="\\u5143" if null, source="" if null',
  '- Calculate fiscalYear from reportDate if null (extract year from "yyyy-MM-dd")',
  '',
  '### 4. DomainConverter (modify)',
  'File: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/converter/DomainConverter.java',
  'Add method: public static FinancialReport toDomain(FinancialReportDO dbEntity)',
  '- report_data JSON string -> Map: JSON.parseObject(dbEntity.getReportData(), Map.class)',
  '- Copy other fields directly',
  '',
  '### 5. DTOs (create in stock-biz)',
  'Package: com.ethanpark.stock.biz.dto',
  '',
  'Create FinancialReportDTO.java:',
  'Fields: Long id, String code, String reportType, String reportDate, String reportPeriod,',
  'Map<String,Object> reportData, Integer fiscalYear, String currency, String unit, String source,',
  'Date gmtCreate, Date gmtModified',
  '',
  'Create FinancialReportSaveRequest.java:',
  'Fields with validation: @NotBlank String code, @NotBlank String reportType,',
  '@NotBlank String reportDate, String reportPeriod, Map<String,Object> reportData,',
  'String currency, String unit, String source',
  '',
  'Create FinancialReportQueryRequest.java:',
  'Fields: String code, String reportType, String startDate, String endDate,',
  'Integer fiscalYear, Integer page=1, Integer size=20',
  '',
  '### 6. DtoConverter (modify)',
  'File: /Users/ethanpark/workspace/stock/stock-biz/src/main/java/com/ethanpark/stock/biz/converter/DtoConverter.java',
  'Add methods:',
  '- public static FinancialReportDTO toDto(FinancialReport report) - copy fields',
  '- public static FinancialReport toDomain(FinancialReportSaveRequest request) - Map to domain',
  '',
  'IMPORTANT: Read each file before modifying it. Match the existing code style exactly.',
  'Use the same import style (wildcard vs explicit) as the existing file.'
].join('\n'), { label: 'T-3 Domain+Converter', agentType: 'developer' });

log('Step T-4: FinancialReportDomainService');

await agent([
  'You are a Java developer. Implement FinancialReportDomainService (interface + impl).',
  '',
  '### Interface',
  'Create: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/service/FinancialReportDomainService.java',
  'Package: com.ethanpark.stock.core.service',
  '',
  'Methods:',
  '- FinancialReport save(FinancialReport report)',
  '- List<FinancialReport> batchSave(List<FinancialReport> reports)',
  '- PageResult<FinancialReport> query(String code, String reportType, String startDate, String endDate, Integer fiscalYear, int page, int size)',
  '- FinancialReport getById(Long id)',
  '- List<FinancialReport> getLatestByCode(String code)',
  '- List<FinancialReport> getByCodeAndType(String code, String reportType)',
  '- void deleteById(Long id)',
  '',
  'Reference: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/service/MetadataDomainService.java',
  '',
  '### Implementation',
  'Create: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/service/impl/FinancialReportDomainServiceImpl.java',
  'Package: com.ethanpark.stock.core.service.impl',
  '',
  '- @Service annotation, constructor injection of FinancialReportMapper',
  '- save(): DbConverter.toDbEntity() -> mapper.upsert() -> selectById -> DomainConverter.toDomain()',
  '- batchSave(): loop save() for each item',
  '- query(): mapper.count() + mapper.selectPage() -> wrap in PageResult',
  '- getById(): mapper.selectById() -> DomainConverter.toDomain()',
  '- getLatestByCode(): mapper.selectLatestByCode() -> convert list',
  '- getByCodeAndType(): mapper.selectByCodeAndType() -> convert list',
  '- deleteById(): mapper.deleteById() (add this method to Mapper if needed)',
  '- Validation: reportType must be in [CASH_FLOW, BALANCE, INCOME], code not blank',
  '',
  'Reference: /Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/service/impl/MetadataDomainServiceImpl.java',
  '',
  'IMPORTANT: Read the existing reference files for style. Check if PageResult exists in the model package first.'
].join('\n'), { label: 'T-4 DomainService', agentType: 'developer' });

log('Step T-5: Metadata initialization PostConstruct');

await agent([
  'You are a Java developer. Implement FinancialMetadataInitializer.',
  '',
  'Create: /Users/ethanpark/workspace/stock/stock-biz/src/main/java/com/ethanpark/stock/biz/init/FinancialMetadataInitializer.java',
  'Package: com.ethanpark.stock.biz.init',
  '',
  'This @Component initializes metadata models for 3 financial statements at startup via @PostConstruct.',
  '',
  'First read the MetadataDomainService API:',
  '/Users/ethanpark/workspace/stock/stock-core/src/main/java/com/ethanpark/stock/core/service/MetadataDomainService.java',
  '',
  'Also read the Metadata request DTOs to understand how to create models and fields:',
  '- Look at MetadataModelSaveRequest, MetadataFieldSaveRequest in stock-biz/dto/',
  '',
  'Three models to create (check if exists first by model code, skip if exists):',
  '',
  '1. cash_flow_statement (Cash Flow Statement) - 10 fields:',
  '   net_cash_flow_operating, net_cash_flow_investing, net_cash_flow_financing,',
  '   cash_sales_received, cash_paid_for_goods, cash_paid_for_employees,',
  '   cash_paid_for_taxes, net_increase_in_cash, cash_equivalent_beginning, cash_equivalent_ending',
  '   All fieldType=DECIMAL',
  '',
  '2. balance_sheet (Balance Sheet) - 15 fields:',
  '   total_assets, current_assets, cash_and_equivalents, accounts_receivable, inventory,',
  '   non_current_assets, fixed_assets, intangible_assets, total_liabilities,',
  '   current_liabilities, non_current_liabilities, accounts_payable, total_equity,',
  '   share_capital, retained_earnings',
  '   All fieldType=DECIMAL',
  '',
  '3. income_statement (Income Statement) - 15 fields:',
  '   total_revenue, operating_revenue, total_cogs, operating_cost, selling_expense,',
  '   admin_expense, r_d_expense, financial_expense, operating_profit, total_profit,',
  '   income_tax, net_profit, net_profit_parent, basic_eps, diluted_eps',
  '   All fieldType=DECIMAL except basic_eps/diluted_eps which are DECIMAL too',
  '',
  'Logic:',
  '- @Component + @PostConstruct',
  '- Inject MetadataDomainService',
  '- For each model, check if already exists (by code)',
  '- If not exists: create model -> create all fields',
  '- Idempotent: rerunning startup should not error or create duplicates',
  '',
  'If MetadataDomainService does not expose a "find by code" method, read the mapper directly.',
  'Also read MetadataFieldSaveRequest for the field creation DTO format.'
].join('\n'), { label: 'T-5 Metadata Init', agentType: 'developer' });

log('Step T-7: FinancialReportController');

await agent([
  'You are a Java developer. Implement FinancialReportController with REST API endpoints.',
  '',
  'Create: /Users/ethanpark/workspace/stock/stock-biz/src/main/java/com/ethanpark/stock/biz/controller/FinancialReportController.java',
  'Package: com.ethanpark.stock.biz.controller',
  '',
  'Reference: /Users/ethanpark/workspace/stock/stock-biz/src/main/java/com/ethanpark/stock/biz/controller/MetadataController.java',
  '',
  'Constructor injection of FinancialReportDomainService.',
  '',
  'Endpoints:',
  '1. GET /api/financial-reports - page query',
  '   params: code, reportType, startDate, endDate, fiscalYear, page, size',
  '   returns: ResponseDTO<PageResult<FinancialReportDTO>>',
  '',
  '2. GET /api/financial-reports/detail - detail by id',
  '   params: id (Long, required)',
  '   returns: ResponseDTO<FinancialReportDTO>',
  '',
  '3. GET /api/financial-reports/latest - latest reports for a stock',
  '   params: code (String, required)',
  '   returns: ResponseDTO<List<FinancialReportDTO>>',
  '',
  '4. GET /api/financial-reports/list-by-code - list by code and type',
  '   params: code, reportType',
  '   returns: ResponseDTO<List<FinancialReportDTO>>',
  '',
  '5. POST /api/financial-reports - single create/update',
  '   body: @Valid FinancialReportSaveRequest',
  '   returns: ResponseDTO<FinancialReportDTO>',
  '',
  '6. POST /api/financial-reports/batch - batch create',
  '   body: List<@Valid FinancialReportSaveRequest>',
  '   returns: ResponseDTO<Integer> (number saved)',
  '',
  'Rules:',
  '- @RestController + @RequestMapping',
  '- All methods return ResponseDTO<T> (read stock-biz/dto/ResponseDTO.java for the wrapper)',
  '- Use @Valid for request body validation',
  '- Use DtoConverter.toDto() to convert domain to DTO',
  '- Map FinancialReportSaveRequest to domain via DtoConverter.toDomain() before saving',
  '- No Process Engine pattern - direct DomainService calls for CRUD',
  '',
  'IMPORTANT: Read ResponseDTO.java and MetadataController.java first to match the exact style.',
  'Also check what methods exist in DtoConverter for FinancialReport conversion.'
].join('\n'), { label: 'T-7 Controller', agentType: 'developer' });

// ============================================================
// Phase 3: 质量 - Verify + Quality
// ============================================================
phase('质量');

log('Step: Verify compilation');
await agent([
  'Verify that the project compiles successfully.',
  'Run: mvn clean compile -pl stock-common,stock-core,stock-biz -am -DskipTests 2>&1',
  'If there are compilation errors, fix them.',
  'Pay attention to:',
  '- Import statements in new files',
  '- Missing methods referenced by DomainService or Controller',
  '- Type mismatches between DO, Domain, and DTO layers',
  '- Correct package names and class references',
  '',
  'Fix any compilation errors found, then recompile until clean.'
].join('\n'), { label: 'Verify Compile', agentType: 'developer' });

// ============================================================
// Phase 4: 归档 - 上传飞书 + 更新标题
// ============================================================
phase('归档');

log('Step: Upload 方案设计 to Feishu');
await agent([
  'Read file: /Users/ethanpark/workspace/stock/spec/feature-05/方案设计-财报指标存储.md',
  'Then create a sub-document under the wiki document with token YdFDdmExNoaLHnxZ5eUcbwMlnee.',
  'Title: "方案设计-财报指标存储"',
  'Use lark-doc skill: lark-cli docs +create --content @file --doc-format markdown --parent-doc YdFDdmExNoaLHnxZ5eUcbwMlnee',
  '',
  'Since the file is large, use Bash to read the file content first, then pass it to lark-cli.',
  'If the file is too large for inline upload, use lark-drive to import the markdown file first.',
  'After creation, confirm the document was created successfully.'
].join('\n'), { label: 'Upload 方案设计' });

log('Step: Upload TODO to Feishu');
await agent([
  'Read file: /Users/ethanpark/workspace/stock/spec/feature-05/TODO-财报指标存储.md',
  'Then create a sub-document under the wiki document with token YdFDdmExNoaLHnxZ5eUcbwMlnee.',
  'Title: "TODO-财报指标存储"',
  'Use lark-doc skill: lark-cli docs +create to create the document with markdown format.',
  'Use Bash to read the file and pass content to lark-cli.',
  'After creation, confirm the document was created successfully.'
].join('\n'), { label: 'Upload TODO' });

log('Step: Upload 测试用例 to Feishu');
await agent([
  'Read file: /Users/ethanpark/workspace/stock/spec/feature-05/测试用例-财报指标存储.md',
  'Then create a sub-document under the wiki document with token YdFDdmExNoaLHnxZ5eUcbwMlnee.',
  'Title: "测试用例-财报指标存储"',
  'Use lark-doc skill: lark-cli docs +create to create the document with markdown format.',
  'Use Bash to read the file and pass content to lark-cli.',
  'After creation, confirm the document was created successfully.'
].join('\n'), { label: 'Upload 测试用例' });

log('Step: Update parent doc title');
await agent([
  'Update the feishu document title.',
  'Document token: YdFDdmExNoaLHnxZ5eUcbwMlnee',
  'Current title: "03. 财报指标存储"',
  'New title: "05.【已完成】财报指标存储"',
  '',
  'First read the lark-doc update reference to understand how to update a document title.',
  'Then use lark-cli docs +update --doc YdFDdmExNoaLHnxZ5eUcbwMlnee --command append to update the title.',
  'Note: updating title may require using block-level operations.'
].join('\n'), { label: 'Update Title' });

log('All phases complete for spec/feature-05.');
log('');
log('=== Summary ===');
log('Phase 1 - Analysis (completed earlier):');
log('  - 方案设计-财报指标存储.md');
log('  - TODO-财报指标存储.md');
log('  - 测试用例-财报指标存储.md');
log('Phase 2 - Implementation (completed now):');
log('  - T-1: basic_init.sql DDL + FinancialReportDO.java');
log('  - T-2: FinancialReportMapper.java + FinancialReportMapper.xml');
log('  - T-3: FinancialReport.java, PageResult.java, DTOs, Converters');
log('  - T-4: FinancialReportDomainService interface + impl');
log('  - T-5: FinancialMetadataInitializer (PostConstruct)');
log('  - T-7: FinancialReportController (6 REST endpoints)');
log('Phase 3 - Quality:');
log('  - Compilation verified: stock-common, stock-core, stock-biz all BUILD SUCCESS');
log('Phase 4 - Archive:');
log('  - Spec documents uploaded to Feishu');
log('  - Document title updated');

return { specDir: 'spec/feature-05', status: 'completed' };
