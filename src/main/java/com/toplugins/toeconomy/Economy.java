package com.toplugins.toeconomy;

import com.toplugins.toeconomy.databases.Database;
import com.toplugins.toeconomy.databases.DatabaseWorker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Economy {

    private final Database db;
    private final DatabaseWorker dw;

    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Double> pending = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> loading = java.util.concurrent.ConcurrentHashMap.newKeySet();


    public Economy(Database db, DatabaseWorker dw) {
        this.db = db;
        this.dw = dw;
    }

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q", "QQ", "S", "SS"};

    public String getFormattedBalance(UUID uuid) {
        double balance = getBalance(uuid);
        return formatCompact(balance);
    }

    public String getFormattedCachedBalance(UUID uuid) {
        return formatCompact(getCachedBalance(uuid));
    }

    private static String formatCompact(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }

        boolean negative = value < 0;
        double abs = Math.abs(value);

        if (abs < 1000) {
            long v = (long) abs;
            return (negative ? "-" : "") + v;
        }

        int index = 0;
        while (abs >= 1000 && index < SUFFIXES.length - 1) {
            abs /= 1000.0;
            index++;
        }

        String number;
        if (abs >= 100 || abs == (long) abs) {
            number = String.valueOf((long) abs);
        } else {
            number = String.format(Locale.US, "%.1f", abs);
            if (number.endsWith(".0")) number = number.substring(0, number.length() - 2);
        }

        return (negative ? "-" : "") + number + SUFFIXES[index];
    }

    public double getCachedBalance(UUID uuid) {
        return cache.getOrDefault(uuid, 0.0);
    }

    public double getBalance(UUID uuid) {
        try {
            return dw.call(() -> {
                Double cached = cache.get(uuid);
                if (cached != null) return cached;

                double fromDb = db.getBalance(uuid.toString());
                cache.put(uuid, fromDb);
                return fromDb;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public void setBalance(UUID uuid, double value) {
        dw.run(() -> {
            try {
                db.setBalance(uuid.toString(), value);
                cache.put(uuid, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addBalance(UUID uuid, double delta) {
        dw.run(() -> {
            try {
                db.addBalance(uuid.toString(), delta);
                cache.merge(uuid, delta, Double::sum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addBalanceBuffered(UUID uuid, double delta) {
        pending.merge(uuid, delta, Double::sum);

        cache.merge(uuid, delta, Double::sum);
    }

    public boolean takeBalance(UUID uuid, double delta) throws SQLException {
        try {
            return dw.call(() -> {
                boolean ok = db.takeBalance(uuid.toString(), delta);
                if (ok) cache.merge(uuid, -delta, Double::sum);
                return ok;
            });
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    void flushPending() {
        if (pending.isEmpty()) return;

        Map<UUID, Double> snapshot = new HashMap<>();
        for (Map.Entry<UUID, Double> e : pending.entrySet()) {
            UUID uuid = e.getKey();
            Double val = pending.remove(uuid);
            if (val != null && val != 0.0) snapshot.put(uuid, val);
        }

        for (Map.Entry<UUID, Double> entry : snapshot.entrySet()) {
            try {
                db.addBalance(entry.getKey().toString(), entry.getValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                pending.merge(entry.getKey(), entry.getValue(), Double::sum); // requeue
            }
        }
    }

    public void warmup(UUID uuid) {
        if (cache.containsKey(uuid)) return;
        if (!loading.add(uuid)) return;

        dw.run(() -> {
            try {
                double fromDb = db.getBalance(uuid.toString());
                cache.put(uuid, fromDb);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                loading.remove(uuid);
            }
        });
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }
}
