/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.fee.discount.impl;

import com.java110.dto.feeDiscount.ComputeDiscountDto;
import com.java110.dto.feeDiscount.FeeDiscountDto;
import com.java110.dto.feeDiscount.FeeDiscountSpecDto;
import com.java110.fee.discount.IComputeDiscount;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 缴几个月赠送几个月 规则
 *
 * @desc add by 吴学文 10:27
 */

@Component(value = "reductionMonthFeeRule")
public class ReductionMonthFeeRule implements IComputeDiscount {

    /**
     * 89002020980001	102020001	月份
     * 89002020980002	102020001	打折率
     */
    private static final String SPEC_MONTH = "89002020980014"; //月份
    private static final String SPEC_RATE = "89002020980015"; // 赠送月份

    @Override
    public ComputeDiscountDto compute(FeeDiscountDto feeDiscountDto) {

        List<FeeDiscountSpecDto> feeDiscountSpecDtos = feeDiscountDto.getFeeDiscountSpecs();

        if (feeDiscountSpecDtos.size() < 1) {
            return null;
        }
        double month = 0.0;
        double reductionMonth = 0.0;
        for (FeeDiscountSpecDto feeDiscountSpecDto : feeDiscountSpecDtos) {
            if (SPEC_MONTH.equals(feeDiscountSpecDto.getSpecId())) {
                month = Double.parseDouble(feeDiscountSpecDto.getSpecValue());
            }
            if (SPEC_RATE.equals(feeDiscountSpecDto.getSpecId())) {
                reductionMonth = Double.parseDouble(feeDiscountSpecDto.getSpecValue());
            }
        }
        if (feeDiscountDto.getCycles() < month) {
            return null;
        } else {
            ComputeDiscountDto computeDiscountDto = new ComputeDiscountDto();
            computeDiscountDto.setDiscountId(feeDiscountDto.getDiscountId());
            computeDiscountDto.setDiscountType(FeeDiscountDto.DISCOUNT_TYPE_D);
            computeDiscountDto.setRuleId(feeDiscountDto.getRuleId());
            computeDiscountDto.setRuleName(feeDiscountDto.getRuleName());
            computeDiscountDto.setDiscountName(feeDiscountDto.getDiscountName());
            computeDiscountDto.setDiscountPrice(0.0);
            computeDiscountDto.setFeeDiscountSpecs(feeDiscountSpecDtos);
            return computeDiscountDto;
        }
    }
}
