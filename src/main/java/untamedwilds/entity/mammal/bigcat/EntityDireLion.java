package untamedwilds.entity.mammal.bigcat;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.ai.*;
import untamedwilds.entity.ai.target.HuntMobTarget;
import untamedwilds.entity.ai.target.ProtectChildrenTarget;
import untamedwilds.init.ModEntity;
import untamedwilds.util.EntityUtils;

import java.util.List;

public class EntityDireLion extends AbstractBigCat {

    private static final ResourceLocation TEXTURE = new ResourceLocation("untamedwilds:textures/entity/big_cat/dire_lion.png");
    private static final float SIZE = 1.1f;
    private static final String BREEDING = "ALL";
    private static final int GESTATION = 5 * ConfigGamerules.cycleLength.get();
    private static final int GROWING = 11 * ConfigGamerules.cycleLength.get();
    private static final int RARITY = 1;

    public EntityDireLion(EntityType<? extends AbstractBigCat> type, World worldIn) {
        super(type, worldIn);
    }

    public void registerGoals() {
        this.goalSelector.addGoal(1, new SmartSwimGoal(this));
        this.goalSelector.addGoal(2, new FindItemsGoal(this, 12, true));
        this.goalSelector.addGoal(2, new SmartMeleeAttackGoal(this, 2.3D, false, 1));
        this.goalSelector.addGoal(3, new SmartAvoidGoal<>(this, LivingEntity.class, 16, 1.2D, 1.6D, input -> this.getEcoLevel(input) > 10));
        this.goalSelector.addGoal(4, new SmartMateGoal(this, 1D));
        this.goalSelector.addGoal(4, new GotoSleepGoal(this, 1D));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new SmartWanderGoal(this, 1D, true));
        this.goalSelector.addGoal(6, new SmartLookAtGoal(this, LivingEntity.class, 10.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ProtectChildrenTarget<>(this, LivingEntity.class, 0, true, true, input -> !(input instanceof EntityDireLion)));
        this.targetSelector.addGoal(3, new HuntMobTarget<>(this, LivingEntity.class, true, 30, false, input -> this.getEcoLevel(input) < 8));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 10.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.16D)
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 32D)
                .createMutableAttribute(Attributes.MAX_HEALTH, 45.0D)
                .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .createMutableAttribute(Attributes.ARMOR, 0D);
    }

    /* Crepuscular: Active between 10:00 and 1:00 */
    public boolean isActive() {
        super.isActive();
        long time = this.world.getDayTime();
        return time > 4000 && time < 19000;
    }

    /* Breeding conditions for the Cave Lion are:
     * Cold Biome (T between -1.0 and 0.4)
     * No other entities nearby */
    public boolean wantsToBreed() {
        if (super.wantsToBreed()) {
            if (!this.isSleeping() && this.getGrowingAge() == 0 && EntityUtils.hasFullHealth(this) && this.getHunger() >= 80) {
                if (ConfigGamerules.hardcoreBreeding.get()) {
                    List<LivingEntity> list = this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow(6.0D, 4.0D, 6.0D));
                    float i = this.world.getBiome(this.getPosition()).getTemperature(this.getPosition());
                    return i <= 0.4 && list.size() < 3;
                }
                return true;
            }
        }
        return false;
    }

    public EntityDireLion func_241840_a(ServerWorld serverWorld, AgeableEntity ageable) {
        EntityDireLion bear = new EntityDireLion(ModEntity.DIRE_LION, this.world);
        bear.setVariant(this.getVariant());
        bear.setGender(this.rand.nextInt(2));
        bear.setMobSize(this.rand.nextFloat());
        return bear;
    }

    public boolean isFavouriteFood(ItemStack stack) { return stack.getItem() == Items.BEEF; }
    public String getBreedingSeason() { return BREEDING; }
    public static int getRarity() { return RARITY; }
    public int getAdulthoodTime() { return GROWING; }
    public int getPregnancyTime() { return GESTATION; }
    public float getModelScale() { return SIZE; }
    public ResourceLocation getTexture() {
        return TEXTURE;
    }
    protected int getOffspring() { return 1; }
}
