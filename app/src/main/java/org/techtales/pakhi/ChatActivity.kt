package org.techtales.pakhi

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.techtales.pakhi.UsersAdapter.MessagesAdapter
import org.techtales.pakhi.databinding.ActivityChatBinding
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    var binding : ActivityChatBinding? = null
    var adpter : MessagesAdapter? = null
    var messages : ArrayList<org.techtales.pakhi.model.Message>? = null
    var senderRoom : String? = null
    var receiverRoom : String? = null
    var database : FirebaseDatabase? = null
    var storage : FirebaseStorage? = null
    var dialog : ProgressDialog? = null
    var senderUid : String? = null
    var receiverUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        
        binding = ActivityChatBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding!!.profile01)
        binding!!.imageView.setOnClickListener {
            
            finish()
        }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   
                    if (snapshot.exists()){
                        
                        val status = snapshot.getValue(String::class.java)
                        if (status == "Offline"){
                            binding!!.status.visibility = View.GONE
                        }
                        else{
                            binding!!.status.setText(status)
                            binding!!.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adpter = MessagesAdapter(this@ChatActivity,messages, senderRoom!!, receiverRoom!!)
        
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.recyclerView.adapter = adpter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snaphot1 in snapshot.children){
                        val message : org.techtales.pakhi.model.Message? = snaphot1.getValue(org.techtales.pakhi.model.Message::class.java)
                        message!!.messageId = snaphot1.key
                        messages!!.add(message)
                    }
                    adpter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        binding!!.sendBtn.setOnClickListener {

            val messageTxt: String = binding!!.meesageBox.text.toString()
            val date = Date()
            val message = org.techtales.pakhi.model.Message(messageTxt,senderUid,date.time)

            binding!!.meesageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgobj = HashMap<String,Any>()
            lastMsgobj["lastMsg"] = message.message!!
            lastMsgobj["lastMsgTime"] = date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgobj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgobj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }
                }
        }
        binding!!.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding!!.meesageBox.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
               database!!.reference.child("Presence")
                   .child(senderUid!!)
                   .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)

            }
            var userStoppedTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }


        })
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25){
            if (data!!.data != null){
                if (data.data != null){
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    var refence = storage?.reference?.child("chats")
                        ?.child(calendar.timeInMillis.toString()+"")
                    dialog!!.show()
                    refence!!.putFile(selectedImage!!)
                        .addOnCompleteListener{task->
                            dialog!!.dismiss()
                            if (task.isSuccessful){
                                refence.downloadUrl.addOnSuccessListener { uri->
                                    val filePath = uri.toString()
                                    val messageTxt :String = binding!!.meesageBox.text.toString()
                                    val date = Date()
                                    val message = org.techtales.pakhi.model.Message(messageTxt,senderUid,date.time)
                                    message.message = "photo"
                                    message.imageUrl = filePath
                                    binding!!.meesageBox.setText("")
                                    val randomKey = database!!.reference.push().key
                                    val lastMsgobj = HashMap<String,Any>()
                                    lastMsgobj["lastMsg"] = message.message!!
                                    lastMsgobj["lastMsgTime"] = date.time
                                    database!!.reference.child("chats")
                                        .updateChildren(lastMsgobj)
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .updateChildren(lastMsgobj)
                                    database!!.reference.child("chats")
                                        .child(senderRoom!!)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database!!.reference.child("chats")
                                                .child(receiverRoom!!)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener {  }
                                        }

                                }
                            }
                        }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Offline")
    }
}