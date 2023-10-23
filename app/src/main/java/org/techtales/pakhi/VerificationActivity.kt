package org.techtales.pakhi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import org.techtales.pakhi.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {

    var auth:FirebaseAuth?=null

    private lateinit var binding: ActivityVerificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser!=null){

            startActivity(Intent(this@VerificationActivity, MainActivity::class.java))
            finish()
        }

        binding.phnText.requestFocus()
        binding.continueBtn.setOnClickListener{

            var intent = Intent(this@VerificationActivity, OTPActivity::class.java)
            intent.putExtra("phoneNumber", binding.phnText.text.toString())
            startActivity(intent)

        }

    }
}