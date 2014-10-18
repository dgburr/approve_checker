Approve Checker Add-on for Stash
================================

Approve Checker is an add-on module for Atlassian Stash which can be used
to prevent pull requests from being merged to certain branches until they are
approved by a specific list of users.  It supports up to 5 separate "rules"
which specify a branch name and a list of approvers.  The following actions 
are performed for each active rule:

1. A pre-receive hook prevents any changes from being pushed directly to
   the specified branch.
2. A merge check prevents pull requests from being merged to the target 
   branch until all of the specified users have given their approval.
   Note that the author of the pull request is considered to have implicitly
   approved it.

Approve Checker has been tested with Stash versions 3.3.0 and 3.3.2.
