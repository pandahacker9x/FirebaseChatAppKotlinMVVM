package com.example.firebasechatappkotlinmvvm.ui.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.example.firebasechatappkotlinmvvm.R
import com.example.firebasechatappkotlinmvvm.data.callback.SingleCallBack
import com.example.firebasechatappkotlinmvvm.data.repo.user.AppUser
import com.example.firebasechatappkotlinmvvm.util.CommonUtil
import dagger.android.support.AndroidSupportInjection
import java.io.InputStream
import java.io.Serializable
import java.lang.Exception

abstract class BaseDialogFragment<TViewBinding : ViewDataBinding, TVModel : BaseViewModel> : DialogFragment() {
    lateinit var vm: TVModel
    lateinit var viewBinding: TViewBinding
    lateinit var mBaseActivity: BaseActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity)
            mBaseActivity = context
        else throw Exception("All Activities must be children of BaseActivity")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        // Make the dialog background transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        vm = getVM()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        viewBinding = DataBindingUtil.inflate(
            inflater, getLayoutResId(), container, false
        )
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewBinding()
        setupViews()
        observeCommonly()
        observe()
    }

    private fun observeCommonly() {
        vm.onError.observe(viewLifecycleOwner, Observer {
            showToastMsg(getString(R.string.sth_went_wrong))
        })
        vm.isLoading.observe(viewLifecycleOwner, Observer {
            if (it) showLoading() else hideLoading()
        })
    }

    fun setupViewBinding() {
        viewBinding.lifecycleOwner = this
        viewBinding.setVariable(getVMBindingVarId(), vm)
        viewBinding.executePendingBindings()
    }

    fun showToastMsg(msg: String) {
        Toast.makeText(this.context, msg, Toast.LENGTH_SHORT)?.show()
    }

    fun showToastMsg(msgResId: Int) {
        Toast.makeText(this.context, msgResId, Toast.LENGTH_SHORT)?.show()
    }

    protected fun showLoading() {
        if (mBaseActivity != null) mBaseActivity.showLoading()
        else CommonUtil.log("BaseActivity.showLoading(): mBaseActivity is not init")
    }

    protected fun hideLoading() {
        if (mBaseActivity != null) mBaseActivity.hideLoading()
        else CommonUtil.log("BaseActivity.showLoading(): mBaseActivity is not init")
    }

    protected fun finishActivity() {
        mBaseActivity.finish()
    }

    protected fun navigate(actionResId: Int) {
        findNavController().navigate(actionResId)
    }

    protected fun navigateWithParcelableData(actionResId: Int, data: Parcelable, dataKey: String) {
        val bundle = Bundle()
        bundle.putParcelable(dataKey, data)
        findNavController().navigate(actionResId, bundle)
    }

    protected fun popBackFragment() {
        findNavController().popBackStack()
    }

    fun showConfirmDialog(
        msgResId: Int,
        onYes: DialogInterface.OnClickListener,
        onNo: DialogInterface.OnClickListener? = null
    ) {
        mBaseActivity.showConfirmDialog(msgResId, onYes, onNo)
    }

    protected fun selectMediaImage(selectImgCallBack: SingleCallBack<Uri>) {
        mBaseActivity.selectMediaImage(selectImgCallBack)
    }

    protected fun openInputStream(uri: Uri): InputStream? {
        return mBaseActivity.contentResolver.openInputStream(uri)
    }

    abstract fun getLayoutResId(): Int
    abstract fun getVMBindingVarId(): Int
    abstract fun getVM(): TVModel
    abstract fun setupViews()
    abstract fun observe()
}
