package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountType
import com.example.data.model.BusinessProfile
import com.example.data.model.CashEntry
import com.example.data.model.CashMemo
import com.example.data.model.ContactAccount
import com.example.data.model.KhataTransaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = enumValueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = enumValueOf(value)
}

@Database(
    entities = [
        ContactAccount::class,
        KhataTransaction::class,
        CashEntry::class,
        CashMemo::class,
        BusinessProfile::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tallyDao(): TallyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tallykhata_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.tallyDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: TallyDao) {
            // Clean initial Business Profile (Empty ledgers, clean real business state without demo dummy records)
            dao.insertOrUpdateProfile(
                BusinessProfile(
                    id = 1,
                    shopName = "আমার ব্যবসা প্রতিষ্ঠান",
                    ownerName = "দোকানের মালিক",
                    phone = "",
                    address = "বাংলাদেশ",
                    currencySymbol = "৳",
                    language = "bn"
                )
            )
        }
    }
}
