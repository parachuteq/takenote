package com.example.takenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleLoginButton = findViewById<Button>(R.id.googleLoginButton)

        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.usernameEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUserWithEmailAndPassword(email, password)
            } else {
                Toast.makeText(this, "Wprowadź adres e-mail i hasło.", Toast.LENGTH_SHORT).show()
            }
        }

        googleLoginButton.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("not yet")
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent

            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun loginUserWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, NotesActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Błąd logowania: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, NotesActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Błąd logowania za pomocą konta Google: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Błąd logowania za pomocą konta Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
