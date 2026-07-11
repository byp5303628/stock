package com.ethanpark.stock.biz.init;

import com.ethanpark.stock.biz.init.metadata.BalanceSheetInitializer;
import com.ethanpark.stock.biz.init.metadata.CashFlowStatementInitializer;
import com.ethanpark.stock.biz.init.metadata.IncomeStatementInitializer;
import com.ethanpark.stock.core.model.Result;
import com.ethanpark.stock.core.model.metadata.MetadataField;
import com.ethanpark.stock.core.model.metadata.MetadataModel;
import com.ethanpark.stock.core.service.MetadataDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitializerRunnerTest {

    @Mock
    private MetadataDomainService metadataDomainService;

    private InitializerRunner runner;

    @BeforeEach
    void setUp() {
        runner = new InitializerRunner();
        runner.setMetadataDomainService(metadataDomainService);
    }

    @Nested
    @DisplayName("场景：METADATA 类型 — 模型不存在时创建模型和字段")
    class MetadataInit {

        @BeforeEach
        void mockModelNotExists() {
            when(metadataDomainService.getModelByCode(anyString())).thenReturn(null);
        }

        @Test
        @DisplayName("为每个枚举项调用 saveModel")
        void run_createsModels() {
            MetadataModel savedModel = new MetadataModel();
            savedModel.setId(1L);
            when(metadataDomainService.saveModel(any()))
                    .thenReturn(Result.ok(savedModel));

            runner.run();

            // 每个 METADATA 枚举项调用一次 saveModel
            long metadataCount = countMetadataEntries();
            verify(metadataDomainService, times((int) metadataCount)).saveModel(any());
        }

        @Test
        @DisplayName("为每个枚举项调用 saveField")
        void run_createsFields() {
            when(metadataDomainService.saveModel(any()))
                    .thenAnswer(invocation -> {
                        MetadataModel model = new MetadataModel();
                        model.setId(invocation.getArgument(0, MetadataModel.class).hashCode() % 1000L + 1);
                        return Result.ok(model);
                    });
            when(metadataDomainService.saveField(any()))
                    .thenReturn(Result.ok(new MetadataField()));

            runner.run();

            // 验证 saveField 被调用的次数为所有 METADATA 枚举项的字段总数
            int totalFields = countTotalFields();
            verify(metadataDomainService, times(totalFields)).saveField(any());
        }

        @Test
        @DisplayName("saveModel 传入的模型 code 正确")
        void run_correctModelCode() {
            MetadataModel savedModel = new MetadataModel();
            savedModel.setId(1L);
            when(metadataDomainService.saveModel(any()))
                    .thenReturn(Result.ok(savedModel));

            runner.run();

            ArgumentCaptor<MetadataModel> captor = ArgumentCaptor.forClass(MetadataModel.class);
            verify(metadataDomainService, atLeastOnce()).saveModel(captor.capture());

            assertThat(captor.getAllValues())
                    .extracting(MetadataModel::getCode)
                    .contains("cash_flow_statement", "balance_sheet", "income_statement");
        }
    }

    @Nested
    @DisplayName("场景：METADATA 类型 — 模型已存在时跳过")
    class MetadataSkip {

        @Test
        @DisplayName("模型已存在时不调用 saveModel")
        void run_skipsExistingModels() {
            when(metadataDomainService.getModelByCode(anyString()))
                    .thenReturn(new MetadataModel());

            runner.run();

            verify(metadataDomainService, never()).saveModel(any());
            verify(metadataDomainService, never()).saveField(any());
        }
    }

    // ===== 辅助方法 =====

    private long countMetadataEntries() {
        return java.util.Arrays.stream(InitializerDefinition.values())
                .filter(def -> def.getCategory() == InitializerDefinition.Category.METADATA)
                .count();
    }

    private int countTotalFields() {
        int total = 0;
        for (InitializerDefinition def : InitializerDefinition.values()) {
            if (def.getCategory() == InitializerDefinition.Category.METADATA) {
                total += countFieldsOf(def);
            }
        }
        return total;
    }

    private int countFieldsOf(InitializerDefinition def) {
        try {
            Class<? extends MetadataInitializer> clazz =
                    (Class<? extends MetadataInitializer>) def.getHandlerClass();
            MetadataInitializer init = clazz.getDeclaredConstructor().newInstance();
            return init.getFields().size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
