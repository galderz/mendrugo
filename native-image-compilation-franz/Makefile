ID ?= native-image-compilation-franz
WORKTREE ?= topic

TOPDIR = ..
include $(TOPDIR)/make/common/MakeBase.gmk

base_branch = master
base_remote = oracle
tree_name = mandrel-$(ID)
worktree_id = $(WORKTREE).$(ID)

idea:
> cd mandrel/substratevm
> mx intellijinit
.PHONY: idea

new-worktree:
> pushd $(HOME)/1/mandrel
> git fetch $(base_remote)
> git pull $(base_remote) $(base_branch)
> git worktree add -b $(worktree_id) ../$(tree_name) $(base_branch)
> popd
> ln -s $(HOME)/1/$(tree_name) mandrel
.PHONY: new-worktree
