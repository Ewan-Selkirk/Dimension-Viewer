package dev.stick_stack.dimensionviewer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.stick_stack.dimensionviewer.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public class CustomCommands {

    public static void RegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dimensionviewer")
                .then(Commands.literal("get")
                        .executes(CustomCommands::getDimensionId)
                        .then(Commands.literal("color")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            String dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();
                                            String value = Services.CONFIG.GetCustomColor(dim);

                                            if (value != null) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("Color for dimension %s: '%s'".formatted(dim, value)), true);
                                                return 1;
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal("Failed to get color of dimension: %s".formatted(value)));
                                                return 0;
                                            }
                                        })
                                )
                        )
                        .then(Commands.literal("alias")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            String dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();

                                            String value = Services.CONFIG.GetAlias(dim);
                                            if (Services.CONFIG.HasAlias(dim)) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("The alias for dimension %s is '%s'".formatted(dim, value)), true);
                                            } else {
                                                ctx.getSource().sendSuccess(() -> Component.literal("There is no alias set for dimension '%s'".formatted(dim)), true);
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("format")
                                .executes(ctx -> {
                                    ctx.getSource().sendSuccess(() -> Component.literal("Current dimension format: %s".formatted(Services.CONFIG.ListFormat())), true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("set")
                        .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.literal("color")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("custom_color", ColorArgument.color())
                                                .executes(ctx -> {
                                                    var dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();
                                                    var color = ColorArgument.getColor(ctx, "custom_color").getSerializedName().toUpperCase(Locale.ROOT);

                                                    Services.CONFIG.SetColor(dim, color);
                                                    refreshDisplayNames(ctx);

                                                    ctx.getSource().sendSuccess(() -> Component.literal("Set color of dimension %s to '%s'".formatted(dim, color)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("alias")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("alias", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    var dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();
                                                    var alias = StringArgumentType.getString(ctx, "alias");

                                                    Services.CONFIG.SetAlias(dim, alias);
                                                    refreshDisplayNames(ctx);

                                                    ctx.getSource().sendSuccess(() -> Component.literal("Set alias of dimension %s to '%s'".formatted(dim, alias)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("format")
                                .then(Commands.argument("format", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            var format = StringArgumentType.getString(ctx, "format");
                                            Services.CONFIG.SetFormat(format);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Changed format to '%s'".formatted(format)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("boolean")
                                .then(Commands.literal("per_dim_color")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> setBoolSetting(ctx, "perDimColor"))
                                        )
                                )
                                .then(Commands.literal("dim_in_chat_name")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> setBoolSetting(ctx, "dimInChatName"))
                                        )
                                )
                                .then(Commands.literal("chat_dim_hover")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> setBoolSetting(ctx, "chatDimHover"))
                                        )
                                )
                                .then(Commands.literal("enable_aliases")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> setBoolSetting(ctx, "enableAliases"))
                                        )
                                )
                        )
                        .then(Commands.literal("position")
                                .then(Commands.literal("prepend")
                                        .executes(ctx -> {
                                            Services.CONFIG.SetPlacement(CommonUtils.DimensionPosition.PREPEND);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Set dimension position to 'PREPEND'"), true);
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("append")
                                        .executes(ctx -> {
                                            Services.CONFIG.SetPlacement(CommonUtils.DimensionPosition.APPEND);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Set dimension position to 'APPEND'"), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("refresh")
                        .requires(src -> src.hasPermission(Commands.LEVEL_MODERATORS))
                        .executes(ctx -> refreshDisplayNames(ctx, true)))
                .then(Commands.literal("reset")
                        .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.literal("format")
                                .executes(ctx -> {
                                    Services.CONFIG.SetFormat(Services.CONFIG.BaseListFormat());
                                    refreshDisplayNames(ctx);

                                    ctx.getSource().sendSuccess(() -> Component.literal("Reset format back to default! (%s)".formatted(Services.CONFIG.BaseListFormat())), true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("color")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            var dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();

                                            Services.CONFIG.ResetColor(dim);
                                            refreshDisplayNames(ctx);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Reset color for dimension: %s".formatted(dim)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("alias")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            var dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();
                                            Services.CONFIG.ResetAlias(dim);
                                            refreshDisplayNames(ctx);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Reset alias for dimension: %s".formatted(dim)), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("custom_colors")
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    MutableComponent response = Component.empty();

                                    if (Services.CONFIG.GetAllCustomColors().size() == 0) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("No custom colors have been registered."), true);
                                        return 1;
                                    }

                                    response.append(Component.literal("Custom colors registered: "));

                                    int i = 0;
                                    for (var color : Services.CONFIG.GetAllCustomColors()) {
                                        var name = color.split(" ", 2)[0];
                                        var value = color.split(" ", 2)[1];

                                        response.append(Component.literal("%s (%s)".formatted(name, value)).withColor(CommonUtils.customColorToInt(color.split(" "))));
                                        if (i < Services.CONFIG.GetAllCustomColors().size() - 1) {
                                            response.append(", ");
                                        }

                                        i++;
                                    }

                                    ctx.getSource().sendSuccess(() -> response, true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("add")
                                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("color", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    var name = StringArgumentType.getString(ctx, "name");
                                                    var color = StringArgumentType.getString(ctx, "color");

                                                    if (Services.CONFIG.GetAllCustomColors().stream().anyMatch(c -> c.split(" ", 2)[0].equals(name))) {
                                                        ctx.getSource().sendFailure(Component.literal("Failed to add custom color. Color with name '%s' already exists!".formatted(name)));
                                                        return 0;
                                                    }

                                                    if (color.matches("(?i)#[0-9a-f]{6}") || color.matches("r[0-9]{1,3} g[0-9]{1,3} b[0-9]{1,3}")) {
                                                        Services.CONFIG.AddCustomColor(name.toUpperCase(Locale.ROOT), color);
                                                        ctx.getSource().sendSuccess(() -> Component.literal("Successfully added new custom color!"), true);
                                                        return 1;
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal("Failed to add custom color!\nColor needs to be in the form of '#123456' or 'r1 g23 b456'"));
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> {
                                            var color = StringArgumentType.getString(ctx, "name");
                                            if (Services.CONFIG.RemoveCustomColor(color)) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("Successfully removed color '%s'".formatted(color)), true);
                                                return 1;
                                            } else {
                                                ctx.getSource().sendFailure(Component.literal("Could not find custom color '%s' to remove".formatted(color)));
                                                return 0;
                                            }
                                        })
                                )
                        )
                        .then(Commands.literal("set")
                                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("custom_color", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    var dim = DimensionArgument.getDimension(ctx, "dimension").dimension().location().toString();
                                                    var color = StringArgumentType.getString(ctx, "custom_color").toUpperCase();

                                                    if (Services.CONFIG.GetAllCustomColors().stream().anyMatch(c -> c.split(" ", 2)[0].equals(color))) {
                                                        Services.CONFIG.SetColor(dim, color);
                                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                                "Successfully set dimension '%s' color to '%s'"
                                                                        .formatted(dim, color)
                                                        ), true);

                                                        return 1;
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal("Failed to find custom color with name '%s'".formatted(color)));
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                        )
                )
        );
    }

    private static int setBoolSetting(CommandContext<CommandSourceStack> ctx, String perDimColor) {
        var value = BoolArgumentType.getBool(ctx, "value");

        Services.CONFIG.SetBoolSetting(perDimColor, value);
        refreshDisplayNames(ctx);
        ctx.getSource().sendSuccess(() -> Component.literal("Set option %s to '%s'".formatted(perDimColor, value)), true);
        return 1;
    }

    private static int getDimensionId(CommandContext<CommandSourceStack> context) {
        try {
            var dim = context.getSource().getPlayerOrException().level().dimension();
            context.getSource().sendSuccess(() -> Component.literal(dim.location() + ": " + CommonUtils.dimensionToString(dim.location())), true);

            return Command.SINGLE_SUCCESS;
        } catch (NullPointerException | CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("Failed to find player!"));

            return -1;
        }
    }

    private static int refreshDisplayNames(CommandContext<CommandSourceStack> context, boolean manuallyCalled) {
        Services.CONFIG.RefreshPlayerData(context.getSource().getServer().getPlayerList());

        if (manuallyCalled) {
            context.getSource().sendSuccess(() -> Component.literal("Refreshing Dimension Viewer details!"), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void refreshDisplayNames(CommandContext<CommandSourceStack> context) {
        refreshDisplayNames(context, false);
    }
}
