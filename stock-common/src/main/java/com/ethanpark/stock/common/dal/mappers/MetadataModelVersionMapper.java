package com.ethanpark.stock.common.dal.mappers;

import com.ethanpark.stock.common.dal.mappers.entity.MetadataModelVersionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 元数据模型版本 Mapper。
 *
 * @author baiyunpeng04
 */
@Mapper
public interface MetadataModelVersionMapper {
    int insert(MetadataModelVersionDO versionDO);

    List<MetadataModelVersionDO> selectByModelId(Long modelId);

    MetadataModelVersionDO selectByModelIdAndVersion(@Param("modelId") Long modelId, @Param("version") Integer version);

    Integer selectMaxVersionByModelId(@Param("modelId") Long modelId);
}
