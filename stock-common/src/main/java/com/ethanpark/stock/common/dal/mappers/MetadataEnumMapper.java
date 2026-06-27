package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.MetadataEnumDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 元数据枚举 Mapper。
 *
 * @author baiyunpeng04
 */
@Mapper
public interface MetadataEnumMapper {
    List<MetadataEnumDO> selectAll();
    MetadataEnumDO selectById(Long id);
    MetadataEnumDO selectByCode(String code);
    int insert(MetadataEnumDO enumDO);
    int updateById(MetadataEnumDO enumDO);
    int deleteById(Long id);
}
