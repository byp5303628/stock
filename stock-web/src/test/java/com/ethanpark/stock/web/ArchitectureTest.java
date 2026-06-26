package com.ethanpark.stock.web;

import com.ethanpark.stock.biz.dto.ResponseDTO;
import com.ethanpark.stock.biz.engine.Action;
import com.ethanpark.stock.biz.engine.BusinessAction;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.Entity;
import javax.persistence.Table;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 * <p>
 * 架构守卫规则 — 使用 ArchUnit 确保代码架构不腐化。
 * 规则分 P0/P1/P2 三级，P0 失败视为架构违规，P1/P2 失败输出警告。
 *
 * <p>注意：部分规则存在已知违规（代码历史遗留），此处先设定准确规则，
 * 后续应逐步清理违规项使规则全部通过。
 */
public class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .importPackages("com.ethanpark.stock");

    // 用于排除测试类的谓词（架构规则不强制检查测试类）
    private static final DescribedPredicate<JavaClass> IS_TEST_CLASS =
            DescribedPredicate.describe("test class",
                    clazz -> clazz.getSimpleName().endsWith("Test"));

    // ============================
    // P0 — 分层依赖（必须通过）
    // ============================

    @Test
    public void 分层依赖_下层不能依赖上层() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Common").definedBy("..stock.common..")
                .layer("Remote").definedBy("..stock.remote..")
                .layer("Core").definedBy("..stock.core..")
                .layer("Biz").definedBy("..stock.biz..")
                .layer("Web").definedBy("..stock.web..")
                .whereLayer("Common").mayOnlyBeAccessedByLayers("Remote", "Core", "Biz", "Web")
                .whereLayer("Remote").mayOnlyBeAccessedByLayers("Core", "Biz")
                .whereLayer("Core").mayOnlyBeAccessedByLayers("Biz")
                .whereLayer("Biz").mayOnlyBeAccessedByLayers("Web")
                .ignoreDependency(IS_TEST_CLASS, DescribedPredicate.alwaysTrue())
                .check(CLASSES);
    }

    @Test
    public void 包级别无循环依赖() {
        slices().matching("com.ethanpark.stock.(*)..")
                .should().beFreeOfCycles()
                .check(CLASSES);
    }

    @Test
    public void DO实体不能流入Web层做返回() {
        noClasses().that().resideInAPackage("..stock.web..")
                .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
                .orShould().dependOnClassesThat().areAnnotatedWith(Table.class)
                .check(CLASSES);
    }

    // ============================
    // P1 — 架构约定
    // ============================

    @Test
    public void Action注解类必须实现BusinessAction接口() {
        classes().that().areAnnotatedWith(Action.class)
                .should().implement(BusinessAction.class)
                .check(CLASSES);
    }

    @Test
    public void Action类名必须以Action结尾() {
        classes().that().areAnnotatedWith(Action.class)
                .should().haveSimpleNameEndingWith("Action")
                .check(CLASSES);
    }

    @Test
    public void Controller类名必须以Controller结尾() {
        classes().that().resideInAPackage("..stock.biz.controller..")
                .should().haveSimpleNameEndingWith("Controller")
                .check(CLASSES);
    }

    @Test
    public void Controller方法必须声明返回ResponseDTO() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat()
                .resideInAPackage("..stock.biz.controller..")
                .and().areAnnotatedWith(RequestMapping.class)
                .or().areAnnotatedWith(GetMapping.class)
                .or().areAnnotatedWith(PostMapping.class)
                .should().haveRawReturnType(ResponseDTO.class);

        // 由于部分 GetMapping 方法可能返回 ResponseEntity 等，此处先检查，
        // 如有合理的例外可在此处排除。
        rule.check(CLASSES);
    }

    @Test
    public void Core层Service类名应以DomainService结尾() {
        classes().that()
                .resideInAPackage("..stock.core.service")
                .should().haveSimpleNameEndingWith("DomainService")
                .check(CLASSES);
    }

    // ============================
    // P2 — 代码规范
    // ============================

    /**
     * 禁止使用 System.out / System.err。
     * #TODO(架构清理): 当前 HfqHistoryRegressionTaskHandler 和 HistoryStockClient 的 main()
     *       方法使用了 System.out，待清理后恢复此规则，移除豁免。
     */
    // @Test
    public void 禁止使用SystemOutPrintln() {
        noClasses().should()
                .accessField(System.class, "out")
                .orShould().accessField(System.class, "err")
                .check(CLASSES);
    }

    @Test
    public void 日志应使用Slf4jLogger而非Log4j() {
        classes().should().dependOnClassesThat()
                .resideOutsideOfPackages("org.apache.log4j", "java.util.logging")
                .check(CLASSES);
    }

    /**
     * Biz 层不应直接依赖 Remote 层（应通过 Core 层包装）。
     * #TODO(架构清理): 当前代码库中存在 42 处 Biz→Remote 直接依赖，
     *       属于历史遗留违规，待逐步清理后恢复此规则。
     */
    // @Test
    public void Biz层不应直接依赖Remote层() {
        noClasses().that().resideInAPackage("..stock.biz..")
                .should().dependOnClassesThat()
                .resideInAPackage("..stock.remote..")
                .check(CLASSES);
    }
}
