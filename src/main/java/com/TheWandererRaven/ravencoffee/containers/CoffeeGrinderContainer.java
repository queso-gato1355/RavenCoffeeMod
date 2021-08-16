package com.TheWandererRaven.ravencoffee.containers;

import com.TheWandererRaven.ravencoffee.containers.inventory.CoffeeGrinderContents;
import com.TheWandererRaven.ravencoffee.containers.slots.CoffeeGrinderOutputSlot;
import com.TheWandererRaven.ravencoffee.tileEntity.CoffeeGrinderTileEntity;
import com.TheWandererRaven.ravencoffee.util.registries.ContainersRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoffeeGrinderContainer extends Container {
    private final World world;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int INPUT_SLOT_COUNT = CoffeeGrinderTileEntity.INPUT_SLOTS_COUNT;  // must match TileEntityInventoryBasic.NUMBER_OF_SLOTS
    private static final int OUTPUT_SLOT_COUNT = CoffeeGrinderTileEntity.OUTPUT_SLOTS_COUNT;
    private static final int COFFEE_GRINDER_SLOTS_COUNT = INPUT_SLOT_COUNT + OUTPUT_SLOT_COUNT;

    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int HOTBAR_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX;
    private static final int PLAYER_INVENTORY_FIRST_SLOT_INDEX = HOTBAR_FIRST_SLOT_INDEX + HOTBAR_SLOT_COUNT;
    private static final int FIRST_INPUT_SLOT_INDEX = PLAYER_INVENTORY_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int FIRST_OUTPUT_SLOT_INDEX = FIRST_INPUT_SLOT_INDEX + INPUT_SLOT_COUNT;

    public static final int PLAYER_HOTBAR_XPOS = 8;
    public static final int PLAYER_HOTBAR_YPOS = 142;
    public static final int PLAYER_INVENTORY_XPOS = 8;
    public static final int PLAYER_INVENTORY_YPOS = 84;

    public static final int OUTPUT_SLOT_POS_X = 116;
    public static final int OUTPUT_SLOT_POS_Y = 35;

    public static final int INPUT_SLOT_POS_X = 56;
    public static final int INPUT_SLOT_Y_SPACING = 19;
    public static final int INPUT_SLOT_POS_Y = 25;

    public static final int SLOT_X_SPACING = 18;
    public static final int SLOT_Y_SPACING = 18;

    private CoffeeGrinderContents inputZoneContents;
    private CoffeeGrinderContents outputZoneContents;
    private static final Logger LOGGER = LogManager.getLogger();

    public static CoffeeGrinderContainer createContainerServerSide(int windowID, PlayerInventory playerInventory,
                                                                   CoffeeGrinderContents inputZoneContents,
                                                                   CoffeeGrinderContents outputZoneContents) {
        return new CoffeeGrinderContainer(windowID, playerInventory, inputZoneContents, outputZoneContents);
    }

    public static CoffeeGrinderContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData) {
        //  don't need extraData for this example; if you want you can use it to provide extra information from the server, that you can use
        //  when creating the client container
        //  eg String detailedDescription = extraData.readString(128);
        CoffeeGrinderContents inputZoneContents = CoffeeGrinderContents.createForClientSideContainer(CoffeeGrinderTileEntity.INPUT_SLOTS_COUNT);
        CoffeeGrinderContents outputZoneContents = CoffeeGrinderContents.createForClientSideContainer(CoffeeGrinderTileEntity.OUTPUT_SLOTS_COUNT);

        // on the client side there is no parent TileEntity to communicate with, so we:
        // 1) use a dummy inventory
        // 2) use "do nothing" lambda functions for canPlayerAccessInventory and markDirty
        return new CoffeeGrinderContainer(windowID, playerInventory, inputZoneContents, outputZoneContents);
    }

    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)


    /**
     * Creates a container suitable for server side or client side
     * @param windowID ID of the container
     * @param playerInventory the inventory of the player
     * @param chestContents the inventory stored in the chest
     */
    private CoffeeGrinderContainer(int windowID, PlayerInventory playerInventory, CoffeeGrinderContents inputZoneContents, CoffeeGrinderContents outputZoneContents) {
        super(ContainersRegistry.COFFEE_GRINDER_CONTAINER.get(), windowID);
        if (ContainersRegistry.COFFEE_GRINDER_CONTAINER == null)
            throw new IllegalStateException("Must initialise containerBasicContainerType before constructing a ContainerBasic!");

        this.world = playerInventory.player.world;
        PlayerInvWrapper playerInventoryForge = new PlayerInvWrapper(playerInventory);  // wrap the IInventory in a Forge IItemHandler.
        // Not actually necessary - can use Slot(playerInventory) instead of SlotItemHandler(playerInventoryForge)
        this.inputZoneContents = inputZoneContents;
        this.outputZoneContents = outputZoneContents;

        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            int slotNumber = x;
            addSlot(new Slot(playerInventory, slotNumber, PLAYER_HOTBAR_XPOS + SLOT_X_SPACING * x, PLAYER_HOTBAR_YPOS));
        }

        // Add the rest of the players inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new Slot(playerInventory, slotNumber,  xpos, ypos));
            }
        }

        // Add the tile input containers to the gui
        for (int x = 0; x < INPUT_SLOT_COUNT; x++) {
            int slotNumber = x;
            addSlot(new Slot(inputZoneContents, slotNumber, INPUT_SLOT_POS_X,  INPUT_SLOT_POS_Y + INPUT_SLOT_Y_SPACING * x));
        }

        for (int y = 0; y < OUTPUT_SLOT_COUNT; y++) {
            int slotNumber = y;
            addSlot(new CoffeeGrinderOutputSlot(outputZoneContents, slotNumber, OUTPUT_SLOT_POS_X,  OUTPUT_SLOT_POS_Y + SLOT_Y_SPACING * y));
        }
    }

    // Vanilla calls this method every tick to make sure the player is still able to access the inventory, and if not closes the gui
    // Called on the SERVER side only
    @Override
    public boolean canInteractWith(PlayerEntity playerEntity)
    {
        // This is typically a check that the player is within 8 blocks of the container.
        //  Some containers perform it using just the block placement:
        //  return isWithinUsableDistance(this.iWorldPosCallable, playerIn, Blocks.MYBLOCK); eg see BeaconContainer
        //  where iWorldPosCallable is a lambda that retrieves the blockstate at a particular world blockpos
        // for other containers, it defers to the IInventory provided to the Container (i.e. the TileEntity) which does the same
        //  calculation
        // return this.furnaceInventory.isUsableByPlayer(playerEntity);
        // Sometimes it perform an additional check (eg for EnderChests - the player owns the chest)

        return inputZoneContents.isUsableByPlayer(playerEntity) && outputZoneContents.isUsableByPlayer(playerEntity);
    }

    // This is where you specify what happens when a player shift clicks a slot in the gui
    //  (when you shift click a slot in the TileEntity Inventory, it moves it to the first available position in the hotbar and/or
    //    player inventory.  When you you shift-click a hotbar or player inventory item, it moves it to the first available
    //    position in the TileEntity inventory)
    // At the very least you must override this and return ItemStack.EMPTY or the game will crash when the player shift clicks a slot
    // returns ItemStack.EMPTY if the source slot is empty, or if none of the the source slot item could be moved
    //   otherwise, returns a copy of the source stack
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotIndex)
    {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (sourceSlotIndex >= VANILLA_FIRST_SLOT_INDEX && sourceSlotIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!mergeItemStack(sourceStack, PLAYER_INVENTORY_FIRST_SLOT_INDEX, PLAYER_INVENTORY_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT, false)){
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (sourceSlotIndex >= PLAYER_INVENTORY_FIRST_SLOT_INDEX && sourceSlotIndex < PLAYER_INVENTORY_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!mergeItemStack(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            LOGGER.warn("Invalid slotIndex:" + sourceSlotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.putStack(ItemStack.EMPTY);
        } else {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(playerEntity, sourceStack);
        return copyOfSourceStack;
    }

    // pass the close container message to the parent inventory (not strictly needed for this example)
    //  see ContainerChest and TileEntityChest - used to animate the lid when no players are accessing the chest any more
    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
    }

    /*
    private void updateRecipeOutput() {
        ItemStack itemstack = this.inputZoneContents.getStackInSlot(0);
        ItemStack itemstack1 = this.inputZoneContents.getStackInSlot(1);
        boolean flag1 = !itemstack.isEmpty() && !itemstack1.isEmpty();
        if (itemstack.isEmpty() || itemstack1.isEmpty()) {
            this.outputZoneContents.setInventorySlotContents(0, ItemStack.EMPTY);
        }
        else {
            int j = 1;
            int i;
            ItemStack itemstack2;
            if (flag1) {
                if (itemstack.getItem() != itemstack1.getItem()) {
                    this.outputZoneContents.setInventorySlotContents(0, ItemStack.EMPTY);
                    this.detectAndSendChanges();
                    return;
                }

                Item item = itemstack.getItem();
                int k = itemstack.getMaxDamage() - itemstack.getDamage();
                int l = itemstack.getMaxDamage() - itemstack1.getDamage();
                int i1 = k + l + itemstack.getMaxDamage() * 5 / 100;
                i = Math.max(itemstack.getMaxDamage() - i1, 0);
                itemstack2 = this.copyEnchantments(itemstack, itemstack1);
                if (!itemstack2.isRepairable()) i = itemstack.getDamage();
                if (!itemstack2.isDamageable() || !itemstack2.isRepairable()) {
                    if (!ItemStack.areItemStacksEqual(itemstack, itemstack1)) {
                        this.outputZoneContents.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.detectAndSendChanges();
                        return;
                    }

                    j = 2;
                }
            } else {
                boolean flag3 = !itemstack.isEmpty();
                i = flag3 ? itemstack.getDamage() : itemstack1.getDamage();
                itemstack2 = flag3 ? itemstack : itemstack1;
            }

            this.outputInventory.setInventorySlotContents(0, this.removeEnchantments(itemstack2, i, j));
        }

        this.detectAndSendChanges();
    }
     */
}
