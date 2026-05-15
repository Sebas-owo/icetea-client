package exiu.iceteaclient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import exiu.iceteaclient.features.DrillSwap;

public class Commands {
    
    public void register() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(literal("icetea")
                .then(literal("drillswap")
                    .executes(context -> {
                        boolean enabled = !DrillSwap.enabled;
                        DrillSwap.enabled = enabled;
                        context.getSource()
                            .sendFeedback(Text.literal("Drillswap is now " + (enabled ? "enabled" : "disabled") + "!")
                            .formatted(enabled ? Formatting.GREEN : Formatting.RED));
                        return 1;
                    })
                )
            );

        });

    }

}
