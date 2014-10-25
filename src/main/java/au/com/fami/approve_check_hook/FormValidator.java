/*
 *  Copyright 2014 Daniel Burr <dburr@fami.com.au>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package au.com.fami.approve_check_hook;

import com.atlassian.stash.user.UserService;
import com.atlassian.stash.setting.*;
import com.atlassian.stash.repository.*;
import java.util.Vector;


public class FormValidator implements RepositorySettingsValidator {

    private final UserService userService;
    private final RepositoryMetadataService metadataService;


    public FormValidator(UserService userService, RepositoryMetadataService metadataService) {
        this.userService = userService;
        this.metadataService = metadataService;
    }


    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
        Vector<String> branch_list = new Vector<String>();
        for(int i = 1; i < 6; i++) checkRule(settings, errors, repository, branch_list, i);
    }


    private void checkRule(Settings settings, SettingsValidationErrors errors, Repository repository, Vector<String> branch_list, int num) {
        String field_enable = "enable" + num;
        String field_branch = "branch" + num;
        String field_approvers = "approvers" + num;
        String field_minCount = "min" + num;

        Boolean enabled = settings.getBoolean(field_enable);
        if(enabled == null) return;

        String branch = settings.getString(field_branch);
        if(branch != null && !branch.isEmpty()) {
            if(branch_list.indexOf(branch) >= 0) {
	        errors.addFieldError(field_branch, "Error: Multiple rules referring to branch");
            } else {
                branch_list.add(branch);

                Ref ref =  metadataService.resolveRef(repository, branch);
                if(ref == null) {
                    errors.addFieldError(field_branch, "Error: Unknown reference");
                } else if(!(ref instanceof Branch)) {
                    errors.addFieldError(field_branch, "Error: Reference is not a branch");
                }
            }
        } else {
            errors.addFieldError(field_branch, "Error: No branch selected");
        }

        String approvers = settings.getString(field_approvers);
        if(approvers != null && !approvers.isEmpty()) {
            for(String user : approvers.split(",")) {
                if(userService.getUserByName(user) == null) {
                    errors.addFieldError(field_approvers, "Error: User '" + user + "' unknown");
                }
            }
        } else {
            errors.addFieldError(field_approvers, "Error: No approvers specified");
        }

        int approverCount = settings.getInt(field_minCount, 0);
        if (approverCount < 0) {
            errors.addFieldError(field_minCount,
                    "Error: At least one approver should be required (or 0 for all)");
        } else if (approvers != null
                && approverCount > approvers.split(",").length) {
            errors.addFieldError(field_minCount,
                    "Error: Cannot have a count higher than the number of approvers specified");
        }
    }
}
