package com.ethanpark.stock.biz.controller;

import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.ScheduleConfig;
import com.ethanpark.stock.core.service.ScheduleConfigDomainService;
import com.ethanpark.stock.biz.converter.DtoConverter;
import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.dto.ScheduleConfigDTO;
import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.exception.BusinessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/8
 */
@RestController
@RequestMapping("/api/schedule-config")
public class ScheduleConfigController {

    private final ScheduleConfigDomainService scheduleConfigDomainService;

    public ScheduleConfigController(ScheduleConfigDomainService scheduleConfigDomainService) {
        this.scheduleConfigDomainService = scheduleConfigDomainService;
    }

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

        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), result.getMsg());
        }
        return ResponseDTO.success();
    }
}
