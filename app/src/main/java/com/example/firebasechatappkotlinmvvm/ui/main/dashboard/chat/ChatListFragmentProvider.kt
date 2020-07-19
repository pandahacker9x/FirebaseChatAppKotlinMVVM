package com.example.firebasechatappkotlinmvvm.ui.main.dashboard.chat

import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * Created by Trung on 7/10/2020
 */
@Module
abstract class ChatListFragmentProvider {
    @ContributesAndroidInjector(modules = [ChatListFragmentModule::class])
    abstract fun provideDashboardFragmentFactory() : ChatListFragment
}