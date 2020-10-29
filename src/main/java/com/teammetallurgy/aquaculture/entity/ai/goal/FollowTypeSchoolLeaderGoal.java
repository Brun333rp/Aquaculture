package com.teammetallurgy.aquaculture.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.fish.AbstractGroupFishEntity;

import java.util.List;
import java.util.function.Predicate;

public class FollowTypeSchoolLeaderGoal extends Goal {
    private final AbstractGroupFishEntity taskOwner;
    private int navigateTimer;
    private int cooldown;

    public FollowTypeSchoolLeaderGoal(AbstractGroupFishEntity fishEntity) {
        this.taskOwner = fishEntity;
        this.cooldown = this.getNewCooldown(fishEntity);
    }

    private int getNewCooldown(AbstractGroupFishEntity fishEntity) {
        return 200 + fishEntity.getRNG().nextInt(200) % 20;
    }

    @Override
    public boolean shouldExecute() {
        if (this.taskOwner.isGroupLeader()) {
            return false;
        } else if (this.taskOwner.hasGroupLeader()) {
            return true;
        } else if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        } else {
            this.cooldown = this.getNewCooldown(this.taskOwner);
            Predicate<AbstractGroupFishEntity> predicate = (fishEntity) -> fishEntity.getType() == this.taskOwner.getType() && (fishEntity.canGroupGrow() || !fishEntity.hasGroupLeader());
            List<AbstractGroupFishEntity> schoolList = this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), this.taskOwner.getBoundingBox().grow(8.0D, 8.0D, 8.0D), predicate);
            AbstractGroupFishEntity fishEntity = schoolList.stream().filter(AbstractGroupFishEntity::canGroupGrow).findAny().orElse(this.taskOwner);
            fishEntity.func_212810_a(schoolList.stream().filter((schoolFish) -> !schoolFish.hasGroupLeader()));
            return this.taskOwner.hasGroupLeader();
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.taskOwner.hasGroupLeader() && this.taskOwner.inRangeOfGroupLeader();
    }

    @Override
    public void startExecuting() {
        this.navigateTimer = 0;
    }

    @Override
    public void resetTask() {
        if (this.taskOwner != null) {
            this.taskOwner.leaveGroup();
        }
    }

    @Override
    public void tick() {
        if (--this.navigateTimer <= 0) {
            this.navigateTimer = 10;
            this.taskOwner.moveToGroupLeader();
        }
    }
}