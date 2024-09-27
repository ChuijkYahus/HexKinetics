package net.sonunte.hexkinetics.forge;


import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.sonunte.hexkinetics.api.config.HexKineticsConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static at.petrak.hexcasting.api.mod.HexConfig.noneMatch;

public class ForgeHexKineticsConfig implements HexKineticsConfig.CommonConfigAccess {
    public ForgeHexKineticsConfig(ForgeConfigSpec.Builder builder) {

    }

    public static class Client implements HexKineticsConfig.ClientConfigAccess {
        public Client(ForgeConfigSpec.Builder builder) {

        }
    }

    public static class Server implements HexKineticsConfig.ServerConfigAccess {
        // costs of example spells
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> translocationDenyList;
        private static ForgeConfigSpec.DoubleValue exampleConstActionCost;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.translation("text.autoconfig.hexkinetics.option.server.translocation").push("translocation");

            translocationDenyList = builder
                    .translation("text.autoconfig.hexkinetics.option.server.translocation.translocationDenyList")
                    .defineList("translocationDenyList",
                            HexKineticsConfig.ServerConfigAccess.Companion.getDEFAULT_TRANSLOCATION_DENY_LIST(),
                            ForgeHexKineticsConfig.Server::isValidReslocArg);

            builder.pop();
        }


        private static boolean isValidReslocArg(Object o) {
            return o instanceof String s && ResourceLocation.isValidResourceLocation(s);
        }

        //region getters

        @Override
        public int getExampleConstActionCost() {
            return (int) (exampleConstActionCost.get() * MediaConstants.DUST_UNIT);
        }

        @Override
        public boolean getMoveTileEntities() {
            return false;
        }

        @Override
        public boolean isTranslocationAllowed(@NotNull ResourceLocation blockId) {
            return noneMatch(translocationDenyList.get(), blockId);
        }

        //endregion
    }
}
