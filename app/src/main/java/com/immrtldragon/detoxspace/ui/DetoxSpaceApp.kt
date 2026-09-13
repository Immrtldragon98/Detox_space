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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun DetoxSpaceApp(vm: DetoxViewModel = viewModel()) {
    val connections by vm.connections.collectAsStateWithLifecycle()
    val recent by vm.recentSignals.collectAsStateWithLifecycle()
    val presence by vm.presence.collectAsStateWithLifecycle()
    val darkMode by vm.darkMode.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.PEOPLE) }
    var target by remember { mutableStateOf<Connection?>(null) }

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
            Tab.PEOPLE -> PeopleScreen(padding, connections, presence, vm::setPresence) { target = it }
            Tab.MOMENTS -> MomentsScreen(padding, recent, vm::updateInvitation)
            Tab.SETTINGS -> SettingsScreen(padding, darkMode, vm::setDarkMode)
        }
    }

    target?.let { person ->
        SignalSheet(person, vm.signalTypes, onDismiss = { target = null }) { signal, window, note ->
            vm.sendSignal(person, signal, window, note)
            target = null
            tab = Tab.MOMENTS
        }
    }
    }
}

@Composable
private fun PeopleScreen(
    padding: PaddingValues,
    people: List<Connection>,
    presence: Presence,
    onPresence: (Presence) -> Unit,
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
    onState: (String, InvitationState) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
        Text("Moments", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Invitations you sent—not engagement statistics.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        if (recent.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your real moments will begin here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            recent.forEach { item ->
                ListItem(
                    headlineContent = { Text("${item.signalTitle} · ${item.connectionName}") },
                    supportingContent = { Text("${item.timeWindow} · ${item.state.name.lowercase()} · ${item.sentAt}") },
                    trailingContent = {
                        if (item.state == InvitationState.SENT || item.state == InvitationState.ACCEPTED) {
                            TextButton(onClick = {
                                onState(item.id, if (item.state == InvitationState.SENT) InvitationState.CANCELLED else InvitationState.COMPLETED)
                            }) { Text(if (item.state == InvitationState.SENT) "Cancel" else "Done") }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues, darkMode: Boolean, onDarkMode: (Boolean) -> Unit) {
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
    }
}
