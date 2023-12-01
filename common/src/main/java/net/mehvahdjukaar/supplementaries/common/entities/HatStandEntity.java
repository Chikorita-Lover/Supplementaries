package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.client.anim.PendulumAnimation;
import net.mehvahdjukaar.moonlight.api.client.anim.SwingAnimation;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class HatStandEntity extends LivingEntity {
    public static final int WOBBLE_TIME = 5;
    private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(HatStandEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(HatStandEntity.class, EntityDataSerializers.ROTATIONS);

    private final NonNullList<ItemStack> helmet = NonNullList.withSize(1, ItemStack.EMPTY);
    private boolean invisible;
    /**
     * After punching the stand, the cooldown before you can punch it again without breaking it.
     */
    public long lastHit;
    private Rotations headPose;
    public final SwingAnimation animation;

    public HatStandEntity(EntityType<? extends HatStandEntity> entityType, Level level) {
        super(entityType, level);
        this.headPose = DEFAULT_HEAD_POSE;
        this.setMaxUpStep(0.0F);
        if (PlatHelper.getPhysicalSide().isClient()) {
            animation = new PendulumAnimation(
                   ClientConfigs.Blocks.HAT_STAND_CONFIG, this::getRotationAxis);
        } else {
            animation = null;
        }
    }

    private Vector3f getRotationAxis() {
        return new Vector3f(0,1,0);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return PlatHelper.getEntitySpawnPacket(this);
    }

    @Override
    public void aiStep() {
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.refreshDimensions();
        this.setPos(d, e, f);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CLIENT_FLAGS, (byte) 0);
        this.entityData.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.helmet;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        if (Objects.requireNonNull(slot) == EquipmentSlot.HEAD) {
            return this.helmet.get(0);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        if (slot == EquipmentSlot.HEAD) {
            this.onEquipItem(slot, this.helmet.set(0, stack), stack);
        }
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(stack);
        return this.getItemBySlot(equipmentSlot).isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ItemStack stack = this.helmet.get(0);
        if (!stack.isEmpty())
            compound.put("Helmet", stack.save(new CompoundTag()));
        compound.putBoolean("Invisible", this.isInvisible());

        ListTag compoundTag = new ListTag();
        if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
            compoundTag = this.headPose.save();
        }
        compound.put("HeadPose", compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Helmet")) {
            this.helmet.set(0, ItemStack.of(compound.getCompound("Helmet")));
        }
        this.setInvisible(compound.getBoolean("Invisible"));
        ListTag listTag = compound.getList("HeadPose", 5);
        this.setHeadPose(listTag.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(listTag));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(Items.NAME_TAG)) {
            boolean isClientSide = player.level().isClientSide;
            if(itemStack.isEmpty() && player.isSecondaryUseActive()){
                if(isClientSide){
                    //TODO: cap impulse
                    //animation.hit(vec, 1);
                        animation.addImpulse(1);
                }
                return InteractionResult.sidedSuccess(isClientSide);
            }
            if (player.isSpectator()) {
                return InteractionResult.SUCCESS;
            } else if (isClientSide) {
                return InteractionResult.CONSUME;
            } else {
                if (itemStack.isEmpty()) {
                    EquipmentSlot targetSlot = EquipmentSlot.HEAD;
                    if (this.hasItemInSlot(targetSlot) && this.swapItem(player, targetSlot, itemStack, hand)) {
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
                    if (equipmentSlot != EquipmentSlot.HEAD) {
                        return InteractionResult.FAIL;
                    }
                    if (this.swapItem(player, equipmentSlot, itemStack, hand)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    private boolean swapItem(Player player, EquipmentSlot slot, ItemStack stack, InteractionHand hand) {
        ItemStack itemStack = this.getItemBySlot(slot);
        if (player.getAbilities().instabuild && itemStack.isEmpty() && !stack.isEmpty()) {
            this.setItemSlot(slot, stack.copyWithCount(1));
            return true;
        } else if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!itemStack.isEmpty()) {
                return false;
            } else {
                this.setItemSlot(slot, stack.split(1));
                return true;
            }
        } else {
            this.setItemSlot(slot, stack);
            player.setItemInHand(hand, itemStack);
            return true;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                this.kill();
                return false;
            } else if (!this.isInvulnerableTo(source) && !this.invisible) {
                if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                    this.brokenByAnything(source);
                    this.kill();
                    return false;
                } else if (source.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
                    if (this.isOnFire()) {
                        this.causeDamage(source, 0.15F);
                    } else {
                        this.setSecondsOnFire(5);
                    }

                    return false;
                } else if (source.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
                    this.causeDamage(source, 4.0F);
                    return false;
                } else {
                    boolean isDirectArrow = source.getDirectEntity() instanceof AbstractArrow;
                    boolean isPierceArrow = isDirectArrow && ((AbstractArrow) source.getDirectEntity()).getPierceLevel() > 0;
                    boolean bl3 = "player".equals(source.getMsgId());
                    if (!bl3 && !isDirectArrow) {
                        return false;
                    } else {
                        Entity sourceEntity = source.getEntity();
                        if (sourceEntity instanceof Player player) {
                            if (!player.getAbilities().mayBuild) {
                                return false;
                            }
                        }
                        if (source.isCreativePlayer()) {
                            this.playBrokenSound();
                            this.showBreakingParticles();
                            this.kill();
                            return isPierceArrow;
                        } else {
                            long l = this.level().getGameTime();
                            if (l - this.lastHit > 5L && !isDirectArrow) {
                                this.level().broadcastEntityEvent(this, (byte) 32);
                                this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
                                this.lastHit = l;
                            } else {
                                this.brokenByPlayer(source);
                                this.showBreakingParticles();
                                this.kill();
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 32) {
            if (this.level().isClientSide) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
                this.lastHit = this.level().getGameTime();
            }
        } else {
            super.handleEntityEvent(id);
        }

    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d) || d == 0.0) {
            d = 4.0;
        }
        d *= 64.0;
        return distance < d * d;
    }

    private void showBreakingParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(),
                    this.getY(0.6666666666666666), this.getZ(), 10,
                    this.getBbWidth() / 4.0F, this.getBbHeight() / 4.0F,
                    this.getBbWidth() / 4.0F, 0.05);
        }
    }

    private void causeDamage(DamageSource damageSource, float amount) {
        float f = this.getHealth();
        f -= amount;
        if (f <= 0.5F) {
            this.brokenByAnything(damageSource);
            this.kill();
        } else {
            this.setHealth(f);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        }
    }

    private void brokenByPlayer(DamageSource damageSource) {
        ItemStack itemStack = new ItemStack(ModRegistry.HAT_STAND.get());
        if (this.hasCustomName()) {
            itemStack.setHoverName(this.getCustomName());
        }

        Block.popResource(this.level(), this.blockPosition(), itemStack);
        this.brokenByAnything(damageSource);
    }

    private void brokenByAnything(DamageSource damageSource) {
        this.playBrokenSound();
        this.dropAllDeathLoot(damageSource);

        ItemStack itemStack;
        itemStack = this.helmet.get(0);
        if (!itemStack.isEmpty()) {
            Block.popResource(this.level(), this.blockPosition().above(), itemStack);
            this.helmet.set(0, ItemStack.EMPTY);
        }
    }

    private void playBrokenSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
        return 0.0F;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * (this.isBaby() ? 0.5F : 0.7F);
    }

    @Override
    public void setYBodyRot(float pOffset) {
        float r = this.getYRot();
        this.yRotO = r;
        this.yBodyRotO = this.yBodyRot = r;
    }

    @Override
    public void setYHeadRot(float pRotation) {
        float r = this.getYRot();
        this.yRotO = r;
        this.yHeadRotO = this.yHeadRot = r;
    }

    @Override
    public void tick() {
        super.tick();
        Rotations rotations = this.entityData.get(DATA_HEAD_POSE);
        if (!this.headPose.equals(rotations)) {
            this.setHeadPose(rotations);
        }
        if(this.level().isClientSide) {
            this.animation.tick(!level().getFluidState(getOnPos()).isEmpty());

        }
    }

    @Override
    protected void updateInvisibilityStatus() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean ignoreExplosion() {
        return this.isInvisible();
    }

    public void setHeadPose(Rotations headPose) {
        this.headPose = headPose;
        this.entityData.set(DATA_HEAD_POSE, headPose);
    }

    public Rotations getHeadPose() {
        return this.headPose;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return entity instanceof Player p && !this.level().mayInteract(p, this.blockPosition());
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_CLIENT_FLAGS.equals(key)) {
            this.refreshDimensions();
            this.blocksBuilding = true;
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModRegistry.HAT_STAND.get());
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return !this.isInvisible();
    }

}

