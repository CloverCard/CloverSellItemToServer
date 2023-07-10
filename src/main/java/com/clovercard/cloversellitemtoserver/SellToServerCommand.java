package com.clovercard.cloversellitemtoserver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
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

import java.math.BigDecimal;

public class SellToServerCommand {
    public SellToServerCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("csell")
                        .then(Commands.literal("all")
                                .executes(cmd -> sellInventory(cmd.getSource()))
                        )
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .executes(cmd -> sellHeldItem(cmd.getSource(), IntegerArgumentType.getInteger(cmd, "count")))
                        )
                        .executes(cmd -> sellHeldItem(cmd.getSource()))
        );
    }
    private int sellHeldItem(CommandSource src) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        assert player != null;

        //Get Held Item
        ItemStack item = player.getItemInHand(Hand.MAIN_HAND);

        //Check if it exists within the shopkeeper json.
        BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
        if(shopItem == null) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return 1;
        }
        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
        //Get Cost Data
        float cost = shopItemVar.getSellCost() * item.getCount();
        if(cost <= 0) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return 1;
        }

        //Sell Item
        StorageProxy.getParty(player).setBalance(BigDecimal.valueOf(StorageProxy.getParty(player).getBalance().doubleValue()+cost));
        player.sendMessage(new StringTextComponent(cost + " has been added to your balance!"), Util.NIL_UUID);
        item.shrink(item.getCount());
        return 0;
    }
    private int sellHeldItem(CommandSource src, int count) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        assert player != null;

        //Get Held Item
        ItemStack item = player.getItemInHand(Hand.MAIN_HAND);

        //Check if it exists within the shopkeeper json.
        BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
        if(shopItem == null) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return 1;
        }
        if(item.getCount() < count) {
            player.sendMessage(new StringTextComponent("You do not have " + count + " of this item!"), Util.NIL_UUID);
            return 1;
        }
        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
        //Get Cost Data
        float cost = shopItemVar.getSellCost() * count;
        if(cost <= 0) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return 1;
        }

        //Sell Item
        StorageProxy.getParty(player).setBalance(BigDecimal.valueOf(StorageProxy.getParty(player).getBalance().doubleValue()+cost));
        player.sendMessage(new StringTextComponent(cost + " has been added to your balance!"), Util.NIL_UUID);
        item.shrink(count);
        return 0;
    }
    private int sellInventory(CommandSource src) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        assert player != null;

        player.inventory.items.stream().forEach(
                item -> {
                    //Check if it exists within the shopkeeper json.
                    BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
                    if(shopItem != null) {
                        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
                        //Get Cost Data
                        float cost = shopItemVar.getSellCost() * item.getCount();
                        if(cost > 0) {
                            //Sell Item
                            StorageProxy.getParty(player).setBalance(BigDecimal.valueOf(StorageProxy.getParty(player).getBalance().doubleValue()+cost));
                            item.shrink(item.getCount());
                        }
                    }
                }
        );
        player.sendMessage(new StringTextComponent("All sellable items have been sold!"), Util.NIL_UUID);
        return 0;
    }
}
