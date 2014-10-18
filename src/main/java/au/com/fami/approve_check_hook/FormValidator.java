/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Daniel Burr <dburr@fami.com.au>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package au.com.fami.approve_check_hook;

import com.atlassian.stash.user.UserService;
import com.atlassian.stash.setting.*;
import com.atlassian.stash.repository.*;


public class FormValidator implements RepositorySettingsValidator {

    private final UserService userService;
    private final RepositoryMetadataService metadataService;


    public FormValidator(UserService userService, RepositoryMetadataService metadataService) {
        this.userService = userService;
        this.metadataService = metadataService;
    }


    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
        for(int i = 1; i < 6; i++) checkRule(settings, errors, repository, i);
    }


    private void checkRule(Settings settings, SettingsValidationErrors errors, Repository repository, int num) {
        String field_enable = "enable" + num;
        String field_branch = "branch" + num;
        String field_approvers = "approvers" + num;

        Boolean enabled = settings.getBoolean(field_enable);
        if(enabled == null) return;

        String branch = settings.getString(field_branch);
        if(branch != null && !branch.isEmpty()) {
            Ref ref =  metadataService.resolveRef(repository, branch);
            if(ref == null) {
                errors.addFieldError(field_branch, "Error: Unknown reference '" + branch + "'");
            } else if(!(ref instanceof Branch)) {
                errors.addFieldError(field_branch, "Error: Reference '" + branch + "' is not a branch");
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
    }
}
