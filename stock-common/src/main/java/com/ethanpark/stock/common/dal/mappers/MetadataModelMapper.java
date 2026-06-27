package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.MetadataModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 元数据模型 Mapper。
 *
 * @author baiyunpeng04
 */
@Mapper
public interface MetadataModelMapper {
    List<MetadataModelDO> selectAll();
    MetadataModelDO selectById(Long id);
    MetadataModelDO selectByCode(String code);
    int insert(MetadataModelDO modelDO);
    int updateById(MetadataModelDO modelDO);
    int deleteById(Long id);
}
