package com.nexauren.docpilot.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.nexauren.docpilot.ui.localization.LText
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.nexauren.docpilot.R
import kotlinx.coroutines.launch
import com.nexauren.docpilot.BuildConfig
import com.nexauren.docpilot.ui.localization.DocPilotLocalization
import androidx.compose.material3.FilterChip
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    hasDocumentAccess: Boolean,
    selectedLanguage: String,
    onLanguageChanged: (String) -> Unit,
    onRequestDocumentAccess: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isCreatingAccount by remember { mutableStateOf(true) }
    var authBusy by remember { mutableStateOf(false) }
    var authMessage by remember { mutableStateOf<String?>(null) }

    fun signInWithGoogle() {
        authBusy = true
        authMessage = null
        scope.launch {
            try {
                val googleOption = GetGoogleIdOption.Builder()
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = credentialManager.getCredential(
                    context = context,
                    request = request,
                )
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleToken.idToken, null)
                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                        authBusy = false
                        if (task.isSuccessful) {
                            currentUser = auth.currentUser
                            authMessage = "Sessão iniciada com Google."
                        } else {
                            authMessage = authErrorMessage(task.exception)
                        }
                    }
                } else {
                    authBusy = false
                    authMessage = "Não foi possível validar a conta Google. Tenta novamente."
                }
            } catch (e: GetCredentialException) {
                authBusy = false
                authMessage = "O início de sessão com Google foi cancelado ou não ficou disponível. Tenta novamente."
            } catch (e: Exception) {
                authBusy = false
                authMessage = "Não foi possível iniciar sessão com Google. Verifica a configuração OAuth e tenta novamente."
            }
        }
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LText("Definições") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Abrir menu")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LText(
                "PDF Reader & Tools, à tua maneira.",
                style = MaterialTheme.typography.headlineSmall,
            )
            LText(
                "Preferências, conta opcional e privacidade.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LText("Idioma da aplicação", style = MaterialTheme.typography.titleMedium)
                    LText(
                        "Escolhe o idioma dos menus e das ferramentas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "pt" to "Português",
                            "en" to "English",
                            "fr" to "Français",
                            "es" to "Español",
                            "de" to "Deutsch",
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = selectedLanguage == code,
                                onClick = { onLanguageChanged(code) },
                                label = { LText(label) },
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        LText(
                            "Conta PDF Reader & Tools (opcional)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                    LText(
                        "Podes continuar a ler e tratar PDFs sem criar uma conta. A conta é opcional e serve para iniciar sessão.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (currentUser != null) {
                        LText("Sessão iniciada", style = MaterialTheme.typography.titleSmall)
                        LText(
                            currentUser?.email.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(
                            onClick = {
                                auth.signOut()
                                scope.launch {
                                    runCatching {
                                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                    }
                                }
                                password = ""
                                authMessage = "Sessão terminada."
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LText("Terminar sessão")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { signInWithGoogle() },
                            enabled = !authBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LText("Continuar com Google")
                        }
                        LText(
                            "ou utiliza o e-mail e a palavra-passe",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; authMessage = null },
                            label = { LText("E-mail") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; authMessage = null },
                            label = { LText("Palavra-passe") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                        )
                        Button(
                            onClick = {
                                val normalizedEmail = email.trim()
                                when {
                                    !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                                        authMessage = "Introduz um endereço de e-mail válido."
                                    }
                                    isCreatingAccount && password.length < 6 -> {
                                        authMessage = "A palavra-passe deve ter pelo menos 6 caracteres."
                                    }
                                    password.isBlank() -> {
                                        authMessage = "Introduz a palavra-passe."
                                    }
                                    else -> {
                                        authBusy = true
                                        authMessage = null
                                        val task = if (isCreatingAccount) {
                                            auth.createUserWithEmailAndPassword(normalizedEmail, password)
                                        } else {
                                            auth.signInWithEmailAndPassword(normalizedEmail, password)
                                        }
                                        task.addOnCompleteListener { result ->
                                            authBusy = false
                                            if (result.isSuccessful) {
                                                currentUser = auth.currentUser
                                                password = ""
                                                authMessage = if (isCreatingAccount) {
                                                    "Conta criada com sucesso."
                                                } else {
                                                    "Sessão iniciada com sucesso."
                                                }
                                            } else {
                                                authMessage = authErrorMessage(result.exception)
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !authBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (authBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                LText(if (isCreatingAccount) "Criar conta" else "Entrar")
                            }
                        }

                        TextButton(
                            onClick = {
                                isCreatingAccount = !isCreatingAccount
                                authMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            LText(
                                if (isCreatingAccount) "Já tenho conta: entrar"
                                else "Ainda não tenho conta: criar conta",
                            )
                        }

                        if (!isCreatingAccount) {
                            TextButton(
                                onClick = {
                                    val normalizedEmail = email.trim()
                                    if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                                        authMessage = "Introduz primeiro o teu e-mail válido."
                                    } else {
                                        authBusy = true
                                        auth.sendPasswordResetEmail(normalizedEmail)
                                            .addOnCompleteListener { result ->
                                                authBusy = false
                                                authMessage = if (result.isSuccessful) {
                                                    "Se a conta existir, receberás uma mensagem para redefinir a palavra-passe."
                                                } else {
                                                    authErrorMessage(result.exception)
                                                }
                                            }
                                    }
                                },
                                enabled = !authBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                LText("Esqueci a palavra-passe")
                            }
                        }
                    }

                    authMessage?.let {
                        LText(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(bottom = 14.dp)) {
                    ListItem(
                        headlineContent = { LText("Biblioteca do dispositivo") },
                        supportingContent = {
                            LText(
                                if (hasDocumentAccess) "Acesso ativo. A aplicação pode localizar PDFs no armazenamento partilhado."
                                else "Ativa o acesso para encontrar PDFs automaticamente no dispositivo."
                            )
                        },
                        leadingContent = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                    )
                    Button(
                        onClick = onRequestDocumentAccess,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    ) {
                        LText(if (hasDocumentAccess) "Atualizar biblioteca" else "Configurar acesso a documentos")
                    }
                    ListItem(
                        headlineContent = { LText("Aparência") },
                        supportingContent = { LText("O tema acompanha as definições do sistema.") },
                        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                    )
                    ListItem(
                        headlineContent = { LText("Privacidade dos documentos") },
                        supportingContent = {
                            LText("Os PDFs são processados localmente. Não precisas de uma conta para usar as ferramentas locais.")
                        },
                        leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                ListItem(
                    headlineContent = { LText("Política de privacidade") },
                    supportingContent = { LText("Consulta como os teus dados são tratados.") },
                    leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                    modifier = Modifier.clickable {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://nexauren1.github.io/DocPilot-PDF-Reader/privacy-policy.html"),
                                ),
                            )
                        }
                    },
                )
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                ListItem(
                    headlineContent = { LText("Sobre PDF Reader & Tools") },
                    supportingContent = { LText("PDF Reader & Tools • versão ${BuildConfig.VERSION_NAME}") },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                )
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                ListItem(
                    headlineContent = { LText("Funcionalidades") },
                    supportingContent = { LText("Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização.") },
                    leadingContent = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                )
            }
        }
    }
}

private fun authErrorMessage(error: Exception?): String {
    val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
    return when {
        code.contains("EMAIL_ALREADY_IN_USE") -> "Este e-mail já está registado. Experimenta entrar."
        code.contains("WEAK_PASSWORD") -> "Escolhe uma palavra-passe mais forte, com pelo menos 6 caracteres."
        code.contains("INVALID_EMAIL") -> "O endereço de e-mail não é válido."
        code.contains("INVALID_LOGIN_CREDENTIALS") ||
            code.contains("WRONG_PASSWORD") ||
            code.contains("USER_NOT_FOUND") -> "E-mail ou palavra-passe incorretos."
        code.contains("OPERATION_NOT_ALLOWED") -> "Este método de início de sessão ainda não está disponível."
        code.contains("NETWORK_REQUEST_FAILED") -> "Sem ligação à Internet. Verifica a conexão e tenta novamente."
        else -> "Não foi possível concluir a operação. Verifica a ligação e tenta novamente."
    }
}
