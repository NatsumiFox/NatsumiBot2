package net.minecraft.server.v1_8_R3;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class ContainerAnvil extends Container {

    private static final Logger f = LogManager.getLogger();
    private IInventory g = new InventoryCraftResult();
    private IInventory h = new InventorySubcontainer("Repair", true, 2) {
        public void update() {
            super.update();
            ContainerAnvil.this.a((IInventory) this);
        }
    };
    private World i;
    private BlockPosition j;
    public int a;
    private int k;
    private String l;
    private final EntityHuman m;
    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory player;

    public ContainerAnvil(PlayerInventory playerinventory, final World world, final BlockPosition blockposition, EntityHuman entityhuman) {
        this.player = playerinventory;
        this.j = blockposition;
        this.i = world;
        this.m = entityhuman;
        this.a(new Slot(this.h, 0, 27, 47));
        this.a(new Slot(this.h, 1, 76, 47));
        this.a(new Slot(this.g, 2, 134, 47) {
            public boolean isAllowed(ItemStack itemstack) {
                return false;
            }

            public boolean isAllowed(EntityHuman entityhuman) {
                return (entityhuman.abilities.canInstantlyBuild || entityhuman.expLevel >= ContainerAnvil.this.a) && ContainerAnvil.this.a > 0 && this.hasItem();
            }

            public void a(EntityHuman entityhuman, ItemStack itemstack) {
                if (!entityhuman.abilities.canInstantlyBuild) {
                    entityhuman.levelDown(-ContainerAnvil.this.a);
                }

                ContainerAnvil.this.h.setItem(0, (ItemStack) null);
                if (ContainerAnvil.this.k > 0) {
                    ItemStack itemstack1 = ContainerAnvil.this.h.getItem(1);

                    if (itemstack1 != null && itemstack1.count > ContainerAnvil.this.k) {
                        itemstack1.count -= ContainerAnvil.this.k;
                        ContainerAnvil.this.h.setItem(1, itemstack1);
                    } else {
                        ContainerAnvil.this.h.setItem(1, (ItemStack) null);
                    }
                } else {
                    ContainerAnvil.this.h.setItem(1, (ItemStack) null);
                }

                ContainerAnvil.this.a = 0;
                IBlockData iblockdata = world.getType(blockposition);

                if (!entityhuman.abilities.canInstantlyBuild && !world.isClientSide && iblockdata.getBlock() == Blocks.ANVIL && entityhuman.bc().nextFloat() < 0.12F) {
                    int i = ((Integer) iblockdata.get(BlockAnvil.DAMAGE)).intValue();

                    ++i;
                    if (i > 2) {
                        world.setAir(blockposition);
                        world.triggerEffect(1020, blockposition, 0);
                    } else {
                        world.setTypeAndData(blockposition, iblockdata.set(BlockAnvil.DAMAGE, Integer.valueOf(i)), 2);
                        world.triggerEffect(1021, blockposition, 0);
                    }
                } else if (!world.isClientSide) {
                    world.triggerEffect(1021, blockposition, 0);
                }

            }
        });

        int i;

        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.a(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.a(new Slot(playerinventory, i, 8 + i * 18, 142));
        }

    }

    public void a(IInventory iinventory) {
        super.a(iinventory);
        if (iinventory == this.h) {
            this.e();
        }

    }

    public void e() {
        ItemStack itemstack = this.h.getItem(0);

        this.a = 1;
        int i = 0;
        byte b0 = 0;
        byte b1 = 0;

        if (itemstack == null) {
            this.g.setItem(0, (ItemStack) null);
            this.a = 0;
        } else {
            ItemStack itemstack1 = itemstack.cloneItemStack();
            ItemStack itemstack2 = this.h.getItem(1);
            Map map = EnchantmentManager.a(itemstack1);
            boolean flag = false;
            int j = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());

            this.k = 0;
            int k;

            if (itemstack2 != null) {
                flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && Items.ENCHANTED_BOOK.h(itemstack2).size() > 0;
                int l;
                int i1;

                if (itemstack1.e() && itemstack1.getItem().a(itemstack, itemstack2)) {
                    k = Math.min(itemstack1.h(), itemstack1.j() / 4);
                    if (k <= 0) {
                        this.g.setItem(0, (ItemStack) null);
                        this.a = 0;
                        return;
                    }

                    for (l = 0; k > 0 && l < itemstack2.count; ++l) {
                        i1 = itemstack1.h() - k;
                        itemstack1.setData(i1);
                        ++i;
                        k = Math.min(itemstack1.h(), itemstack1.j() / 4);
                    }

                    this.k = l;
                } else {
                    if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.e())) {
                        this.g.setItem(0, (ItemStack) null);
                        this.a = 0;
                        return;
                    }

                    int j1;

                    if (itemstack1.e() && !flag) {
                        k = itemstack.j() - itemstack.h();
                        l = itemstack2.j() - itemstack2.h();
                        i1 = l + itemstack1.j() * 12 / 100;
                        int k1 = k + i1;

                        j1 = itemstack1.j() - k1;
                        if (j1 < 0) {
                            j1 = 0;
                        }

                        if (j1 < itemstack1.getData()) {
                            itemstack1.setData(j1);
                            i += 2;
                        }
                    }

                    Map map1 = EnchantmentManager.a(itemstack2);
                    Iterator iterator = map1.keySet().iterator();

                    while (iterator.hasNext()) {
                        i1 = ((Integer) iterator.next()).intValue();
                        Enchantment enchantment = Enchantment.getById(i1);

                        if (enchantment != null) {
                            j1 = map.containsKey(Integer.valueOf(i1)) ? ((Integer) map.get(Integer.valueOf(i1))).intValue() : 0;
                            int l1 = ((Integer) map1.get(Integer.valueOf(i1))).intValue();
                            int i2;

                            if (j1 == l1) {
                                ++l1;
                                i2 = l1;
                            } else {
                                i2 = Math.max(l1, j1);
                            }

                            l1 = i2;
                            boolean flag1 = enchantment.canEnchant(itemstack);

                            if (this.m.abilities.canInstantlyBuild || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                                flag1 = true;
                            }

                            Iterator iterator1 = map.keySet().iterator();

                            int j2;

                            while (iterator1.hasNext()) {
                                j2 = ((Integer) iterator1.next()).intValue();
                                if (j2 != i1 && !enchantment.a(Enchantment.getById(j2))) {
                                    flag1 = false;
                                    ++i;
                                }
                            }

                            if (flag1) {
                                if (i2 > enchantment.getMaxLevel()) {
                                    l1 = enchantment.getMaxLevel();
                                }

                                map.put(Integer.valueOf(i1), Integer.valueOf(l1));
                                j2 = 0;
                                switch (enchantment.getRandomWeight()) {
                                case 1:
                                    j2 = 8;
                                    break;

                                case 2:
                                    j2 = 4;

                                case 3:
                                case 4:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                default:
                                    break;

                                case 5:
                                    j2 = 2;
                                    break;

                                case 10:
                                    j2 = 1;
                                }

                                if (flag) {
                                    j2 = Math.max(1, j2 / 2);
                                }

                                i += j2 * l1;
                            }
                        }
                    }
                }
            }

            if (StringUtils.isBlank(this.l)) {
                if (itemstack.hasName()) {
                    b1 = 1;
                    i += b1;
                    itemstack1.r();
                }
            } else if (!this.l.equals(itemstack.getName())) {
                b1 = 1;
                i += b1;
                itemstack1.c(this.l);
            }

            this.a = j + i;

            if (itemstack1 != null) {
                k = itemstack1.getRepairCost();
                if (itemstack2 != null && k < itemstack2.getRepairCost()) {
                    k = itemstack2.getRepairCost();
                }

                k = (int) (k * 1.3 + 1);
                itemstack1.setRepairCost(k);
                EnchantmentManager.a(map, itemstack1);
            }

            this.g.setItem(0, itemstack1);
            this.b();
        }

    }

    public void addSlotListener(ICrafting icrafting) {
        super.addSlotListener(icrafting);
        icrafting.setContainerData(this, 0, this.a);
    }

    public void b(EntityHuman entityhuman) {
        super.b(entityhuman);
        if (!this.i.isClientSide) {
            for (int i = 0; i < this.h.getSize(); ++i) {
                ItemStack itemstack = this.h.splitWithoutUpdate(i);

                if (itemstack != null) {
                    entityhuman.drop(itemstack, false);
                }
            }
        }

    }

    public boolean a(EntityHuman entityhuman) {
        return !this.checkReachable ? true : (this.i.getType(this.j).getBlock() != Blocks.ANVIL ? false : entityhuman.e((double) this.j.getX() + 0.5D, (double) this.j.getY() + 0.5D, (double) this.j.getZ() + 0.5D) <= 64.0D);
    }

    public ItemStack b(EntityHuman entityhuman, int i) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.c.get(i);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();

            itemstack = itemstack1.cloneItemStack();
            if (i == 2) {
                if (!this.a(itemstack1, 3, 39, true)) {
                    return null;
                }

                slot.a(itemstack1, itemstack);
            } else if (i != 0 && i != 1) {
                if (i >= 3 && i < 39 && !this.a(itemstack1, 0, 2, false)) {
                    return null;
                }
            } else if (!this.a(itemstack1, 3, 39, false)) {
                return null;
            }

            if (itemstack1.count == 0) {
                slot.set((ItemStack) null);
            } else {
                slot.f();
            }

            if (itemstack1.count == itemstack.count) {
                return null;
            }

            slot.a(entityhuman, itemstack1);
        }

        return itemstack;
    }

    public void a(String s) {
        this.l = s;
        if (this.getSlot(2).hasItem()) {
            ItemStack itemstack = this.getSlot(2).getItem();

            if (StringUtils.isBlank(s)) {
                itemstack.r();
            } else {
                itemstack.c(this.l);
            }
        }

        this.e();
    }

    public CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        } else {
            CraftInventoryAnvil craftinventoryanvil = new CraftInventoryAnvil(this.h, this.g);

            this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), craftinventoryanvil, this);
            return this.bukkitEntity;
        }
    }
}
