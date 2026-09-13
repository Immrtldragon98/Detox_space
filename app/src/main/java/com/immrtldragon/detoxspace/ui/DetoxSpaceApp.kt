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

private enum class Tab(val label: String) { PEOPLE("People"), MOMENTS("Moments") }

@Composable
fun DetoxSpaceApp(vm: DetoxViewModel = viewModel()) {
    val connections by vm.connections.collectAsStateWithLifecycle()
    val recent by vm.recentSignals.collectAsStateWithLifecycle()
    val presence by vm.presence.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.PEOPLE) }
    var target by remember { mutableStateOf<Connection?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == Tab.PEOPLE, { tab = Tab.PEOPLE }, { Icon(Icons.Rounded.People, null) }, { Text("People") })
                NavigationBarItem(tab == Tab.MOMENTS, { tab = Tab.MOMENTS }, { Icon(Icons.Rounded.History, null) }, { Text("Moments") })
            }
        }
    ) { padding ->
        when (tab) {
            Tab.PEOPLE -> PeopleScreen(padding, connections, presence, vm::setPresence) { target = it }
            Tab.MOMENTS -> MomentsScreen(padding, recent)
        }
    }

    target?.let { person ->
        SignalSheet(person, vm.signalTypes, onDismiss = { target = null }) { signal ->
            vm.sendSignal(person, signal)
            target = null
            tab = Tab.MOMENTS
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
private fun SignalSheet(person: Connection, signals: List<SignalType>, onDismiss: () -> Unit, onSend: (SignalType) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("Invite ${person.name} into a moment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("One tap. No pressure. They can answer when ready.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            signals.forEach { signal ->
                ListItem(
                    headlineContent = { Text("${signal.emoji}  ${signal.title}", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(signal.subtitle) },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onSend(signal) },
                )
            }
        }
    }
}

@Composable
private fun MomentsScreen(padding: PaddingValues, recent: List<com.immrtldragon.detoxspace.domain.SentSignal>) {
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
                    supportingContent = { Text("Sent ${item.sentAt}") },
                )
                HorizontalDivider()
            }
        }
    }
}

