package com.basketball;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Partidos 1 contra 1: /challenger <jugador>, /challenger accept, /score. */
public final class MatchManager {
    public static final int TARGET = 21;

    public static final class Match {
        public final UUID a, b;
        public final String an, bn;
        public int sa, sb;
        Match(ServerPlayer x, ServerPlayer y) {
            a = x.getUUID(); b = y.getUUID();
            an = x.getGameProfile().getName(); bn = y.getGameProfile().getName();
        }
        public String line() { return an + " " + sa + " - " + sb + " " + bn; }
    }
    private record Pending(UUID from, long until) {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, Match> MATCHES = new HashMap<>();

    public static Match of(Player p) { return MATCHES.get(p.getUUID()); }
    private static void say(ServerPlayer p, String s, ChatFormatting c) { p.sendSystemMessage(Component.literal(s).withStyle(c)); }

    public static int challenge(ServerPlayer from, ServerPlayer to) {
        if (from == to) { say(from, "No puedes retarte a ti mismo", ChatFormatting.RED); return 0; }
        if (of(from) != null || of(to) != null) { say(from, "Ya hay un partido en curso (/challenger end)", ChatFormatting.RED); return 0; }
        PENDING.put(to.getUUID(), new Pending(from.getUUID(), from.level().getGameTime() + 1200));
        say(from, "Reto enviado a " + to.getGameProfile().getName(), ChatFormatting.GRAY);
        say(to, from.getGameProfile().getName() + " te reta a un partido a " + TARGET + ". Escribe /challenger accept", ChatFormatting.GOLD);
        return 1;
    }

    public static int accept(ServerPlayer me) {
        Pending pd = PENDING.remove(me.getUUID());
        if (pd == null || me.level().getGameTime() > pd.until()) { say(me, "No tienes retos pendientes", ChatFormatting.RED); return 0; }
        ServerPlayer from = me.getServer().getPlayerList().getPlayer(pd.from());
        if (from == null || of(from) != null || of(me) != null) { say(me, "El reto ya no es valido", ChatFormatting.RED); return 0; }
        Match m = new Match(from, me);
        MATCHES.put(m.a, m); MATCHES.put(m.b, m);
        broadcast(me, m, "¡Partido iniciado! " + m.line() + " (a " + TARGET + "). Usa /score", ChatFormatting.GREEN);
        return 1;
    }

    public static int end(ServerPlayer p) {
        Match m = of(p);
        if (m == null) { say(p, "No estas en un partido", ChatFormatting.RED); return 0; }
        MATCHES.remove(m.a); MATCHES.remove(m.b);
        broadcast(p, m, "Partido terminado: " + m.line(), ChatFormatting.YELLOW);
        return 1;
    }

    public static void leave(ServerPlayer p) {
        PENDING.remove(p.getUUID());
        Match m = of(p);
        if (m != null) {
            MATCHES.remove(m.a); MATCHES.remove(m.b);
            broadcast(p, m, p.getGameProfile().getName() + " salio. Partido cancelado.", ChatFormatting.YELLOW);
        }
    }

    /** Suma puntos al partido del jugador. Devuelve el marcador (o null si no esta en partido). */
    public static String onScore(ServerPlayer p, int pts) {
        Match m = of(p);
        if (m == null) return null;
        if (p.getUUID().equals(m.a)) m.sa += pts; else m.sb += pts;
        String line = m.line();
        if (m.sa >= TARGET || m.sb >= TARGET) {
            MATCHES.remove(m.a); MATCHES.remove(m.b);
            String w = m.sa >= TARGET ? m.an : m.bn;
            broadcast(p, m, "¡" + w + " gana el partido! " + line, ChatFormatting.GOLD);
            return line + " FIN";
        }
        broadcast(p, m, "Marcador: " + line, ChatFormatting.AQUA);
        return line;
    }

    private static void broadcast(ServerPlayer any, Match m, String text, ChatFormatting c) {
        for (UUID id : new UUID[] { m.a, m.b }) {
            ServerPlayer sp = any.getServer().getPlayerList().getPlayer(id);
            if (sp != null) say(sp, text, c);
        }
    }
}
