package com.sdm.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.core.util.Globalizer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedFilter implements Serializable {

    /**
     * Not Equal
     */
    Object isNot;
    /**
     * In
     */
    List<Object> include;
    /**
     * Not In
     */
    List<Object> exclude;
    /**
     * Greater Than Equal
     */
    Object from;
    /**
     * Less Than Equal
     */
    Object to;
    @NotBlank
    private String field;
    /**
     * Equal
     */
    private Object mustBe;
    /**
     * Nullable
     */
    private Boolean isNull;

    @JsonIgnore
    public String getParam() {
        return this.getParam("");
    }

    @JsonIgnore
    public String getParam(String prefix) {
        String cleanFieldName = this.field.replaceAll(".id", "Id");
        if (prefix.length() > 0) {
            return prefix + Globalizer.capitalize(cleanFieldName);
        }
        return cleanFieldName;
    }

    @JsonIgnore
    public String getQuery(String alias, Map<String, Object> params) {
        if (this.mustBe != null) {
            params.put(getParam(), this.mustBe);
            return alias + "." + this.field + " = :" + getParam();
        } else if (this.isNot != null) {
            params.put(getParam(), this.isNot);
            return alias + "." + this.field + " <> :" + getParam();
        } else if (this.isNull != null) {
            String check = this.isNull ? " NULL" : " NOT NULL";
            return alias + "." + this.field + " IS " + check;
        } else if (this.include != null) {
            params.put(getParam(), this.include);
            return alias + "." + this.field + " IN :" + getParam();
        } else if (this.exclude != null) {
            params.put(getParam(), this.exclude);
            return alias + "." + this.field + " NOT IN :" + getParam();
        } else if (from != null && to != null) {
            String fromParam = getParam("from");
            String toParam = getParam("to");
            params.put(fromParam, this.from);
            params.put(toParam, this.to);
            return alias + "." + this.field + " BETWEEN :" + fromParam + " AND :" + toParam;
        } else if (from != null) {
            params.put(getParam(), this.from);
            return alias + "." + this.field + " >= :" + getParam();
        } else if (to != null) {
            params.put(getParam(), this.to);
            return alias + "." + this.field + " <= :" + getParam();
        } else {
            return "";
        }
    }
}
