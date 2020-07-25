package untamedwilds.entity.mammal.bigcat;

import com.github.alexthe666.citadel.animation.Animation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.tuple.Pair;
import untamedwilds.UntamedWilds;
import untamedwilds.entity.ComplexMobTerrestrial;
import untamedwilds.init.ModEntity;
import untamedwilds.init.ModSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BigCatAbstract extends ComplexMobTerrestrial {

    public static Animation ATTACK_BITE;
    public static Animation ATTACK_MAUL;
    public static Animation ATTACK_POUNCE;
    public static Animation ANIMATION_ROAR;
    public static Animation ANIMATION_STAND;
    public static Animation ANIMATION_EAT;
    public static Animation IDLE_TALK;
    public int aggroProgress;

    public BigCatAbstract(EntityType<? extends BigCatAbstract> type, World worldIn) {
        super(type, worldIn);
        ATTACK_POUNCE = Animation.create(42);
        IDLE_TALK = Animation.create(20);
        this.stepHeight = 1;
        this.experienceValue = 10;
    }

    public boolean isActive() {
        if (this.forceSleep < 0) {
            return false;
        }
        float f = this.world.getCelestialAngle(0F);
        return f < 0.21F || f > 0.78;
    }

    /*public boolean wantsToBreed() {
        if (Config.doNaturalBreeding && this.growingAge == 0) {
            if (CompatBridge.SereneSeasons) {
                return (CompatSereneSeasons.isCurrentSeason(this.world, SpeciesBear.values()[this.getSpecies()].breedingSeason));
            }
            return this.getHunger() >= 80;
        }
        return false;
    }*/

    public void livingTick() {
        if (!this.world.isRemote) {
            /*if (!this.isMale() && !this.isChild() && this.growingAge < 100 && this.growingAge != 0) {
                EntityHelper.spawnOffspring(this, 1 + this.rand.nextInt(2), Config.HCBreeding ? 1008000 : 48000);
            }*/
            if (this.world.getGameTime() % 1000 == 0) {
                this.addHunger(-3);
                if (!this.isStarving()) {
                    this.heal(2.0F);
                }
            }
            if (this.isSleeping() && this.forceSleep == 0) {
                List<PlayerEntity> list = this.world.getEntitiesWithinAABB(PlayerEntity.class, this.getBoundingBox().grow(6.0D, 4.0D, 6.0D));
                if (!list.isEmpty()) {
                    PlayerEntity player = list.get(0);
                    if (!player.isShiftKeyDown() && !player.isCreative()) {
                        this.setSleeping(false);
                        this.setAttackTarget(player);
                        this.forceSleep = -300;
                    }
                }
            }
            // Random idle animations
            if (this.getAnimation() == NO_ANIMATION && this.getAttackTarget() == null && !this.isSleeping()) {
                if (this.getCommandInt() == 0) {
                    int i = this.rand.nextInt(3000);
                    if (i == 0 && !this.isInWater() && this.isNotMoving() && this.canMove() && this.isActive()) {
                        this.getNavigator().clearPath();
                        this.setSitting(true);
                    }
                    if ((i == 1 || this.isInWater()) && this.isSitting() && this.getCommandInt() < 2) {
                        this.setSitting(false);
                    }
                    if (i > 2980 && !this.isInWater() && !this.isChild()) {
                        this.setAnimation(IDLE_TALK);
                    }
                }
            }
            if (this.ticksExisted % 80 == 2 && this.getAttackTarget() != null && this.getAnimation() == NO_ANIMATION) {
                this.setAnimation(ANIMATION_ROAR);
            }
            this.setAngry(this.getAttackTarget() != null);
            int i = this.rand.nextInt(3000);
            if (i == 0 && !this.isInWater() && this.isNotMoving() && this.canMove() && this.getAnimation() == NO_ANIMATION) {
                this.getNavigator().clearPath();
                this.setSitting(true);
            }
            if (i == 1 && this.isSitting()) {
                this.setSitting(false);
            }
            if (i == 2 && this.canMove() && !this.isInWater() && !this.isChild() && this.getAnimation() == NO_ANIMATION) {
                this.setAnimation(ANIMATION_STAND);
            }
        }
        if (this.getAnimation() == ANIMATION_EAT && (this.getAnimationTick() == 10 || this.getAnimationTick() == 20 || this.getAnimationTick() == 30)) {
            this.playSound(SoundEvents.ENTITY_HORSE_EAT,1.5F, 0.8F);
        }
        if (this.getAnimation() == IDLE_TALK && this.getAnimationTick() == 1) {
            this.playSound(ModSounds.ENTITY_BIG_CAT_AMBIENT, 1.5F, 1);
        }
        if (this.world.isRemote && this.isAngry() && this.aggroProgress < 40) {
            this.aggroProgress++;
        } else if (this.world.isRemote && !this.isAngry() && this.aggroProgress > 0) {
            this.aggroProgress--;
        }
        super.livingTick();
    }

    public double getMountedYOffset() { return this.getModelScale() + 0.5f * this.getMobSize(); }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getAmbientSound() {
        return !this.isChild() ? null : ModSounds.ENTITY_BEAR_BABY_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return !this.isChild() ? ModSounds.ENTITY_BIG_CAT_HURT : SoundEvents.ENTITY_OCELOT_HURT;
    }

    protected SoundEvent getDeathSound() {
        return !this.isChild() ? ModSounds.ENTITY_BIG_CAT_DEATH : SoundEvents.ENTITY_OCELOT_DEATH;
    }

    public boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(Hand.MAIN_HAND);
        if (hand == Hand.MAIN_HAND && !this.world.isRemote()) { // Prevents all code from running twice
            if (!this.world.isRemote()) {
                if (player.isCreative() && itemstack.isEmpty()) {
                    UntamedWilds.LOGGER.info(this.getDistanceSq(this.getHomeAsVec()) + " | " + this.getPosition() + " | " + this.getHome());
                }

                if (this.isTamed() && this.getOwner() == player) {
                    if (itemstack.isEmpty()) {
                        this.setCommandInt(this.getCommandInt() + 1);
                        player.sendMessage(new TranslationTextComponent("entity.untamedwilds.command." + this.getCommandInt()));
                        if (this.getCommandInt() > 1) {
                            this.getNavigator().clearPath();
                            this.setSitting(true);
                        } else if (this.getCommandInt() <= 1 && this.isSitting()) {
                            this.setSitting(false);
                        }
                    }
                    if (itemstack.isFood()) {
                        this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, 1);
                        this.addHunger((itemstack.getItem().getFood().getHealing() * 10 * itemstack.getCount()));
                        for (Pair<EffectInstance, Float> pair : itemstack.getItem().getFood().getEffects()) {
                            if (pair.getLeft() != null && this.world.rand.nextFloat() < pair.getRight()) {
                                this.addPotionEffect(new EffectInstance(pair.getLeft()));
                            }
                        }
                    }
                    else if (itemstack.hasEffect()) {
                        this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1, 1);
                        this.addHunger(10);
                        for(EffectInstance effectinstance : PotionUtils.getEffectsFromStack(itemstack)) {
                            if (effectinstance.getPotion().isInstant()) {
                                effectinstance.getPotion().affectEntity(this.getOwner(), this.getOwner(), this, effectinstance.getAmplifier(), 1.0D);
                            } else {
                                this.addPotionEffect(new EffectInstance(effectinstance));
                            }
                        }
                    }
                }
            }
            if (!this.isTamed() && this.isChild() && this.getHealth() == this.getMaxHealth() && this.isFavouriteFood(itemstack)) {
                this.playSound(SoundEvents.ENTITY_HORSE_EAT, 1.5F, 0.8F);
                if (this.getRNG().nextInt(3) == 0) {
                    this.setTamedBy(player);
                    //this.registerGoals(); // AI Reset Hook
                    for (int i = 0; i < 6; i++) {
                        ((ServerWorld)this.world).spawnParticle(ParticleTypes.HEART, this.getPosX(), this.getPosY() + (double)this.getHeight() / 1.5D, this.getPosZ(), 3, this.getWidth() / 4.0F, this.getHeight() / 4.0F, this.getWidth() / 4.0F, 0.05D);
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        ((ServerWorld)this.world).spawnParticle(ParticleTypes.SMOKE, this.getPosX(), this.getPosY() + (double)this.getHeight() / 1.5D, this.getPosZ(), 3, this.getWidth() / 4.0F, this.getHeight() / 4.0F, this.getWidth() / 4.0F, 0.01D);
                    }
                }
            }
        }

        return super.processInteract(player, hand);
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        if (flag && this.getAnimation() == NO_ANIMATION && !this.isChild()) {
            Animation anim = chooseAttackAnimation(this);
            this.setAnimation(anim);
            this.setAnimation(anim);
            this.setAnimationTick(0);
        }
        return flag;
    }

    private Animation chooseAttackAnimation(Entity entityIn) {
        return ATTACK_POUNCE;
    }

    //public Animation getAnimationEat() { return ANIMATION_EAT; }
    @Override
    public Animation[] getAnimations() {
        return new Animation[]{NO_ANIMATION, ATTACK_POUNCE, IDLE_TALK};
    }

    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return sizeIn.height * 0.85F;
    }

    public enum SpeciesBigCat implements IStringSerializable {

        JAGUAR		(ModEntity.JAGUAR, BigCatJaguar.getRarity(), BiomeDictionary.Type.JUNGLE),
        LEOPARD		(ModEntity.LEOPARD, BigCatLeopard.getRarity(), BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.SPARSE),
        LION		(ModEntity.LION, BigCatLion.getRarity(), BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.SPARSE),
        PANTHER		(ModEntity.PANTHER, BigCatPanther.getRarity(), BiomeDictionary.Type.JUNGLE),
        PUMA		(ModEntity.PUMA, BigCatPuma.getRarity(), BiomeDictionary.Type.SPARSE, BiomeDictionary.Type.MESA, BiomeDictionary.Type.FOREST),
        SNOW_LEOPARD(ModEntity.SNOW_LEOPARD, BigCatSnowLeopard.getRarity(), BiomeDictionary.Type.SNOWY),
        TIGER		(ModEntity.TIGER, BigCatTiger.getRarity(), BiomeDictionary.Type.JUNGLE);

        public EntityType type;
        public int rarity;
        public BiomeDictionary.Type[] spawnBiomes;

        SpeciesBigCat(EntityType type, int rolls, BiomeDictionary.Type... biomes) {
            this.type = type;
            this.rarity = rolls;
            this.spawnBiomes = biomes;
        }

        @Override
        public String getName() {
            return "why would you do this?";
        }

        public static EntityType getSpeciesByBiome(Biome biome) {
            List<BigCatAbstract.SpeciesBigCat> types = new ArrayList<>();
            for (BigCatAbstract.SpeciesBigCat type : values()) {
                for(BiomeDictionary.Type biomeTypes : type.spawnBiomes) {
                    if(BiomeDictionary.hasType(biome, biomeTypes)){
                        for (int i = 0; i < type.rarity; i++) {
                            types.add(type);
                        }
                    }
                }
            }
            if (types.isEmpty()) {
                return null;
            }
            return types.get(new Random().nextInt(types.size())).type;
        }
    }
}