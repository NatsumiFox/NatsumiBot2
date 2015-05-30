package net.minecraft.server.v1_8_R3;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.world.StructureGrowEvent;

public final class ItemStack {

    public static final DecimalFormat a = new DecimalFormat("#.###");
    public int count;
    public int c;
    private Item item;
    private NBTTagCompound tag;
    private int damage;
    private EntityItemFrame g;
    private Block h;
    private boolean i;
    private Block j;
    private boolean k;

    public ItemStack(Block block) {
        this(block, 1);
    }

    public ItemStack(Block block, int i) {
        this(block, i, 0);
    }

    public ItemStack(Block block, int i, int j) {
        this(Item.getItemOf(block), i, j);
    }

    public ItemStack(Item item) {
        this(item, 1);
    }

    public ItemStack(Item item, int i) {
        this(item, i, 0);
    }

    public ItemStack(Item item, int i, int j) {
        this.h = null;
        this.i = false;
        this.j = null;
        this.k = false;
        this.item = item;
        this.count = i;
        this.setData(j);
    }

    public static ItemStack createStack(NBTTagCompound nbttagcompound) {
        ItemStack itemstack = new ItemStack();

        itemstack.c(nbttagcompound);
        return itemstack.getItem() != null ? itemstack : null;
    }

    private ItemStack() {
        this.h = null;
        this.i = false;
        this.j = null;
        this.k = false;
    }

    public ItemStack a(int i) {
        ItemStack itemstack = new ItemStack(this.item, i, this.damage);

        if (this.tag != null) {
            itemstack.tag = (NBTTagCompound) this.tag.clone();
        }

        this.count -= i;
        return itemstack;
    }

    public Item getItem() {
        return this.item;
    }

    public boolean placeItem(EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1, float f2) {
        int i = this.getData();
        int j = this.count;

        if (!(this.getItem() instanceof ItemBucket)) {
            world.captureBlockStates = true;
            if (this.getItem() instanceof ItemDye && this.getData() == 15) {
                Block block = world.getType(blockposition).getBlock();

                if (block == Blocks.SAPLING || block instanceof BlockMushroom) {
                    world.captureTreeGeneration = true;
                }
            }
        }

        boolean flag = this.getItem().interactWith(this, entityhuman, world, blockposition, enumdirection, f, f1, f2);
        int k = this.getData();
        int l = this.count;

        this.count = j;
        this.setData(i);
        world.captureBlockStates = false;
        if (flag && world.captureTreeGeneration && world.capturedBlockStates.size() > 0) {
            world.captureTreeGeneration = false;
            Location location = new Location(world.getWorld(), (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
            TreeType treetype = BlockSapling.treeType;

            BlockSapling.treeType = null;
            List list = (List) world.capturedBlockStates.clone();

            world.capturedBlockStates.clear();
            StructureGrowEvent structuregrowevent = null;

            if (treetype != null) {
                structuregrowevent = new StructureGrowEvent(location, treetype, false, (Player) entityhuman.getBukkitEntity(), list);
                Bukkit.getPluginManager().callEvent(structuregrowevent);
            }

            if (structuregrowevent == null || !structuregrowevent.isCancelled()) {
                if (this.count == j && this.getData() == i) {
                    this.setData(k);
                    this.count = l;
                }

                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    org.bukkit.block.BlockState org_bukkit_block_blockstate = (org.bukkit.block.BlockState) iterator.next();

                    org_bukkit_block_blockstate.update(true);
                }
            }

            return flag;
        } else {
            world.captureTreeGeneration = false;
            if (flag) {
                Object object = null;
                List list1 = (List) world.capturedBlockStates.clone();

                world.capturedBlockStates.clear();
                if (list1.size() > 1) {
                    object = CraftEventFactory.callBlockMultiPlaceEvent(world, entityhuman, list1, blockposition.getX(), blockposition.getY(), blockposition.getZ());
                } else if (list1.size() == 1) {
                    object = CraftEventFactory.callBlockPlaceEvent(world, entityhuman, (org.bukkit.block.BlockState) list1.get(0), blockposition.getX(), blockposition.getY(), blockposition.getZ());
                }

                org.bukkit.block.BlockState org_bukkit_block_blockstate1;
                Iterator iterator1;

                if (object != null && (((BlockPlaceEvent) object).isCancelled() || !((BlockPlaceEvent) object).canBuild())) {
                    flag = false;
                    iterator1 = list1.iterator();

                    while (iterator1.hasNext()) {
                        org_bukkit_block_blockstate1 = (org.bukkit.block.BlockState) iterator1.next();
                        org_bukkit_block_blockstate1.update(true, false);
                    }
                } else {
                    if (this.count == j && this.getData() == i) {
                        this.setData(k);
                        this.count = l;
                    }

                    int i1;
                    Block block1;
                    BlockPosition blockposition1;
                    IBlockData iblockdata;

                    for (iterator1 = list1.iterator(); iterator1.hasNext(); world.notifyAndUpdatePhysics(blockposition1, (Chunk) null, block1, iblockdata.getBlock(), i1)) {
                        org_bukkit_block_blockstate1 = (org.bukkit.block.BlockState) iterator1.next();
                        int j1 = org_bukkit_block_blockstate1.getX();
                        int k1 = org_bukkit_block_blockstate1.getY();
                        int l1 = org_bukkit_block_blockstate1.getZ();

                        i1 = ((CraftBlockState) org_bukkit_block_blockstate1).getFlag();
                        org.bukkit.Material org_bukkit_material = org_bukkit_block_blockstate1.getType();

                        block1 = CraftMagicNumbers.getBlock(org_bukkit_material);
                        blockposition1 = new BlockPosition(j1, k1, l1);
                        iblockdata = world.getType(blockposition1);
                        if (!(iblockdata instanceof BlockContainer)) {
                            iblockdata.getBlock().onPlace(world, blockposition1, iblockdata);
                        }
                    }

                    iterator1 = world.capturedTileEntities.entrySet().iterator();

                    while (iterator1.hasNext()) {
                        Entry entry = (Entry) iterator1.next();

                        world.setTileEntity((BlockPosition) entry.getKey(), (TileEntity) entry.getValue());
                    }

                    if (this.getItem() instanceof ItemRecord) {
                        ((BlockJukeBox) Blocks.JUKEBOX).a(world, blockposition, world.getType(blockposition), this);
                        world.a((EntityHuman) null, 1005, blockposition, Item.getId(this.getItem()));
                        --this.count;
                        entityhuman.b(StatisticList.X);
                    }

                    if (this.getItem() == Items.SKULL) {
                        BlockPosition blockposition2 = blockposition;

                        if (!world.getType(blockposition).getBlock().a(world, blockposition)) {
                            if (!world.getType(blockposition).getBlock().getMaterial().isBuildable()) {
                                blockposition2 = null;
                            } else {
                                blockposition2 = blockposition.shift(enumdirection);
                            }
                        }

                        if (blockposition2 != null) {
                            TileEntity tileentity = world.getTileEntity(blockposition2);

                            if (tileentity instanceof TileEntitySkull) {
                                Blocks.SKULL.a(world, blockposition2, (TileEntitySkull) tileentity);
                            }
                        }
                    }

                    entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this.item)]);
                }
            }

            world.capturedTileEntities.clear();
            world.capturedBlockStates.clear();
            return flag;
        }
    }

    public float a(Block block) {
        return this.getItem().getDestroySpeed(this, block);
    }

    public ItemStack a(World world, EntityHuman entityhuman) {
        return this.getItem().a(this, world, entityhuman);
    }

    public ItemStack b(World world, EntityHuman entityhuman) {
        return this.getItem().b(this, world, entityhuman);
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = (MinecraftKey) Item.REGISTRY.c(this.item);

        nbttagcompound.setString("id", minecraftkey == null ? "minecraft:air" : minecraftkey.toString());
        nbttagcompound.setByte("Count", (byte) this.count);
        nbttagcompound.setShort("Damage", (short) this.damage);
        if (this.tag != null) {
            nbttagcompound.set("tag", this.tag.clone());
        }

        return nbttagcompound;
    }

    public void c(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKeyOfType("id", 8)) {
            this.item = Item.d(nbttagcompound.getString("id"));
        } else {
            this.item = Item.getById(nbttagcompound.getShort("id"));
        }

        this.count = nbttagcompound.getByte("Count");
        this.setData(nbttagcompound.getShort("Damage"));
        if (nbttagcompound.hasKeyOfType("tag", 10)) {
            this.tag = (NBTTagCompound) nbttagcompound.getCompound("tag").clone();
            if (this.item != null) {
                this.item.a(this.tag);
            }
        }

    }

    public int getMaxStackSize() {
        return this.getItem().getMaxStackSize();
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.e() || !this.g());
    }

    public boolean e() {
        return this.item.getMaxDurability() <= 0 ? false : !this.hasTag() || !this.getTag().getBoolean("Unbreakable");
    }

    public boolean usesData() {
        return this.item.k();
    }

    public boolean g() {
        return this.e() && this.damage > 0;
    }

    public int h() {
        return this.damage;
    }

    public int getData() {
        return this.damage;
    }

    public void setData(int i) {
        if (i == 32767) {
            this.damage = i;
        } else {
            if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) != Blocks.AIR && !this.usesData() && !this.getItem().usesDurability()) {
                i = 0;
            }

            if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) == Blocks.DOUBLE_PLANT && (i > 5 || i < 0)) {
                i = 0;
            }

            this.damage = i;
            if (this.damage < -1) {
                this.damage = 0;
            }

        }
    }

    public int j() {
        return this.item.getMaxDurability();
    }

    public boolean isDamaged(int i, Random random) {
        return this.isDamaged(i, random, (EntityLiving) null);
    }

    public boolean isDamaged(int i, Random random, EntityLiving entityliving) {
        if (!this.e()) {
            return false;
        } else {
            if (i > 0) {
                int j = EnchantmentManager.getEnchantmentLevel(Enchantment.DURABILITY.id, this);
                int k = 0;

                for (int l = 0; j > 0 && l < i; ++l) {
                    if (EnchantmentDurability.a(this, j, random)) {
                        ++k;
                    }
                }

                i -= k;
                if (entityliving instanceof EntityPlayer) {
                    CraftItemStack craftitemstack = CraftItemStack.asCraftMirror(this);
                    PlayerItemDamageEvent playeritemdamageevent = new PlayerItemDamageEvent((Player) entityliving.getBukkitEntity(), craftitemstack, i);

                    Bukkit.getServer().getPluginManager().callEvent(playeritemdamageevent);
                    if (playeritemdamageevent.isCancelled()) {
                        return false;
                    }

                    i = playeritemdamageevent.getDamage();
                }

                if (i <= 0) {
                    return false;
                }
            }

            this.damage += i;
            return this.damage > this.j();
        }
    }

    public void damage(int i, EntityLiving entityliving) {
        if ((!(entityliving instanceof EntityHuman) || !((EntityHuman) entityliving).abilities.canInstantlyBuild) && this.e() && this.isDamaged(i, entityliving.bc(), entityliving)) {
            entityliving.b(this);
            --this.count;
            if (entityliving instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entityliving;

                entityhuman.b(StatisticList.BREAK_ITEM_COUNT[Item.getId(this.item)]);
                if (this.count == 0 && this.getItem() instanceof ItemBow) {
                    entityhuman.ca();
                }
            }

            if (this.count < 0) {
                this.count = 0;
            }

            if (this.count == 0 && entityliving instanceof EntityHuman) {
                CraftEventFactory.callPlayerItemBreakEvent((EntityHuman) entityliving, this);
            }

            this.damage = 0;
        }

    }

    public void a(EntityLiving entityliving, EntityHuman entityhuman) {
        boolean flag = this.item.a(this, entityliving, (EntityLiving) entityhuman);

        if (flag) {
            entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this.item)]);
        }

    }

    public void a(World world, Block block, BlockPosition blockposition, EntityHuman entityhuman) {
        boolean flag = this.item.a(this, world, block, blockposition, entityhuman);

        if (flag) {
            entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this.item)]);
        }

    }

    public boolean b(Block block) {
        return this.item.canDestroySpecialBlock(block);
    }

    public boolean a(EntityHuman entityhuman, EntityLiving entityliving) {
        return this.item.a(this, entityhuman, entityliving);
    }

    public ItemStack cloneItemStack() {
        ItemStack itemstack = new ItemStack(this.item, this.count, this.damage);

        if (this.tag != null) {
            itemstack.tag = (NBTTagCompound) this.tag.clone();
        }

        return itemstack;
    }

    public static boolean equals(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == null && itemstack1 == null ? true : (itemstack != null && itemstack1 != null ? (itemstack.tag == null && itemstack1.tag != null ? false : itemstack.tag == null || itemstack.tag.equals(itemstack1.tag)) : false);
    }

    public static boolean fastMatches(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == null && itemstack1 == null ? true : (itemstack != null && itemstack1 != null ? itemstack.count == itemstack1.count && itemstack.item == itemstack1.item && itemstack.damage == itemstack1.damage : false);
    }

    public static boolean matches(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == null && itemstack1 == null ? true : (itemstack != null && itemstack1 != null ? itemstack.d(itemstack1) : false);
    }

    private boolean d(ItemStack itemstack) {
        return this.count != itemstack.count ? false : (this.item != itemstack.item ? false : (this.damage != itemstack.damage ? false : (this.tag == null && itemstack.tag != null ? false : this.tag == null || this.tag.equals(itemstack.tag))));
    }

    public static boolean c(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == null && itemstack1 == null ? true : (itemstack != null && itemstack1 != null ? itemstack.doMaterialsMatch(itemstack1) : false);
    }

    public boolean doMaterialsMatch(ItemStack itemstack) {
        return itemstack != null && this.item == itemstack.item && this.damage == itemstack.damage;
    }

    public String a() {
        return this.item.e_(this);
    }

    public static ItemStack b(ItemStack itemstack) {
        return itemstack == null ? null : itemstack.cloneItemStack();
    }

    public String toString() {
        return this.count + "x" + this.item.getName() + "@" + this.damage;
    }

    public void a(World world, Entity entity, int i, boolean flag) {
        if (this.c > 0) {
            --this.c;
        }

        this.item.a(this, world, entity, i, flag);
    }

    public void a(World world, EntityHuman entityhuman, int i) {
        entityhuman.a(StatisticList.CRAFT_BLOCK_COUNT[Item.getId(this.item)], i);
        this.item.d(this, world, entityhuman);
    }

    public int l() {
        return this.getItem().d(this);
    }

    public EnumAnimation m() {
        return this.getItem().e(this);
    }

    public void b(World world, EntityHuman entityhuman, int i) {
        this.getItem().a(this, world, entityhuman, i);
    }

    public boolean hasTag() {
        return this.tag != null;
    }

    public NBTTagCompound getTag() {
        return this.tag;
    }

    public NBTTagCompound a(String s, boolean flag) {
        if (this.tag != null && this.tag.hasKeyOfType(s, 10)) {
            return this.tag.getCompound(s);
        } else if (flag) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            this.a(s, (NBTBase) nbttagcompound);
            return nbttagcompound;
        } else {
            return null;
        }
    }

    public NBTTagList getEnchantments() {
        return this.tag == null ? null : this.tag.getList("ench", 10);
    }

    public void setTag(NBTTagCompound nbttagcompound) {
        this.tag = nbttagcompound;
    }

    public String getName() {
        String s = this.getItem().a(this);

        if (this.tag != null && this.tag.hasKeyOfType("display", 10)) {
            NBTTagCompound nbttagcompound = this.tag.getCompound("display");

            if (nbttagcompound.hasKeyOfType("Name", 8)) {
                s = nbttagcompound.getString("Name");
            }
        }

        return s;
    }

    public ItemStack c(String s) {
        if (this.tag == null) {
            this.tag = new NBTTagCompound();
        }

        if (!this.tag.hasKeyOfType("display", 10)) {
            this.tag.set("display", new NBTTagCompound());
        }

        this.tag.getCompound("display").setString("Name", s);
        return this;
    }

    public void r() {
        if (this.tag != null && this.tag.hasKeyOfType("display", 10)) {
            NBTTagCompound nbttagcompound = this.tag.getCompound("display");

            nbttagcompound.remove("Name");
            if (nbttagcompound.isEmpty()) {
                this.tag.remove("display");
                if (this.tag.isEmpty()) {
                    this.setTag((NBTTagCompound) null);
                }
            }
        }

    }

    public boolean hasName() {
        return this.tag == null ? false : (!this.tag.hasKeyOfType("display", 10) ? false : this.tag.getCompound("display").hasKeyOfType("Name", 8));
    }

    public EnumItemRarity u() {
        return this.getItem().g(this);
    }

    public boolean v() {
        return !this.getItem().f_(this) ? false : !this.hasEnchantments();
    }

    public void addEnchantment(Enchantment enchantment, int i) {
        if (this.tag == null) {
            this.setTag(new NBTTagCompound());
        }

        if (!this.tag.hasKeyOfType("ench", 9)) {
            this.tag.set("ench", new NBTTagList());
        }

        NBTTagList nbttaglist = this.tag.getList("ench", 10);
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.setShort("id", (short) enchantment.id);
        nbttagcompound.setShort("lvl", (short) ((byte) i));
        nbttaglist.add(nbttagcompound);
    }

    public boolean hasEnchantments() {
        return this.tag != null && this.tag.hasKeyOfType("ench", 9);
    }

    public void a(String s, NBTBase nbtbase) {
        if (this.tag == null) {
            this.setTag(new NBTTagCompound());
        }

        this.tag.set(s, nbtbase);
    }

    public boolean x() {
        return this.getItem().s();
    }

    public boolean y() {
        return this.g != null;
    }

    public void a(EntityItemFrame entityitemframe) {
        this.g = entityitemframe;
    }

    public EntityItemFrame z() {
        return this.g;
    }

    public int getRepairCost() {
        return this.hasTag() && this.tag.hasKeyOfType("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
    }

    public void setRepairCost(int i) {
        if (!this.hasTag()) {
            this.tag = new NBTTagCompound();
        }

        this.tag.setInt("RepairCost", i);
    }

    public Multimap<String, AttributeModifier> B() {
        Object object;

        if (this.hasTag() && this.tag.hasKeyOfType("AttributeModifiers", 9)) {
            object = HashMultimap.create();
            NBTTagList nbttaglist = this.tag.getList("AttributeModifiers", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.get(i);
                AttributeModifier attributemodifier = GenericAttributes.a(nbttagcompound);

                if (attributemodifier != null && attributemodifier.a().getLeastSignificantBits() != 0L && attributemodifier.a().getMostSignificantBits() != 0L) {
                    ((Multimap) object).put(nbttagcompound.getString("AttributeName"), attributemodifier);
                }
            }
        } else {
            object = this.getItem().i();
        }

        return (Multimap) object;
    }

    public void setItem(Item item) {
        this.item = item;
        this.setData(this.getData());
    }

    public IChatBaseComponent C() {
        ChatComponentText chatcomponenttext = new ChatComponentText(this.getName());

        if (this.hasName()) {
            chatcomponenttext.getChatModifier().setItalic(Boolean.valueOf(true));
        }

        IChatBaseComponent ichatbasecomponent = (new ChatComponentText("[")).addSibling(chatcomponenttext).a("]");

        if (this.item != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            this.save(nbttagcompound);
            ichatbasecomponent.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM, new ChatComponentText(nbttagcompound.toString())));
            ichatbasecomponent.getChatModifier().setColor(this.u().e);
        }

        return ichatbasecomponent;
    }

    public boolean c(Block block) {
        if (block == this.h) {
            return this.i;
        } else {
            this.h = block;
            if (this.hasTag() && this.tag.hasKeyOfType("CanDestroy", 9)) {
                NBTTagList nbttaglist = this.tag.getList("CanDestroy", 8);

                for (int i = 0; i < nbttaglist.size(); ++i) {
                    Block block1 = Block.getByName(nbttaglist.getString(i));

                    if (block1 == block) {
                        this.i = true;
                        return true;
                    }
                }
            }

            this.i = false;
            return false;
        }
    }

    public boolean d(Block block) {
        if (block == this.j) {
            return this.k;
        } else {
            this.j = block;
            if (this.hasTag() && this.tag.hasKeyOfType("CanPlaceOn", 9)) {
                NBTTagList nbttaglist = this.tag.getList("CanPlaceOn", 8);

                for (int i = 0; i < nbttaglist.size(); ++i) {
                    Block block1 = Block.getByName(nbttaglist.getString(i));

                    if (block1 == block) {
                        this.k = true;
                        return true;
                    }
                }
            }

            this.k = false;
            return false;
        }
    }
}
