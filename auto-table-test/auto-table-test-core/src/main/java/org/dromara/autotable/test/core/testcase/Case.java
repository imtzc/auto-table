package org.dromara.autotable.test.core.testcase;


import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnDefault;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 案件对象 zf_case
 *
 * @author chengliang4810
 * @date 2024-08-22
 */
@Data
@Accessors(chain = true)
@AutoTable(comment = "案件")
@EqualsAndHashCode(callSuper = true)
public class Case extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 案件ID
     */
    private Long caseId;

    /**
     * 案号
     */
    private Integer caseNumber;

    /**
     * 案件登记号
     */
    private String registerNumber;

    /**
     * 当事人名称
     */
    private String partyName;

    /**
     *  当事人主体资格证照名称
     */
    private String partyLegalPersonName;

    /**
     * 当事人统一社会信用代码
     */
    private String partySocialCreditCode;

    /**
     * 当事人住所
     */
    private String partyAddress;

    /**
     * 当事人负责人
     */
    private String partyLegalPerson;

    /**
     * 当事人身份证件号码
     */
    private String partyIdCardNumber;

    /**
     * 当事人联系电话
     */
    private String partyPhoneNumber;

    /**
     * 当事人其他联系方式
     */
    private String partyOtherContact;

    /**
     * 处罚方式
     */
    private String punishmentMethod;

    /**
     * 处罚金额
     */
    private Integer punishmentAmount;

    /**
     * 处罚决定时间
     */
    private Date punishmentDecisionTime;

    /**
     * 处罚决定内容
     */
    private String punishmentDecisionContent;

    /**
     * 备注
     */
    private String remark;
    /**
     * 是否办理结束
     */
    @ColumnDefault("false")
    private Boolean isEnd;
    /**
     * 流程实例Id
     */
    private Long flowInstanceId;
    /**
     * 案件当前阶段ID
     */
    private Long currentPhaseId;
    /**
     * 当前流程名称
     */
    private String currentFlowName;

    /**
     * 当前流程key
     */
    private String currentFlowKey;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 总处理时长: 单位秒
     */
    private Long totalHandlingTime;

    /**
     * 执行程序
     */
    private String executionProcedure;

    /**
     * 执法人员
     */
    private String officer;

    /**
     * 案卷编号
     */
    private String caseFileNumber;

    /**
     * 案件逻辑删除
     */
    @ColumnDefault("0")
    private String deleteFlag;

    /**
     * 是否是案子
     */
    @ColumnDefault("false")
    private Boolean isCases;
}
