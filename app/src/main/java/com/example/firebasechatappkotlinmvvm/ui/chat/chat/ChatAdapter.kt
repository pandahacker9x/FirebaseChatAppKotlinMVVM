package com.example.firebasechatappkotlinmvvm.ui.chat.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasechatappkotlinmvvm.R
import com.example.firebasechatappkotlinmvvm.data.repo.chat.Messagee
import com.example.firebasechatappkotlinmvvm.ui.base.BaseViewHolder
import com.example.firebasechatappkotlinmvvm.ui.base.OnItemClickListener
import com.example.firebasechatappkotlinmvvm.ui.common.ImageViewerDialog
import com.example.firebasechatappkotlinmvvm.util.CommonUtil
import com.example.firebasechatappkotlinmvvm.util.extension.format
import com.example.firebasechatappkotlinmvvm.util.extension.subInMilis
import kotlinx.android.synthetic.main.item_img_msg_me.view.*
import kotlinx.android.synthetic.main.item_text_msg_me.view.*
import kotlinx.android.synthetic.main.item_text_msg_me.view.mTvTime

class ChatAdapter(
    val onMsgClickListener: OnItemClickListener<Messagee>
) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    lateinit var meId: String
    lateinit var childFragmentManager: FragmentManager

    // Init (messages) with Messagee.MSG_LOAD_MORE to show loading progress item
    // When load first messages done (messages) will reassigned
    // so the loading progress item is hide
    var messages: MutableList<Messagee> = mutableListOf(Messagee.MSG_LOAD_MORE)

    companion object {
        const val VIEW_TYPE_TEXT_MSG_ME = 0
        const val VIEW_TYPE_TEXT_MSG_OTHER = 1
        const val VIEW_TYPE_IMG_MSG_ME = 2
        const val VIEW_TYPE_IMG_MSG_OTHER = 3
        const val VIEW_TYPE_VOICE_MSG_ME = 4
        const val VIEW_TYPE_VOICE_MSG_OTHER = 5
        const val VIEW_TYPE_LOAD_MORE = 6
    }

    override fun getItemViewType(position: Int): Int {
        if (messages[position].type == Messagee.MSG_TYPE_LOAD_MORE)
            return VIEW_TYPE_LOAD_MORE

        return if (messages[position].senderUserId == meId) {
            when (messages[position].type) {
                Messagee.MSG_TYPE_TEXT -> VIEW_TYPE_TEXT_MSG_ME
                Messagee.MSG_TYPE_IMG -> VIEW_TYPE_IMG_MSG_ME
                Messagee.MSG_TYPE_VOICE -> VIEW_TYPE_VOICE_MSG_ME
                else -> VIEW_TYPE_TEXT_MSG_ME
            }
        } else when (messages[position].type) {
            Messagee.MSG_TYPE_TEXT -> VIEW_TYPE_TEXT_MSG_OTHER
            Messagee.MSG_TYPE_IMG -> VIEW_TYPE_IMG_MSG_OTHER
            Messagee.MSG_TYPE_VOICE -> VIEW_TYPE_VOICE_MSG_OTHER
            else -> VIEW_TYPE_TEXT_MSG_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val viewResId: Int =
            when (viewType) {
                VIEW_TYPE_TEXT_MSG_ME -> R.layout.item_text_msg_me
                VIEW_TYPE_IMG_MSG_ME -> R.layout.item_img_msg_me
                VIEW_TYPE_VOICE_MSG_ME -> R.layout.item_text_msg_me
                VIEW_TYPE_TEXT_MSG_OTHER -> R.layout.item_text_msg_other
                VIEW_TYPE_IMG_MSG_OTHER -> R.layout.item_img_msg_other
                VIEW_TYPE_VOICE_MSG_OTHER -> R.layout.item_text_msg_other

                VIEW_TYPE_LOAD_MORE -> R.layout.item_load_more_msg
                else -> R.layout.item_text_msg_me
            }

        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(viewResId, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bindView(position)
    }

    fun addMessage(message: Messagee) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun addLoadMoreProgress() {
        messages.add(0, Messagee.MSG_LOAD_MORE)
        notifyItemInserted(0)
    }

    fun addNextMessages(it: List<Messagee>?) {
        messages.addAll(0, it!!)
        notifyItemRangeInserted(0, it.size)
    }

    fun removeLoadMoreProgress() {
        if (!messages.isNullOrEmpty()) {
            /*
            *  messages[0].type not always == Messagee.MSG_TYPE_LOAD_MORE/
            *  for ex in case of calling addNextMessages() before calling removeLoadMoreProgress()
            *  so use while loop to find and remove message with type MSG_TYPE_LOAD_MOR
            * */
            var i = 0
            // @Warning use for loop will get out of bound exception
            // when an element removed
            while (i < messages.size) {
                if (messages[i].type == Messagee.MSG_TYPE_LOAD_MORE){
                    messages.removeAt(i)
                    notifyItemRemoved(i)
                    break
                }
                i++
            }
        }
    }

    inner class ChatViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindView(position: Int) {
            if (messages[position].type != Messagee.MSG_TYPE_LOAD_MORE) {
                setEvent(position)
                setData(position)
            } else CommonUtil.log("Item load more")
        }

        private fun setEvent(position: Int) {
            itemView.setOnClickListener {
                onMsgClickListener.onItemClicked(
                    position,
                    messages[position]
                )
                if (itemView.mTvTime.visibility == View.GONE)
                    itemView.mTvTime.visibility = View.VISIBLE
                else itemView.mTvTime.visibility = View.GONE
            }

            if (messages[position].type == Messagee.MSG_TYPE_IMG){
                itemView.mMsgImg.setOnClickListener {
                    ImageViewerDialog.show(
                        messages[position].content,
                        childFragmentManager
                    )
                }
            }
        }

        private fun setData(position: Int) {
            val message = messages[position]
            setTime(message, position)
            showTimeIfLongTimeSpacing(message, position)
            setContentMessageByType(message)
        }

        private fun setContentMessageByType(message: Messagee) {
            when (message.type) {
                Messagee.MSG_TYPE_TEXT -> itemView.mTvMsg.text = message.content

                Messagee.MSG_TYPE_IMG ->
                    Glide.with(itemView.context)
                        .load(message.content)
                        .centerCrop()
                        .into(itemView.mMsgImg)

                Messagee.MSG_TYPE_VOICE -> itemView.mTvMsg.text = message.content
            }
        }

        // Set time for message
        // If the next msg has time with the same day/month/year
        // then just show time in HH:mm for it
        // Otherwise show full time in HH:mm dd/MM/yyyy
        private fun setTime(message: Messagee, position: Int) {
            if (message.createdAt == null) {
                itemView.mTvTime.text = ""
                return
            }

            val pattern: String = CommonUtil.createDatePattern(message.createdAt)

            itemView.mTvTime.text = message.createdAt.format(pattern)
        }

        private val TIME_SPACING_ENOUGH_LONG: Long = 2 * 60 * 1000

        // By default msg time is gone
        // This func check if the spacing time is long then show the time
        private fun showTimeIfLongTimeSpacing(message: Messagee, position: Int) {
            // check in the case of self message
            // message.createdAt is provided from firebase server
            // so at that time message.createdAt = null, do not
            // load createdAt because no need
            if (message.createdAt == null ||
                position >= 1 &&
                messages[position - 1].createdAt == null
            ) {
                // When message.createdAt = null and If the
                // tv time is visible, then hide it
                if (itemView.mTvTime.visibility == View.VISIBLE)
                    itemView.mTvTime.visibility = View.GONE
                return
            }

            val timeSpacing: Long =
                when {
                    messages.size == 1 -> TIME_SPACING_ENOUGH_LONG + 1
                    position > 0 -> message.createdAt.subInMilis(
                        messages[position - 1].createdAt!!
                    )
                    else -> messages[1].createdAt!!.subInMilis(
                        message.createdAt
                    )
                }

            if (timeSpacing > TIME_SPACING_ENOUGH_LONG) // timeSpacing > 2 minutes
                itemView.mTvTime.visibility = View.VISIBLE
            else itemView.mTvTime.visibility = View.GONE
        }
    }
}
