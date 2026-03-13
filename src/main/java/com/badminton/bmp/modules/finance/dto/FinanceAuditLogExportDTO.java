package com.badminton.bmp.modules.finance.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Getter;
import lombok.Setter;

/**
 * 财务审计日志导出DTO
 * 用于Excel导出
 */
@Getter
@Setter
@HeadRowHeight(20)
@ContentRowHeight(18)
public class FinanceAuditLogExportDTO {

    @ExcelProperty(value = "序号", index = 0)
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty(value = "财务单号", index = 1)
    @ColumnWidth(20)
    private String financeNo;

    @ExcelProperty(value = "操作类型", index = 2)
    @ColumnWidth(15)
    private String operationTypeDisplay;

    @ExcelProperty(value = "操作人", index = 3)
    @ColumnWidth(15)
    private String operator;

    @ExcelProperty(value = "操作时间", index = 4)
    @ColumnWidth(20)
    private String operationTime;

    @ExcelProperty(value = "变更摘要", index = 5)
    @ColumnWidth(30)
    private String changeSummary;

    @ExcelProperty(value = "IP地址", index = 6)
    @ColumnWidth(18)
    private String ipAddress;

    @ExcelProperty(value = "备注", index = 7)
    @ColumnWidth(30)
    private String remark;

    /**
     * 设置操作类型（自动转换为中文显示）
     */
    public void setOperationType(String operationType) {
        if (operationType == null) {
            this.operationTypeDisplay = "";
            return;
        }
        switch (operationType) {
            case "CREATE":
                this.operationTypeDisplay = "创建";
                break;
            case "UPDATE":
                this.operationTypeDisplay = "修改";
                break;
            case "DELETE":
                this.operationTypeDisplay = "删除";
                break;
            case "RECONCILE":
                this.operationTypeDisplay = "对账";
                break;
            default:
                this.operationTypeDisplay = operationType;
        }
    }
}
