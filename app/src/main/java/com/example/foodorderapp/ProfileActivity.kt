package com.example.foodorderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        val uid = user.uid

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val userRef = FirebaseDatabase.getInstance("https://signaling-project-76d29-default-rtdb.firebaseio.com")
            .reference.child("users").child(uid)

        etEmail.setText(user.email ?: "")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                etName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                etPhone.setText(snapshot.child("phone").getValue(String::class.java) ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            userRef.child("name").setValue(name)
            userRef.child("phone").setValue(phone)

            val emailChanged = newEmail.isNotEmpty() && newEmail != user.email
            val passwordChanged = newPassword.isNotEmpty()

            if (emailChanged || passwordChanged) {
                if (currentPassword.isEmpty()) {
                    Toast.makeText(this, "Enter your current password to change email or password", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        if (emailChanged) {
                            user.verifyBeforeUpdateEmail(newEmail)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Verification email sent to $newEmail. Click the link there to confirm.", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Email update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }

                        if (passwordChanged) {
                            user.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show()
                                    etNewPassword.setText("")
                                    etCurrentPassword.setText("")
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Password update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }

                        if (!emailChanged && !passwordChanged) {
                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Reauthentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}