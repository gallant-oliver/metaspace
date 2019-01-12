package io.zeta.metaspace.model.result;


import java.util.List;

public class TableColumnRules {
    private List<SystemRule> tableRules;
    private List<ColumnsRule> columnsRules;

    public List<SystemRule> getTableRules() {
        return tableRules;
    }

    public void setTableRules(List<SystemRule> tableRules) {
        this.tableRules = tableRules;
    }

    public List<ColumnsRule> getColumnsRules() {
        return columnsRules;
    }

    public void setColumnsRules(List<ColumnsRule> columnsRules) {
        this.columnsRules = columnsRules;
    }
    public static class SystemRule {
        private String systemRuleId;
        private String ruleName;
        private String ruleInfo;
        private List<Integer> ruleAllowCheckType;
        private String ruleCheckThresholdUnit;

        public String getSystemRuleId() {
            return systemRuleId;
        }

        public void setSystemRuleId(String systemRuleId) {
            this.systemRuleId = systemRuleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public String getRuleInfo() {
            return ruleInfo;
        }

        public void setRuleInfo(String ruleInfo) {
            this.ruleInfo = ruleInfo;
        }

        public List<Integer> getRuleAllowCheckType() {
            return ruleAllowCheckType;
        }

        public void setRuleAllowCheckType(List<Integer> ruleAllowCheckType) {
            this.ruleAllowCheckType = ruleAllowCheckType;
        }

        public String getRuleCheckThresholdUnit() {
            return ruleCheckThresholdUnit;
        }

        public void setRuleCheckThresholdUnit(String ruleCheckThresholdUnit) {
            this.ruleCheckThresholdUnit = ruleCheckThresholdUnit;
        }
    }
    public static class ColumnsRule{
        private String columnName;
        private String columnType;
        private List<SystemRule> columnRules;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public List<SystemRule> getColumnRules() {
            return columnRules;
        }

        public void setColumnRules(List<SystemRule> columnRules) {
            this.columnRules = columnRules;
        }
    }
}
