package com.immrtldragon.detoxspace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.immrtldragon.detoxspace.ui.theme.DetoxSpaceTheme

@Composable
fun DetoxRoot(auth: AuthViewModel = hiltViewModel()) {
    val session by auth.session.collectAsStateWithLifecycle()
    if (session == null) AuthScreen(auth) else DetoxSpaceApp(onLogout = auth::logout, onDeleteAccount = auth::deleteAccount)
}

@Composable
private fun AuthScreen(vm: AuthViewModel) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var createAccount by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    DetoxSpaceTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("•  •", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Detox Space", style = MaterialTheme.typography.headlineLarge)
                Text("Less scrolling. More real moments.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(28.dp))
                if (createAccount) {
                    OutlinedTextField(username, { username = it.take(24) }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(email, { email = it.take(254) }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
                } else {
                    OutlinedTextField(login, { login = it.take(254) }, Modifier.fillMaxWidth(), label = { Text("Username or email") }, singleLine = true)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    password, { password = it.take(128) }, Modifier.fillMaxWidth(),
                    label = { Text("Password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
                ui.error?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        vm.clearError()
                        if (createAccount) vm.register(username, email, password) else vm.login(login, password)
                    },
                    enabled = !ui.loading && password.length >= 10 &&
                        (if (createAccount) username.length >= 3 && email.contains("@") else login.length >= 3),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (ui.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(if (createAccount) "Create account" else "Sign in")
                }
                TextButton(
                    onClick = { createAccount = !createAccount; vm.clearError() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (createAccount) "Already have an account? Sign in" else "New here? Create account") }
                Text(
                    "Your private invitations are not a feed and are never public.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
