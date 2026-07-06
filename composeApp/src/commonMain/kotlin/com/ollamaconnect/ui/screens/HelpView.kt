package com.ollamaconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpView(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hilfe", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text("Fertig", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            HelpHeaderCard()

            // Verbindung
            HelpSection(
                icon = Icons.Default.Wifi,
                iconColor = Color(0xFF3B82F6),
                title = "Verbindung",
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Dns,
                        "Server-Typ wählen",
                        "Wähle zwischen Ollama und llama-server. Beide Backends werden unterstützt — Ollama nutzt die native API, llama-server die OpenAI-kompatible API (/v1/chat/completions)."
                    ),
                    HelpItemInfo(
                        Icons.Default.SettingsEthernet,
                        "IP-Adresse & Port eingeben",
                        "Tippe auf den Verbindungsstatus oben links und gib die IP-Adresse deines Servers ein (z.B. 192.168.1.100). Standard-Port: Ollama 11434, llama-server 8080."
                    ),
                    HelpItemInfo(
                        Icons.Default.Refresh,
                        "Neu verbinden",
                        "Nach einer Unterbrechung kannst du mit \"Neu verbinden\" die Verbindung wiederherstellen, ohne die Einstellungen neu eingeben zu müssen."
                    ),
                    HelpItemInfo(
                        Icons.Default.History,
                        "Gespeicherte Server",
                        "Zuletzt verwendete Server-Adressen werden mit ihrem Typ automatisch gespeichert und können mit einem Tipp ausgewählt werden."
                    )
                )
            )

            // Chatten
            HelpSection(
                icon = Icons.Default.Chat,
                iconColor = Color(0xFF10B981),
                title = "Chatten",
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Send,
                        "Nachricht senden",
                        "Schreibe deine Nachricht im Eingabefeld und tippe auf den Pfeil-Button oder drücke ⌘+Enter auf der externen Tastatur."
                    ),
                    HelpItemInfo(
                        Icons.Default.Stop,
                        "Generierung stoppen",
                        "Während die Antwort generiert wird, erscheint ein roter Stop-Button. Damit kannst du die Generierung jederzeit abbrechen – der bisherige Text bleibt erhalten."
                    ),
                    HelpItemInfo(
                        Icons.Default.ContentCopy,
                        "Code kopieren",
                        "Code-Blöcke in Antworten zeigen oben rechts einen \"Kopieren\"-Button. Tippe darauf, um den Code in die Zwischenablage zu kopieren."
                    ),
                    HelpItemInfo(
                        Icons.Default.Computer,
                        "Modell wechseln",
                        "Oben rechts wählst du das aktive Modell aus der Liste aller auf dem Server verfügbaren Modelle."
                    )
                )
            )

            // Chat-Verlauf
            HelpSection(
                icon = Icons.Default.Menu,
                iconColor = Color(0xFF8B5CF6),
                title = "Chat-Verlauf",
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Edit,
                        "Neuer Chat",
                        "Starte einen neuen Chat mit dem Stift-Button in der Sidebar oder oben rechts im Chat."
                    ),
                    HelpItemInfo(
                        Icons.Default.Search,
                        "Chats suchen",
                        "Nutze das Suchfeld in der Sidebar, um Chats nach Titel, Modell oder Nachrichteninhalt zu filtern."
                    ),
                    HelpItemInfo(
                        Icons.Default.Label,
                        "Chat umbenennen",
                        "Halte einen Chat in der Sidebar gedrückt und wähle \"Umbenennen\", um dem Chat einen eigenen Titel zu geben."
                    ),
                    HelpItemInfo(
                        Icons.Default.Share,
                        "Chat exportieren",
                        "Exportiere einen Chat als formatierten Markdown-Text über das Kontextmenü (lange gedrückt halten)."
                    ),
                    HelpItemInfo(
                        Icons.Default.Delete,
                        "Chat löschen",
                        "Lösche einen Chat über das Kontextmenü (lange gedrückt halten)."
                    )
                )
            )

            // Assistent
            HelpSection(
                icon = Icons.Default.Face,
                iconColor = Color(0xFFEC4899),
                title = "Assistent (Persona · Gedächtnis)",
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Settings,
                        "Konfiguration",
                        "In der Konfiguration legst du fest, wie der Assistent heißt, wer du bist und welche Rahmenbedingungen für jeden Chat gelten — z.B. Sprache, Stil, no-gos. Wird jedem Chat als System-Prompt vorangestellt."
                    ),
                    HelpItemInfo(
                        Icons.Default.AutoAwesome,
                        "Persönlichkeit (Soul)",
                        "Hier definierst du den Vibe: Tonfall, Energie, Sprachstil. Diese Eigenschaften bestimmen, wie der Assistent klingt — nicht, was er sagt."
                    ),
                    HelpItemInfo(
                        Icons.Default.Memory,
                        "Gedächtnis",
                        "Persistente Fakten, die über alle Chats hinweg im Kopf bleiben. Bittest du den Assistenten im Chat, sich etwas zu merken, ergänzt er den Eintrag selbst (über einen versteckten <remember>-Tag, der aus der Antwort entfernt wird, bevor du sie siehst)."
                    ),
                    HelpItemInfo(
                        Icons.Default.FolderOpen,
                        "Dateien & Editierbarkeit",
                        "Konfiguration, Persönlichkeit und Gedächtnis liegen als Markdown-Dateien im Assistant-Ordner. Du kannst sie auch mit externen Editoren bearbeiten. „Aus Datei neu laden“ aktualisiert die Anzeige."
                    ),
                    HelpItemInfo(
                        Icons.Default.ToggleOn,
                        "Ein-/Ausschalten",
                        "Jeder der drei Bausteine kann einzeln aktiviert oder deaktiviert werden — praktisch, um z.B. mal ohne Persönlichkeit zu chatten oder das Gedächtnis temporär zu pausieren."
                    )
                )
            )

            // Parameter
            HelpSection(
                icon = Icons.Default.Tune,
                iconColor = Color(0xFFEF4444),
                title = "Modell-Parameter",
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.DeviceThermostat,
                        "Temperature (0–2)",
                        "Steuert die Kreativität der Antworten. Niedrige Werte (z.B. 0.2) liefern präzise, vorhersehbare Antworten. Hohe Werte (z.B. 1.2) machen das Modell kreativer."
                    ),
                    HelpItemInfo(
                        Icons.Default.FormatListNumbered,
                        "Top-K (1–200)",
                        "Begrenzt die Token-Auswahl auf die K wahrscheinlichsten nächsten Tokens."
                    ),
                    HelpItemInfo(
                        Icons.Default.Percent,
                        "Top-P (0–1)",
                        "Wählt Tokens aus dem kleinsten Set, dessen kumulative Wahrscheinlichkeit P überschreitet. Funktioniert ähnlich wie Top-K, aber adaptiv."
                    ),
                    HelpItemInfo(
                        Icons.Default.FilterList,
                        "Min-P (0–1)",
                        "Schneidet Tokens unterhalb einer Mindestwahrscheinlichkeit ab. 0 = deaktiviert. Hilfreich gegen unwahrscheinliche, halluzinationsanfällige Tokens."
                    ),
                    HelpItemInfo(
                        Icons.Default.Repeat,
                        "Presence Penalty (0–2)",
                        "Bestraft Tokens, die schon vorgekommen sind — reduziert Endlosschleifen. Unsloth empfiehlt für Qwen3.6 oft 1.5."
                    )
                )
            )

            // Tastaturkürzel
            HelpSection(
                icon = Icons.Default.Keyboard,
                iconColor = Color(0xFF64748B),
                title = "Tastaturkürzel",
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.KeyboardTab,
                        "⌘ + Enter – Nachricht senden",
                        "Auf einer externen Tastatur kannst du mit ⌘+Enter (Befehl + Eingabe) Nachrichten direkt abschicken."
                    )
                )
            )

            // Tip Card
            TipCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpHeaderCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🦙", fontSize = 56.sp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Ollama Connect", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Chatte mit KI-Modellen auf deinem eigenen Server – schnell, privat und ohne Cloud. Version 2.0 (KMP) © 2026 Peter Betz",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

data class HelpItemInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

@Composable
fun HelpSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    items: List<HelpItemInfo>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.forEachIndexed { index, item ->
                    HelpRowView(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(start = 36.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HelpRowView(item: HelpItemInfo) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                Text(item.title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 90f else 0f)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = item.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(start = 36.dp, top = 8.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun TipCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Beste Ergebnisse erzielen", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Kombiniere einen präzisen System-Prompt mit einer niedrigen Temperature (0.1–0.4) für sachliche Aufgaben wie Coding oder Übersetzungen. Für kreative Texte erhöhe Temperature auf 0.8–1.2 und deaktiviere den System-Prompt.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
