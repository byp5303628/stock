package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.ScheduleConfig;
import com.ethanpark.stock.core.service.ScheduleConfigDomainService;
import com.ethanpark.stock.biz.converter.DtoConverter;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.dto.ScheduleConfigDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@RestController
@RequestMapping("/api/schedule-config")
public class ScheduleConfigController {

    @Resource
    private ScheduleConfigDomainService scheduleConfigDomainService;

    @GetMapping("/list.json")
    public ResponseDTO<List<ScheduleConfigDTO>> getAllScheduleConfigs() {
        List<ScheduleConfig> scheduleConfigs = scheduleConfigDomainService.getScheduleConfigs();

        List<ScheduleConfigDTO> dtos =
                scheduleConfigs.stream().map(DtoConverter::toDto).collect(Collectors.toList());

        return ResponseDTO.success(dtos);
    }

    @PostMapping("/save.json")
    public ResponseDTO<Void> save(@RequestBody ScheduleConfigDTO scheduleConfigDTO) {
        ScheduleConfig domain = DtoConverter.toDomain(scheduleConfigDTO);

        Result<Void> result = scheduleConfigDomainService.save(domain);

        if (result.isSuccess()) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.error(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }
    }
}
