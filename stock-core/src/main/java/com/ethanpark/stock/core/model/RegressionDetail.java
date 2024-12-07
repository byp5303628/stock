package com.ethanpark.stock.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: baiyunpeng04
 * @since: 2024/12/7
 */
@Getter
@Setter
public class RegressionDetail {
    private Set<String> allCodes = new HashSet<>();

    private Set<String> successCodes = new HashSet<>();

    public List<String> getUnFinishCodes() {
        Set<String> codes = new HashSet<>(allCodes);

        codes.removeAll(successCodes);

        return new ArrayList<>(codes);
    }

    public boolean isFinished() {
        return allCodes.size() == successCodes.size();
    }
}
