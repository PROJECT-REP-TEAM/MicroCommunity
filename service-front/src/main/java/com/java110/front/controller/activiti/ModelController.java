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
package com.java110.front.controller.activiti;

import com.java110.front.smo.activiti.IModelSMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc add by 吴学文 8:38
 */
@RestController
public class ModelController {

    @Autowired
    private IModelSMO modelSMOImpl;

    /**
     * 查询工作流json
     *
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/model/${modelId}/json", method = RequestMethod.GET)
    public ResponseEntity<String> queryJson(@PathVariable String modelId) {

        return modelSMOImpl.getJson(modelId);
    }
}
