package au.com.fami.approve_check_hook;

import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.scm.pull.MergeRequest;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.repository.*;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.user.StashUser;
import java.util.Collection;
import java.util.Vector;
import java.util.Iterator;


public class ApproveCheck implements PreReceiveRepositoryHook, RepositoryMergeRequestCheck {

    private final UserService userService;
    private final RepositoryMetadataService metadataService;


    public ApproveCheck(UserService userService, RepositoryMetadataService metadataService) {
        this.userService = userService;
        this.metadataService = metadataService;
    }


    /**
     * Vetos a pull-request if the necessary approvals have not been given
     */
    @Override
    public void check(RepositoryMergeRequestCheckContext context) {
        for(int i = 1; i < 6; i++) checkMergeRule(context, i);
    }


    /**
      * Prevent changes from being pushed to protected branches
      */
    @Override
    public boolean onReceive(RepositoryHookContext context, Collection<RefChange> refChanges, HookResponse hookResponse) {
        for(int i = 1; i < 6; i++) if(!checkPushRule(context, refChanges, i)) return false;
        return true;
    }


    private boolean isRuleEnabled(Settings settings, int num) {
        boolean enabled = settings.getBoolean("enable" + num, false);
        return enabled;
    }


    private Ref getBranch(Settings settings, Repository repository, int num) {
        String branch = settings.getString("branch" + num);
        return metadataService.resolveRef(repository, branch);
    }


    Vector<StashUser> getApprovers(Settings settings, int num) {
        String approvers = settings.getString("approvers" + num);
        Vector<StashUser> users = new Vector<StashUser>();
        for(String user : approvers.split(",")) {
            users.add(userService.getUserByName(user));
        }
        return users;
    }


    /**
     * Returns the user names in a comma separated list
     */
    String getUserNames(Vector<StashUser> users) {
        StringBuilder o = new StringBuilder();
        for(Iterator<StashUser> it = users.iterator(); it.hasNext();) {
            o.append(it.next().getDisplayName()).append(it.hasNext()? ", ": "");
        }
        return o.toString();
    }


    /**
     * Remove participant from list if they have given their approval
     */
    void checkApproval(PullRequestParticipant participant, Vector<StashUser> list) {
        if(!participant.isApproved()) return;

        StashUser user = participant.getUser();
        int pos = list.indexOf(user);
        if(pos >= 0) {
            System.out.println(user.getDisplayName() + " has approved, removing from list");
            list.remove(pos);
        }
    }
 

    private void checkMergeRule(RepositoryMergeRequestCheckContext context, int num) {
        Settings settings = context.getSettings();
        MergeRequest merge_request = context.getMergeRequest();
        PullRequest pull_request = merge_request.getPullRequest();

        // skip rule if not enabled
        boolean enabled = isRuleEnabled(settings, num);
        //System.out.println("Check merge rule " + num + ", enabled? " + enabled);
        if(!enabled) return;

        // skip rule if target branch does not match the branch from the rule
        Ref ref = getBranch(settings, pull_request.getToRef().getRepository(), num);
        //System.out.println("Compare push branch '" + pull_request.getToRef().getId() + "' vs '" + ref.getId() + "'");
        if(!pull_request.getToRef().getId().equals(ref.getId())) return;

        if(pull_request.isClosed()) {
            merge_request.veto("Request closed", "This pull request is already closed");
            return;
        }

        Vector<StashUser> approvers = getApprovers(settings, num);
        System.out.println("Requires approval from the following users: " + getUserNames(approvers));

        // the author of pull request has approved it implicitly
        approvers.remove(pull_request.getAuthor().getUser());

        // remove any reviewers or participants who have given their approval
        for(PullRequestParticipant reviewer : pull_request.getReviewers()) checkApproval(reviewer, approvers);
        for(PullRequestParticipant participant : pull_request.getParticipants()) checkApproval(participant, approvers);

        if(approvers.size() > 0) {
            merge_request.veto("Merge denied", "Still require approvals from the following users: " + getUserNames(approvers));
        }
    }


    private boolean checkPushRule(RepositoryHookContext context, Collection<RefChange> refChanges, int num) {
        Settings settings = context.getSettings();

        // skip rule if not enabled
        boolean enabled = isRuleEnabled(settings, num);
        //System.out.println("Check push rule " + num + ", enabled? " + enabled);
        if(!enabled) return true;

        // reject if any incoming references match the branch from the rule
        Ref ref = getBranch(settings, context.getRepository(), num);
        for(RefChange refChange : refChanges) {
            //System.out.println("Compare push branch '" + refChange.getRefId() + "' vs '" + ref.getId() + "'");
            if(refChange.getRefId().equals(ref.getId())) return false;
        }

        return true;
    }
}
