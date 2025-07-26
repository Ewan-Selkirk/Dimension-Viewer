package dev.stick_stack.dimensionviewer.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.stick_stack.dimensionviewer.CustomCommands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ArgumentTypeInfos.class)
public abstract class ArgumentTypeInfosMixin {

    @Shadow
    protected static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> p_235387_, String p_235388_, Class<? extends A> p_235389_, ArgumentTypeInfo<A, T> p_235390_) {
        return null;
    }

    @Inject(method = "bootstrap", at = @At(value = "TAIL"))
    private static void registerNewTypes(Registry<ArgumentTypeInfo<?, ?>> pRegistry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> cir) {
        register(pRegistry, "custom_color", CustomCommands.CustomColorArgument.class, SingletonArgumentInfo.contextFree(CustomCommands.CustomColorArgument::color));
    }

}
