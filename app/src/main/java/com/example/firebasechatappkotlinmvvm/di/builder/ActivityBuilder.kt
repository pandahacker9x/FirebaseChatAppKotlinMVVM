package com.example.firebasechatappkotlinmvvm.di.builder

import com.example.firebasechatappkotlinmvvm.ui.auth.AuthActivity
import com.example.firebasechatappkotlinmvvm.ui.auth.AuthActivityModule
import com.example.firebasechatappkotlinmvvm.ui.auth.login.LoginFragmentProvider
import com.example.firebasechatappkotlinmvvm.ui.auth.login.LoginFragmentModule
import com.example.firebasechatappkotlinmvvm.ui.main.MainActivity
import com.example.firebasechatappkotlinmvvm.ui.main.MainActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * Created by Trung on 7/10/2020
 */
@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [AuthActivityModule::class, LoginFragmentProvider::class])
    abstract fun bindAuthActivity(): AuthActivity
}