package com.example.firebasechatappkotlinmvvm.ui.main.dashboard.profile

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.firebasechatappkotlinmvvm.BR
import com.example.firebasechatappkotlinmvvm.R
import com.example.firebasechatappkotlinmvvm.data.callback.SingleCallBack
import com.example.firebasechatappkotlinmvvm.databinding.FragmentChatListBinding
import com.example.firebasechatappkotlinmvvm.ui.auth.AuthActivity
import com.example.firebasechatappkotlinmvvm.ui.base.BaseFragment
import com.example.firebasechatappkotlinmvvm.ui.base.OnItemWithPositionClickListener
import com.example.firebasechatappkotlinmvvm.ui.base.OptionBottomSheetDialogFragment
import com.example.firebasechatappkotlinmvvm.util.AppConstants
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : BaseFragment<FragmentChatListBinding, ProfileViewModel>() {

    @Inject
    lateinit var mFactory: ProfileViewModel.Factory

    override fun getLayoutResId(): Int {
        return R.layout.fragment_profile
    }

    override fun getVMBindingVarId(): Int {
        return BR.viewModel;
    }

    override fun getVM(): ProfileViewModel {
        return ViewModelProviders
            .of(this, mFactory)[ProfileViewModel::class.java]
    }

    override fun setupViews() {
        setupAvatarOption()
        setupProfileOptionRecyclerView()
        vm.loadUserProfile()
    }

    private fun setupAvatarOption() {
        mImgAvatar.setOnClickListener {
            OptionBottomSheetDialogFragment(
                AppConstants.STR_IDS_AVATAR_OPTION,
                object : OnItemWithPositionClickListener {
                    override fun onItemWithPositionClicked(position: Int) {
                        when (position) {
                            1 -> uploadAvatar()
                        }
                    }
                }
            ).show(childFragmentManager, "")
        }
    }

    val selectMediaImgUriCallBack : SingleCallBack<Uri>
        = object : SingleCallBack<Uri> {
        override fun onSuccess(avatarUri: Uri) {
            vm.uploadAvatar(openInputStream(avatarUri))
            mImgAvatar.setImageURI(avatarUri)
        }
    }

    private fun uploadAvatar() {
        selectMediaImage(selectMediaImgUriCallBack)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_avatar_profile, menu)
    }

    private val mOnItemProfileOptionClick: OnItemWithPositionClickListener =
        object : OnItemWithPositionClickListener {
            override fun onItemWithPositionClicked(position: Int) {
                showExitConfirmDialog()
            }
        }

    private fun showExitConfirmDialog() {
        showConfirmDialog(R.string.are_you_sure,
            DialogInterface.OnClickListener { dialog, which ->
                vm.signOut()
                AuthActivity.open(requireContext())
                finishActivity()
            })
    }

    private fun setupProfileOptionRecyclerView() {
        mRecyclerView.adapter = ProfileOptionAdapter(mOnItemProfileOptionClick)
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)
    }

    override fun observe() {
        vm.onGetAppUserResult.observe(this, Observer {
            mTvNickname.text = it.nickname
            if (it.avatarUrl.isNotEmpty())
                Glide.with(requireContext())
                    .load(it.avatarUrl)
                    .placeholder(R.drawable.ic_no_avatar_50px)
                    .centerCrop()
                    .into(mImgAvatar)
        })
    }

}