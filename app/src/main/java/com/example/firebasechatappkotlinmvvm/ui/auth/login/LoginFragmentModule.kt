package com.example.firebasechatappkotlinmvvm.ui.auth.login

import com.example.firebasechatappkotlinmvvm.data.repo.user.UserRepo
import com.example.firebasechatappkotlinmvvm.ui.auth.sign_up.SignUpViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Provider


/**
 * Created by Trung on 7/10/2020
 */
@Module
class LoginFragmentModule {
    @Provides
    fun provideLoginViewModel(userRepo: UserRepo) = LoginViewModel(userRepo)

    @Provides
    fun provideFactory(provider: Provider<LoginViewModel>)
        = LoginViewModel.Factory(provider)
}