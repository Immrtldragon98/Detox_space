package com.immrtldragon.detoxspace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.immrtldragon.detoxspace.domain.Connection
import com.immrtldragon.detoxspace.domain.Presence
import com.immrtldragon.detoxspace.domain.SignalType
import com.immrtldragon.detoxspace.domain.TimeWindow
import com.immrtldragon.detoxspace.domain.InvitationState
import com.immrtldragon.detoxspace.ui.theme.DetoxSpaceTheme

private enum class Tab(val label: String) { PEOPLE("People"), MOMENTS("Moments"), SETTINGS("Settings") }

@Composable
fun DetoxSpaceApp(vm: DetoxViewModel = viewModel(), onLogout: () -> Unit = {}) {
    val connections by vm.connections.collectAsStateWithLifecycle()
    val recent by vm.recentSignals.collectAsStateWithLifecycle()
    val presence by vm.presence.collectAsStateWithLifecycle()
    val darkMode by vm.darkMode.collectAsStateWithLifecycle()
    val connectionCodeUi by vm.connectionCodeUi.collectAsStateWithLifecycle()
    val syncing by vm.syncing.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.PEOPLE) }
    var target by remember { mutableStateOf<Connection?>(null) }
    var showConnect by remember { mutableStateOf(false) }

    DetoxSpaceTheme(darkTheme = darkMode) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == Tab.PEOPLE, { tab = Tab.PEOPLE }, { Icon(Icons.Rounded.People, null) }, { Text("People") })
                NavigationBarItem(tab == Tab.MOMENTS, { tab = Tab.MOMENTS }, { Icon(Icons.Rounded.History, null) }, { Text("Moments") })
                NavigationBarItem(tab == Tab.SETTINGS, { tab = Tab.SETTINGS }, { Icon(Icons.Rounded.Settings, null) }, { Text("Settings") })
            }
        }
    ) { padding ->
        when (tab) {
            Tab.PEOPLE -> PeopleScreen(padding, connections, presence, vm::setPresence, { showConnect = true }) { target = it }
            Tab.MOMENTS -> MomentsScreen(padding, recent, syncing, vm::refresh, vm::updateInvitation)
            Tab.SETTINGS -> SettingsScreen(padding, darkMode, vm::setDarkMode, onLogout)
        }
    }

    target?.let { person ->
        SignalSheet(person, vm.signalTypes, onDismiss = { target = null }) { signal, window, note ->
            vm.sendSignal(person, signal, window, note)
            target = null
            tab = Tab.MOMENTS
        }
    }
    if (showConnect) {
        ConnectionSheet(
            state = connectionCodeUi,
            onDismiss = { showConnect = false; vm.clearConnectionCodeState() },
            onCreate = vm::createConnectionCode,
            onAccept = vm::acceptConnectionCode,
        )
    }
    }
}

@Composable
private fun PeopleScreen(
    padding: PaddingValues,
    people: List<Connection>,
    presence: Presence,
    onPresence: (Presence) -> Unit,
    onConnect: () -> Unit,
    onPerson: (Connection) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Detox Space", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Less scrolling. More real moments.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(22.dp))
            Text("How available are you?", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Presence.entries.forEach { value ->
                    FilterChip(
                        selected = presence == value,
                        onClick = { onPresence(value) },
                        label = { Text(value.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Your people", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Tap someone. Send one small invitation.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onConnect, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Connect a trusted person")
            }
        }
        items(people, key = { it.id }) { person -> PersonCard(person) { onPerson(person) } }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FavoriteBorder, null)
                    Spacer(Modifier.width(12.dp))
                    Text("No feed. No likes. Just a gentle way to reach your people.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionSheet(
    state: DetoxViewModel.ConnectionCodeUiState,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onAccept: (String) -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("Connect a trusted person", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Share a private code in person or through a channel you trust.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            state.generatedCode?.let { generated ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Your 24-hour code", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(6.dp))
                        Text(generated, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { clipboard.setText(AnnotatedString(generated)) }) { Text("Copy code") }
                    }
                }
                Spacer(Modifier.height(16.dp))
            } ?: OutlinedButton(onClick = onCreate, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                Text("Create my private code")
            }
            HorizontalDivider(Modifier.padding(vertical = 18.dp))
            Text("Have their code?", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filterNot(Char::isWhitespace).take(64) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Connection code") },
                singleLine = true,
            )
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)) }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onAccept(code) },
                enabled = !state.loading && code.length >= 12,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Connect")
            }
            Text(
                "Codes work once and expire after 24 hours. No contacts or location access is used.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun PersonCard(person: Connection, onClick: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable(enabled = person.allowSignals, onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text(person.initials, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(person.name, fontWeight = FontWeight.Bold)
                Text(person.status, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val color = when (person.presence) {
                Presence.AVAILABLE -> Color(0xFF4F8A68)
                Presence.QUIET -> Color(0xFFE0A458)
                Presence.AWAY -> Color(0xFF9A9A9A)
            }
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignalSheet(
    person: Connection,
    signals: List<SignalType>,
    onDismiss: () -> Unit,
    onSend: (SignalType, TimeWindow, String?) -> Unit,
) {
    var selectedSignal by remember { mutableStateOf<SignalType?>(null) }
    var selectedWindow by remember { mutableStateOf(TimeWindow.NOW) }
    var note by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("Invite ${person.name} into a moment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("One tap. No pressure. They can answer when ready.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            signals.forEach { signal ->
                ListItem(
                    headlineContent = { Text("${signal.emoji}  ${signal.title}", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(signal.subtitle) },
                    trailingContent = { RadioButton(selectedSignal == signal, { selectedSignal = signal }) },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { selectedSignal = signal },
                )
            }
            Text("When?", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeWindow.entries.forEach { window ->
                    FilterChip(selectedWindow == window, { selectedWindow = window }, { Text(window.label) })
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 80) note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Optional note") },
                supportingText = { Text("${note.length}/80") },
                maxLines = 2,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { selectedSignal?.let { onSend(it, selectedWindow, note) } },
                enabled = selectedSignal != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Send invitation") }
        }
    }
}

@Composable
private fun MomentsScreen(
    padding: PaddingValues,
    recent: List<com.immrtldragon.detoxspace.domain.SentSignal>,
    syncing: Boolean,
    onRefresh: () -> Unit,
    onState: (String, InvitationState) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Moments", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Private invitations between your people.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRefresh, enabled = !syncing) {
                if (syncing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Icon(Icons.Rounded.Refresh, contentDescription = "Refresh invitations")
            }
        }
        Spacer(Modifier.height(20.dp))
        if (recent.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your real moments will begin here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            recent.forEach { item ->
                ListItem(
                    overlineContent = { Text(if (item.isIncoming) "FROM ${item.connectionName.uppercase()}" else "TO ${item.connectionName.uppercase()}") },
                    headlineContent = { Text(item.signalTitle) },
                    supportingContent = {
                        Column {
                            Text("${item.timeWindow} · ${item.state.name.lowercase()} · ${item.sentAt}")
                            item.note?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    },
                    trailingContent = {
                        if (!item.isIncoming && (item.state == InvitationState.SENT || item.state == InvitationState.ACCEPTED)) {
                            TextButton(onClick = {
                                onState(item.id, if (item.state == InvitationState.SENT) InvitationState.CANCELLED else InvitationState.COMPLETED)
                            }) { Text(if (item.state == InvitationState.SENT) "Cancel" else "Done") }
                        }
                    },
                )
                if (item.isIncoming && item.state in setOf(InvitationState.SENT, InvitationState.LATER)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onState(item.id, InvitationState.DECLINED) }) { Text("Not today") }
                        TextButton(onClick = { onState(item.id, InvitationState.LATER) }, enabled = item.state == InvitationState.SENT) { Text("Later") }
                        Button(onClick = { onState(item.id, InvitationState.ACCEPTED) }) { Text("I'm in") }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    padding: PaddingValues,
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        ListItem(
            headlineContent = { Text("Dark mode") },
            supportingContent = { Text("Saved on this device") },
            trailingContent = { Switch(checked = darkMode, onCheckedChange = onDarkMode) },
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Privacy first") },
            supportingContent = { Text("No location, contacts, feed, likes, or background tracking") },
        )
        HorizontalDivider()
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
}
