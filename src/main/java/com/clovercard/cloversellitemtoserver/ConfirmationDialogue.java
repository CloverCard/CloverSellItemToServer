package com.clovercard.cloversellitemtoserver;

import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationDialogue {
    public static void sendConfirmation(ServerPlayerEntity player, String prompt, String type, Integer count) {
        List<Dialogue> dialogues = new ArrayList<>();
        dialogues.add(new Dialogue.DialogueBuilder()
                .setText(prompt)
                .addChoice(Choice.builder()
                        .setText("Yes")
                        .setHandle(e -> {
                            switch (type) {
                                case "all":
                                    SellToServerCommand.sellInventory(player);
                                    break;
                                case "count":
                                    SellToServerCommand.sellHeldItem(player, count);
                                    break;
                                default:
                                    SellToServerCommand.sellHeldItem(player);
                                    break;
                            }
                        })
                        .build()
                )
                .addChoice(Choice.builder()
                        .setText("No")
                        .build()
                )
                .build()
        );
        Dialogue.setPlayerDialogueData(player, dialogues, true);
    }
}
