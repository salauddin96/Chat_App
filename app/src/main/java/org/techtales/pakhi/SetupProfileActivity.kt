package org.techtales.pakhi

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.techtales.pakhi.databinding.ActivitySetupProfileBinding
import org.techtales.pakhi.model.User
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null
    private lateinit var binding: ActivitySetupProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.profileImageBig.setOnClickListener {

            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        binding.setupBtn.setOnClickListener {

            val name: String = binding.nameETxt.text.toString()
            if (name.isEmpty()) {
                binding.nameETxt.setError("Please type a name...")

            }
            dialog!!.show()
            if (selectedImage != null) {

                val reference = storage!!.reference.child("Profile")
                    .child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val imageUrl = it.toString()
                        val uid = auth!!.uid
                        val phone = auth!!.currentUser!!.phoneNumber
                        val name: String = binding.nameETxt.text.toString()
                        val user = User(uid!!, name, phone!!, imageUrl)

                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCompleteListener {

                                dialog!!.dismiss()
                                val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    } else {

                        val uid = auth!!.uid
                        val phone = auth!!.currentUser!!.phoneNumber
                        val name: String = binding.nameETxt.text.toString()
                        val user = User(uid!!, name, phone!!, "No Image")

                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCompleteListener {

                                dialog!!.dismiss()
                                val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }


        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data!= null) {

            if (data.data!= null) {

                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString()+"")
                reference.putFile(uri!!).addOnCompleteListener {

                    if (it.isSuccessful) {

                        reference.downloadUrl.addOnCompleteListener {

                            val filePath = it.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database!!.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener {  }

                        }


                    }
                }

                binding.profileImage.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }
}