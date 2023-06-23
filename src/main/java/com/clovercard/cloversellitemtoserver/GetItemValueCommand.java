package com.clovercard.cloversellitemtoserver;

import com.mojang.brigadier.CommandDispatcher;
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

public class GetItemValueCommand {
    public GetItemValueCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("cvalue").executes(cmd -> valueHeldItem(cmd.getSource()))
        );
    }
    private int valueHeldItem(CommandSource src) {
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

        //Get cost data
        float unitCost = shopItemVar.getSellCost();
        float totalCost = shopItemVar.getSellCost() * item.getCount();
        if(unitCost <= 0 || totalCost <= 0) {
            player.sendMessage(new StringTextComponent("This item does not have a listed price to sell!"), Util.NIL_UUID);
            return 1;
        }
        player.sendMessage(new StringTextComponent("Sell Price: " + totalCost + " (" + unitCost + " per single unit)"), Util.NIL_UUID);
        return 0;
    }
}
