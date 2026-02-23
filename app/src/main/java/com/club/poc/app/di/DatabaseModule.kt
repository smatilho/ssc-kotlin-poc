package com.club.poc.app.di

import android.content.Context
import androidx.room.Room
import com.club.poc.core.database.BedNightDao
import com.club.poc.core.database.BookingLifecycleDao
import com.club.poc.core.database.CLUB_POC_DATABASE_NAME
import com.club.poc.core.database.ClubConfigDao
import com.club.poc.core.database.ClubPocDatabase
import com.club.poc.core.database.DocumentDao
import com.club.poc.core.database.LodgeDao
import com.club.poc.core.database.MemberRoleDao
import com.club.poc.core.database.MembershipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ClubPocDatabase {
        return Room.databaseBuilder(
            context,
            ClubPocDatabase::class.java,
            CLUB_POC_DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideClubConfigDao(database: ClubPocDatabase): ClubConfigDao = database.clubConfigDao()

    @Provides
    fun provideMembershipDao(database: ClubPocDatabase): MembershipDao = database.membershipDao()

    @Provides
    fun provideMemberRoleDao(database: ClubPocDatabase): MemberRoleDao = database.memberRoleDao()

    @Provides
    fun provideLodgeDao(database: ClubPocDatabase): LodgeDao = database.lodgeDao()

    @Provides
    fun provideDocumentDao(database: ClubPocDatabase): DocumentDao = database.documentDao()

    @Provides
    fun provideBedNightDao(database: ClubPocDatabase): BedNightDao = database.bedNightDao()

    @Provides
    fun provideBookingLifecycleDao(database: ClubPocDatabase): BookingLifecycleDao = database.bookingLifecycleDao()
}
