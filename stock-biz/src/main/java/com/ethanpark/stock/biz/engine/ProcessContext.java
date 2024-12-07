package com.ethanpark.stock.biz.engine;

import com.ethanpark.stock.biz.ErrorCode;
import com.ethanpark.stock.biz.engine.exception.ProcessException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class ProcessContext {
    /**
     * 产品吗, 用于区分场景使用
     */
    private String productCode;

    /**
     * 业务吗, 用于区分产品下子业务使用
     */
    private String businessCode;

    /**
     * Current Stage;
     */
    private String stage = Router.PRE_PROCESS;

    /**
     * 操作实体, 可以包含实际业务所需的内容
     */
    private Object entity;

    /**
     * 入参实体, 用于通用校验
     */
    private Object reqDTO;

    /**
     * 错误码, 用于构建返回结果
     */
    private Integer resultCode = 200;

    /**
     * 错误文案, 用于构建返回结果
     */
    private String resultMsg = "执行成功";

    /**
     * 是否直接跳到FINISH阶段
     */
    private boolean breakToFinish = false;

    /**
     * 执行流程, 在执行开始进行绑定
     */
    private ProcessConfig processConfig;

    @SuppressWarnings("unchecked")
    public <T> T getEntity() {
        return (T) this.entity;
    }

    public <T> T getEntity(Class<T> tClass) {
        try {
            return tClass.cast(this.entity);
        } catch (ClassCastException e) {
            throw new ProcessException(ErrorCode.ILLEGAL_PARAM.getCode(), "参数异常! action不支持该类型实体," +
                    " entity=" + this.entity.getClass());
        }
    }
}
