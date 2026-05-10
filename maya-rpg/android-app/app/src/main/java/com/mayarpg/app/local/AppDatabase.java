package com.mayarpg.app.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mayarpg.app.local.dao.CacheJsonDao;
import com.mayarpg.app.local.dao.DashboardDao;
import com.mayarpg.app.local.entities.CacheJson;
import com.mayarpg.app.local.entities.DashboardCache;

@Database(
    entities = { DashboardCache.class, CacheJson.class },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract DashboardDao dashboardDao();
    public abstract CacheJsonDao cacheJsonDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "maya_rpg_local.db"
                    )
                    .allowMainThreadQueries()
                    // Se a versao mudar e nao tiver migration, recria o banco do zero
                    // (cache e descartavel, sem perda de dados importantes do usuario)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
}
