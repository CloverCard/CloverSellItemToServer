package com.clovercard.cloversellitemtoserver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import com.pixelmonmod.pixelmon.entities.npcs.registry.BaseShopItem;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ServerNPCRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ShopItem;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ShopItemWithVariation;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

import java.util.concurrent.atomic.AtomicReference;

public class SellToServerCommand {
    public SellToServerCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("csell")
                        .then(Commands.literal("all")
                                .executes(cmd -> sellCommand(cmd.getSource(), "all", 0))
                        )
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .executes(cmd -> sellCommand(cmd.getSource(), "count", IntegerArgumentType.getInteger(cmd, "count")))
                        )
                        .executes(cmd -> sellCommand(cmd.getSource(), "base", 0))
        );
    }
    private int sellCommand(CommandSource src, String type, int count) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        assert player != null;
        if(type.equals("all")) {
            ConfirmationDialogue.sendConfirmation(player, "Are you sure you want to sell all the sellable items in your inventory?", type, count);
        } else if (type.equals("count")) {
            ConfirmationDialogue.sendConfirmation(player, "Are you sure you want to sell " + count + " of this item?", type, count);
        }
        else {
            ConfirmationDialogue.sendConfirmation(player, "Are you sure you want to sell all of this item?", "base", count);
        }
        return 0;
    }
    public static void sellHeldItem(ServerPlayerEntity player) {
        if(player == null) return;

        //Get Held Item
        ItemStack item = player.getItemInHand(Hand.MAIN_HAND);

        //Check if it exists within the shopkeeper json.
        BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
        if(shopItem == null) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return;
        }
        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
        //Get Cost Data
        float cost = shopItemVar.getSellCost() * item.getCount();
        if(cost <= 0) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return;
        }

        //Sell Item
        BankAccount account = BankAccountProxy.getBankAccount(player).orElse(null);
        if(account == null) {
            player.sendMessage(new StringTextComponent("Could not found account associated with you!"), Util.NIL_UUID);
            return;
        }
        account.add(cost);
        player.sendMessage(new StringTextComponent(cost + " has been added to your balance!"), Util.NIL_UUID);
        item.shrink(item.getCount());
    }
    public static void sellHeldItem(ServerPlayerEntity player, int count) {
        if(player == null) return;

        //Get Held Item
        ItemStack item = player.getItemInHand(Hand.MAIN_HAND);

        //Check if it exists within the shopkeeper json.
        BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
        if(shopItem == null) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return;
        }
        if(item.getCount() < count) {
            player.sendMessage(new StringTextComponent("You do not have " + count + " of this item!"), Util.NIL_UUID);
            return;
        }
        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
        //Get Cost Data
        float cost = shopItemVar.getSellCost() * count;
        if(cost <= 0) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return;
        }

        //Sell Item
        BankAccount account = BankAccountProxy.getBankAccount(player).orElse(null);
        if(account == null) {
            player.sendMessage(new StringTextComponent("Could not found account associated with you!"), Util.NIL_UUID);
            return;
        }
        account.add(cost);
        player.sendMessage(new StringTextComponent(cost + " has been added to your balance!"), Util.NIL_UUID);
        item.shrink(count);
    }
    public static void sellInventory(ServerPlayerEntity player) {
        if(player == null) return;
        BankAccount account = BankAccountProxy.getBankAccount(player).orElse(null);
        if(account == null) {
            player.sendMessage(new StringTextComponent("Could not found account associated with you!"), Util.NIL_UUID);
            return;
        }
        AtomicReference<Float> sum = new AtomicReference<>((float) 0);
        player.inventory.items.forEach(
                item -> {
                    //Check if it exists within the shopkeeper json.
                    BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
                    if(shopItem != null) {
                        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
                        //Get Cost Data
                        float cost = shopItemVar.getSellCost() * item.getCount();
                        if(cost > 0) {
                            //Sell Item
                            account.add(cost);
                            item.shrink(item.getCount());
                        }
                        sum.updateAndGet(v -> v + cost);
                    }
                }
        );
        player.sendMessage(new StringTextComponent("All sellable items have been sold! " + sum.get() + " earned!"), Util.NIL_UUID);
    }
}
