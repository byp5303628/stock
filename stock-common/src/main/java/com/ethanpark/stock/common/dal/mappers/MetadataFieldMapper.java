package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.MetadataFieldDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 元数据字段 Mapper。
 *
 * @author baiyunpeng04
 */
@Mapper
public interface MetadataFieldMapper {
    List<MetadataFieldDO> selectByModelId(Long modelId);
    List<MetadataFieldDO> selectByEnumId(Long enumId);

    /**
     * 批量查询多个枚举 ID 下的引用字段（C3: 消除 N+1 查询）。
     *
     * @param enumIds 枚举 ID 列表
     * @return 所有引用字段列表
     */
    List<MetadataFieldDO> selectByEnumIds(List<Long> enumIds);

    MetadataFieldDO selectById(Long id);
    int insert(MetadataFieldDO fieldDO);
    int updateById(MetadataFieldDO fieldDO);
    int deleteById(Long id);
    int deleteByModelId(Long modelId);
}
