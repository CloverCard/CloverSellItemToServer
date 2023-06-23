package com.clovercard.cloversellitemtoserver;

import com.mojang.brigadier.CommandDispatcher;
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
                Commands.literal("csell").executes(cmd -> sellHeldItem(cmd.getSource()))
        );
    }
    private int sellHeldItem(CommandSource src) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        assert player != null;

        //Get Held Item
        ItemStack item = player.getItemInHand(Hand.MAIN_HAND);
        if(item == null) {
            return 1;
        }

        //Check if it exists within the shopkeeper json.
        BaseShopItem shopItem = ServerNPCRegistry.shopkeepers.getItem(item);
        if(shopItem == null) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return 1;
        }
        ShopItemWithVariation shopItemVar = new ShopItemWithVariation(new ShopItem(shopItem, 1 ,1, false));
        float cost = shopItemVar.getSellCost() * item.getCount();
        StorageProxy.getParty(player).setBalance(BigDecimal.valueOf(StorageProxy.getParty(player).getBalance().doubleValue()+cost));
        player.sendMessage(new StringTextComponent(cost + " has been added to your balance!"), Util.NIL_UUID);
        item.shrink(item.getCount());
        return 0;
    }
}
