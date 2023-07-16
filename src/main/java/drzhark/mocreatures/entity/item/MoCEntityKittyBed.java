/*
 * GNU GENERAL PUBLIC LICENSE Version 3
 */
package drzhark.mocreatures.entity.item;

import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.init.MoCItems;
import drzhark.mocreatures.init.MoCSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MoCEntityKittyBed extends EntityLiving {

    private static final DataParameter<Boolean> HAS_MILK = EntityDataManager.createKey(MoCEntityKittyBed.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_FOOD = EntityDataManager.createKey(MoCEntityKittyBed.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> PICKED_UP = EntityDataManager.createKey(MoCEntityKittyBed.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SHEET_COLOR = EntityDataManager.createKey(MoCEntityKittyBed.class, DataSerializers.VARINT);
    public float milklevel;

    public MoCEntityKittyBed(World world) {
        super(world);
        setSize(1.0F, 0.15F);
        this.milklevel = 0.0F;
    }

    public MoCEntityKittyBed(World world, double d, double d1, double d2) {
        super(world);
        setSize(1.0F, 0.15F);
        this.milklevel = 0.0F;
    }

    public MoCEntityKittyBed(World world, int i) {
        this(world);
        setSheetColor(i);
    }

    public ResourceLocation getTexture() {
        return MoCreatures.proxy.getModelTexture("kitty_bed.png");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D); // setMaxHealth
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HAS_MILK, Boolean.FALSE);
        this.dataManager.register(HAS_FOOD, Boolean.FALSE);
        this.dataManager.register(PICKED_UP, Boolean.FALSE);
        this.dataManager.register(SHEET_COLOR, 0);
    }

    public boolean getHasFood() {
        return this.dataManager.get(HAS_FOOD);
    }

    public void setHasFood(boolean flag) {
        this.dataManager.set(HAS_FOOD, flag);
    }

    public boolean getHasMilk() {
        return this.dataManager.get(HAS_MILK);
    }

    public void setHasMilk(boolean flag) {
        this.dataManager.set(HAS_MILK, flag);
    }

    public boolean getPickedUp() {
        return this.dataManager.get(PICKED_UP);
    }

    public void setPickedUp(boolean flag) {
        this.dataManager.set(PICKED_UP, flag);
    }

    public int getSheetColor() {
        return this.dataManager.get(SHEET_COLOR);
    }

    public void setSheetColor(int i) {
        this.dataManager.set(SHEET_COLOR, i);
        //this.bedColor = EnumDyeColor.byMetadata(i).getUnlocalizedName().toLowerCase();
    }

    public boolean attackEntityFrom(Entity entity, int i) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return !this.isDead;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean canEntityBeSeen(Entity entity) {
        return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + getEyeHeight(), this.posZ),
                new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ)) == null;
    }

    @Override
    public void fall(float f, float f1) {
    }

    @Override
    protected float getSoundVolume() {
        return 0.0F;
    }

    @Override
    public double getMountedYOffset() {
        return -0.1D;
    }

    @Override
    public void handleStatusUpdate(byte byte0) {
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && (stack.getItem() == Items.MILK_BUCKET)) {
            player.setHeldItem(hand, new ItemStack(Items.BUCKET, 1));
            MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_KITTYBED_POURINGMILK);
            setHasMilk(true);
            setHasFood(false);
            return true;
        }
        if (!stack.isEmpty() && !getHasFood() && (stack.getItem() == MoCItems.petfood)) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
            }
            MoCTools.playCustomSound(this, MoCSoundEvents.ENTITY_KITTYBED_POURINGFOOD);
            setHasMilk(false);
            setHasFood(true);
            return true;
        }
        if (player.isSneaking() && this.getRidingEntity() == null) {
            final int color = getSheetColor();
            player.inventory.addItemStackToInventory(new ItemStack(MoCItems.kittybed[color], 1));
            MoCTools.playCustomSound(this, SoundEvents.ENTITY_ITEM_PICKUP, 0.2F);
            setDead();
            return true;
        }
        if (this.getRidingEntity() == null) {
            if (this.startRiding(player)) {
                this.rotationYaw = player.rotationYaw;
                setPickedUp(true);
            }

            return true;
        }

        return true;
    }

    @Override
    public void move(MoverType type, double d, double d1, double d2) {
        if ((this.getRidingEntity() != null) || !this.onGround || !MoCreatures.proxy.staticBed) {
            if (!this.world.isRemote) {
                super.move(type, d, d1, d2);
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.onGround) {
            setPickedUp(false);
        }
        if ((getHasMilk() || getHasFood()) && (this.isBeingRidden()) && !this.world.isRemote) {
            this.milklevel += 0.003F;
            if (this.milklevel > 2.0F) {
                this.milklevel = 0.0F;
                setHasMilk(false);
                setHasFood(false);
            }
        }
        if (this.isRiding()) MoCTools.dismountSneakingPlayer(this);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        setHasMilk(nbttagcompound.getBoolean("HasMilk"));
        setSheetColor(nbttagcompound.getInteger("SheetColour"));
        setHasFood(nbttagcompound.getBoolean("HasFood"));
        this.milklevel = nbttagcompound.getFloat("MilkLevel");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("HasMilk", getHasMilk());
        nbttagcompound.setInteger("SheetColour", getSheetColor());
        nbttagcompound.setBoolean("HasFood", getHasFood());
        nbttagcompound.setFloat("MilkLevel", this.milklevel);
    }

    @Override
    public void onLivingUpdate() {
        this.moveStrafing = 0.0F;
        this.moveForward = 0.0F;
        this.randomYawVelocity = 0.0F;
        super.onLivingUpdate();
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        return false;
    }
}
