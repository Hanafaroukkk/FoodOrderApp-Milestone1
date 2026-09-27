package com.example.foodorderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val uid = auth.currentUser?.uid

        if (uid == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val userRef = FirebaseDatabase.getInstance("https://signaling-project-76d29-default-rtdb.firebaseio.com")
            .reference.child("users").child(uid)

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

            userRef.child("name").setValue(name)
            userRef.child("phone").setValue(phone)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}