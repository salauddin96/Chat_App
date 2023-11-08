package org.techtales.pakhi.UsersAdapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.techtales.pakhi.R
import org.techtales.pakhi.databinding.DeleteLayoutBinding
import org.techtales.pakhi.databinding.SendMsgBinding
import org.techtales.pakhi.model.Message

class MessagesAdapter(

    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom:String
): RecyclerView.Adapter<RecyclerView.ViewHolder?>()
{

    lateinit var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val senderRoom: String
    val receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return if (viewType == ITEM_SENT) {
           val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
           SentMsgHolder(view)
       }
        else{

            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
           ReceiveMsgHolder(view)
       }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messages[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId) {
            ITEM_SENT
        }else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java){
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding:DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog: AlertDialog? = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId.let { it1->
                       // if (it1 != null) {
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(senderRoom)
                                .child("message")
                                .child(it1!!).setValue(message)
                       // }

                    }
                    message.messageId.let {it1->
                        //if (it1 != null) {
                            FirebaseDatabase.getInstance().reference.child("chats")
                                .child(receiverRoom)
                                .child("message")
                                .child(it1!!).setValue(message)
                       // }

                    }

                    dialog?.dismiss()
                }
                binding.delete.setOnClickListener {

                    message.messageId.let { it1->
                        // if (it1 != null) {
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                        // }

                    }
                        dialog?.dismiss()

                }
                binding.cancel.setOnClickListener { dialog?.dismiss() }
                dialog?.show()
                false
            }

        }
        else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {

                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)

            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog: AlertDialog? = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId.let { it1 ->
                        // if (it1 != null) {
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                        // }

                    }
                    message.messageId.let { it1 ->
                        //if (it1 != null) {
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                        // }

                    }

                    dialog?.dismiss()
                }
                binding.delete.setOnClickListener {

                    message.messageId.let { it1 ->
                        // if (it1 != null) {
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1!!).setValue(message)
                        // }

                    }
                    dialog?.dismiss()

                }
                binding.cancel.setOnClickListener { dialog?.dismiss() }
                dialog?.show()
                false
            }
        }
    }

    inner class SentMsgHolder(itemView: android.view.View): RecyclerView.ViewHolder(itemView){
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: android.view.View): RecyclerView.ViewHolder(itemView){

        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }
    init {
        if (messages != null) {
            this.messages = messages
        }

        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }



}