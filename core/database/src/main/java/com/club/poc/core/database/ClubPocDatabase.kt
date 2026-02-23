package com.club.poc.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClubConfigEntity::class,
        MemberProfileEntity::class,
        MemberRoleEntity::class,
        LodgeEntity::class,
        DocumentEntity::class,
        BedNightEntity::class,
        BookingHoldEntity::class,
        BookingEntity::class,
        BookingNightEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class ClubPocDatabase : RoomDatabase() {
    abstract fun clubConfigDao(): ClubConfigDao
    abstract fun membershipDao(): MembershipDao
    abstract fun memberRoleDao(): MemberRoleDao
    abstract fun lodgeDao(): LodgeDao
    abstract fun documentDao(): DocumentDao
    abstract fun bedNightDao(): BedNightDao
    abstract fun bookingLifecycleDao(): BookingLifecycleDao
}
