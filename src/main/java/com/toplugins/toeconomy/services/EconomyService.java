package com.toplugins.toeconomy.services;

import com.toplugins.toeconomy.databases.DatabaseWorker;
import com.toplugins.toeconomy.databases.SQLiteDb;

import java.sql.SQLException;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EconomyService {

    private final SQLiteDb db;
    private final DatabaseWorker dw;

    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();
    public EconomyService(SQLiteDb db, DatabaseWorker dw) {
        this.db = db;
        this.dw = dw;
    }

    public double getCachedBalance(UUID uuid) {
        return cache.getOrDefault(uuid, 0.0);
    }

    public double getBalance(UUID uuid) throws SQLException {
        try {
            return dw.call(() -> {
                Double cached = cache.get(uuid);
                if (cached != null) return cached;

                double fromDb = db.getBalance(uuid.toString());
                cache.put(uuid, fromDb);
                return fromDb;
            });
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public void setBalance(UUID uuid, double value) throws SQLException {
        dw.run(()->{
            try {
                db.setBalance(uuid.toString(), value);
                cache.put(uuid, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void addBalance(UUID uuid, double delta) throws SQLException {
        dw.run(()->{
            try {
                db.addBalance(uuid.toString(), delta);
                cache.merge(uuid, delta, Double::sum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean takeBalance(UUID uuid, double delta) throws SQLException {
        try {
            return dw.call(()->{
                boolean ok = db.takeBalance(uuid.toString(), delta);
                if (ok) cache.merge(uuid, -delta, Double::sum);
                return ok;
            });
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }
}
