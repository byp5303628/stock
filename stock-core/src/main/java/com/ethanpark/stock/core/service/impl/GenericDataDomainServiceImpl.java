package com.ethanpark.stock.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ethanpark.stock.core.model.GenericDataRecord;
import com.ethanpark.stock.core.model.GenericDataQuery;
import com.ethanpark.stock.core.model.PageResult;
import com.ethanpark.stock.core.service.GenericDataDomainService;
import com.ethanpark.stock.core.route.RouteDispatcher;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenericDataDomainServiceImpl implements GenericDataDomainService {

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
