package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.MetadataEnumValueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 元数据枚举值 Mapper。
 *
 * @author baiyunpeng04
 */
@Mapper
public interface MetadataEnumValueMapper {
    List<MetadataEnumValueDO> selectByEnumId(Long enumId);

    /**
     * 批量查询多个枚举 ID 下的枚举值（C3: 消除 N+1 查询）。
     *
     * @param enumIds 枚举 ID 列表
     * @return 所有枚举值列表
     */
    List<MetadataEnumValueDO> selectByEnumIds(List<Long> enumIds);

    int insert(MetadataEnumValueDO valueDO);
    int updateById(MetadataEnumValueDO valueDO);
    int deleteById(Long id);
    int deleteByEnumId(Long enumId);
}
