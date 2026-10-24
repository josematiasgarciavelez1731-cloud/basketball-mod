package com.basketball;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.EntityArgument;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class BasketCommands {
    private static int shoes(CommandContext<CommandSourceStack> c) {
        StringBuilder sb = new StringBuilder("ZAPATILLAS (se fabrican con monedas o se compran a aldeanos)\n");
        for (ShoeTier t : ShoeTier.values())
            sb.append(t.label).append(": vel +").append(Math.round(t.speed * 100)).append("%")
              .append(t.jump >= 0 ? ", salto +" + (t.jump + 1) : "")
              .append(", punteria +").append(Math.round(t.accuracy * 100)).append("%")
              .append(", manejo +").append(Math.round(t.handling * 100)).append("%")
              .append(" | aldeano: ").append(t.price).append(" monedas\n");
        c.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
    private static int courtGuide(CommandContext<CommandSourceStack> c) {
        String g = "COMO CONSTRUIR UNA CANCHA (15 x 29 bloques)\n"
            + "1) Suelo: 15 de ancho x 29 de largo con bloques de cancha. Tipos: parquet (madera), cemento, asfalto o goma. Rebotan distinto: parquet > goma > cemento > asfalto. En tierra o cesped el balon casi no bota.\n"
            + "2) Lineas (bloque Linea de cancha): borde exterior, linea de media cancha y circulo central de radio 2.\n"
            + "3) En cada extremo: pintura de 5 de ancho x 6 de largo y linea de triple en arco de radio 7 desde el aro.\n"
            + "4) Aros: un bloque detras de cada linea de fondo pon un poste de 4 bloques de alto. El aro va pegado al poste, en el bloque de mas arriba, mirando a la cancha.\n"
            + "5) Opcional: pantalla de marcador junto al aro.\n"
            + "Atajo (op): /basket court build <parquet|cemento|asfalto|goma>";
        c.getSource().sendSuccess(() -> Component.literal(g), false);
        return 1;
    }
    private static int build(CommandContext<CommandSourceStack> c, String tipo) throws CommandSyntaxException {
        var sup = CourtBuilder.TYPES.get(tipo.toLowerCase());
        if (sup == null) { c.getSource().sendFailure(Component.literal("Tipos: parquet, cemento, asfalto, goma")); return 0; }
        ServerPlayer p = c.getSource().getPlayerOrException();
        CourtBuilder.build(p.serverLevel(), p.blockPosition().below(), sup.get());
        p.addItem(new ItemStack(BasketballMod.BALL.get()));
        c.getSource().sendSuccess(() -> Component.literal("Cancha de " + tipo + " creada con 2 aros. ¡Te di un balon!"), true);
        return 1;
    }

    private static int showScore(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        MatchManager.Match m = MatchManager.of(p);
        String s = m == null ? "No estas en un partido. Usa /challenger <jugador>" : m.line() + "  (a " + MatchManager.TARGET + ")";
        c.getSource().sendSuccess(() -> Component.literal(s), false);
        return 1;
    }
    private static void registerMatch(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("score").executes(BasketCommands::showScore)
            .then(Commands.literal("partido").executes(BasketCommands::showScore)));
        d.register(Commands.literal("challenger")
            .then(Commands.literal("accept").executes(c -> MatchManager.accept(c.getSource().getPlayerOrException())))
            .then(Commands.literal("end").executes(c -> MatchManager.end(c.getSource().getPlayerOrException())))
            .then(Commands.argument("player", EntityArgument.player()).executes(c ->
                MatchManager.challenge(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "player")))));
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        registerMatch(d);
        d.register(Commands.literal("basket")
            .then(Commands.literal("points").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                c.getSource().sendSuccess(() -> Component.literal("Puntos: " + PlayerData.points(p)), false);
                return 1; }))
            .then(Commands.literal("moves").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                StringBuilder sb = new StringBuilder("Puntos: " + PlayerData.points(p) + "\n");
                for (Move m : Move.values())
                    sb.append(PlayerData.has(p, m) ? "[x] " : "[ ] ").append(m.name().toLowerCase()).append(" - ")
                      .append(m.label).append(" (").append(m.cost).append(" pts)").append(m.active() ? "" : " [pasivo]").append("\n");
                c.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                return 1; }))
            .then(Commands.literal("unlock").then(Commands.argument("move", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Move.values()).map(m -> m.name().toLowerCase()), b))
                .executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    String a = StringArgumentType.getString(c, "move");
                    Move m = Arrays.stream(Move.values()).filter(x -> x.name().equalsIgnoreCase(a)).findFirst().orElse(null);
                    if (m == null) { c.getSource().sendFailure(Component.literal("Movimiento no existe")); return 0; }
                    if (PlayerData.has(p, m)) { c.getSource().sendFailure(Component.literal("Ya lo tienes")); return 0; }
                    if (PlayerData.points(p) < m.cost) { c.getSource().sendFailure(Component.literal("Necesitas " + m.cost + " puntos")); return 0; }
                    PlayerData.addPoints(p, -m.cost);
                    PlayerData.unlock(p, m);
                    c.getSource().sendSuccess(() -> Component.literal("¡Desbloqueado: " + m.label + "!"), false);
                    return 1; })))
            .then(Commands.literal("addpoints").requires(s -> s.hasPermission(2))
                .then(Commands.argument("n", IntegerArgumentType.integer(1, 1000)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerData.addPoints(p, IntegerArgumentType.getInteger(c, "n"));
                    return 1; })))
            .then(Commands.literal("shoes").executes(BasketCommands::shoes))
            .then(Commands.literal("court").executes(BasketCommands::courtGuide)
                .then(Commands.literal("build").requires(s -> s.hasPermission(2))
                    .executes(c -> build(c, "parquet"))
                    .then(Commands.argument("tipo", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(CourtBuilder.TYPES.keySet(), b))
                        .executes(c -> build(c, StringArgumentType.getString(c, "tipo")))))));
    }
}
