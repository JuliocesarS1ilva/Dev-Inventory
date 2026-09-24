package com.devinventory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Tool(
    val id: String = "",
    val name: String = "",
    val type: String = "Software",
    val category: String = "",
    val version: String = "",
    val license: String = "",
    val platform: String = "",
    val status: String = "Em uso",
    val notes: String = ""
)

class ToolRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("ferramentas")

    fun listen(onChange: (List<Tool>) -> Unit, onError: (String) -> Unit): ListenerRegistration {
        return collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "Erro ao consultar o Firestore.")
                return@addSnapshotListener
            }
            val tools = snapshot?.documents?.map { d ->
                Tool(
                    id = d.id,
                    name = d.getString("name") ?: "",
                    type = d.getString("type") ?: "Software",
                    category = d.getString("category") ?: "",
                    version = d.getString("version") ?: "",
                    license = d.getString("license") ?: "",
                    platform = d.getString("platform") ?: "",
                    status = d.getString("status") ?: "Em uso",
                    notes = d.getString("notes") ?: ""
                )
            } ?: emptyList()
            onChange(tools)
        }
    }

    fun save(tool: Tool, onDone: () -> Unit, onError: (String) -> Unit) {
        val data = mapOf(
            "name" to tool.name,
            "type" to tool.type,
            "category" to tool.category,
            "version" to tool.version,
            "license" to tool.license,
            "platform" to tool.platform,
            "status" to tool.status,
            "notes" to tool.notes
        )
        val task = if (tool.id.isBlank()) collection.add(data) else collection.document(tool.id).set(data)
        task.addOnSuccessListener { onDone() }
            .addOnFailureListener { onError(it.message ?: "Não foi possível salvar.") }
    }

    fun delete(id: String, onDone: () -> Unit, onError: (String) -> Unit) {
        collection.document(id).delete()
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onError(it.message ?: "Não foi possível excluir.") }
    }

    fun seed(onDone: () -> Unit, onError: (String) -> Unit) {
        val examples = listOf(
            Tool(name="Android Studio", type="IDE", category="Desenvolvimento Mobile", version="Ladybug", license="Gratuita", platform="Windows / Linux / macOS", status="Em uso", notes="IDE principal para projetos Android."),
            Tool(name="Git", type="Software", category="Versionamento", version="2.x", license="Open Source", platform="Windows / Linux / macOS", status="Em uso", notes="Controle de versão dos projetos."),
            Tool(name="GitLens", type="Plugin", category="Produtividade", version="Latest", license="Freemium", platform="VS Code", status="Em uso", notes="Recursos avançados para Git."),
            Tool(name="Jetpack Compose", type="Biblioteca", category="Interface Android", version="1.x", license="Open Source", platform="Android", status="Em uso", notes="Toolkit declarativo para UI.")
        )
        val batch = db.batch()
        examples.forEach { tool ->
            val ref = collection.document()
            batch.set(ref, mapOf(
                "name" to tool.name, "type" to tool.type, "category" to tool.category,
                "version" to tool.version, "license" to tool.license,
                "platform" to tool.platform, "status" to tool.status, "notes" to tool.notes
            ))
        }
        batch.commit().addOnSuccessListener { onDone() }
            .addOnFailureListener { onError(it.message ?: "Não foi possível inserir exemplos.") }
    }
}

class ToolViewModel : ViewModel() {
    private val repository = try { ToolRepository() } catch (e: Exception) { null }
    private val _tools = MutableStateFlow<List<Tool>>(emptyList())
    val tools: StateFlow<List<Tool>> = _tools.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var registration: ListenerRegistration? = null

    init {
        if (repository == null) {
            _message.value = "Firebase ainda não configurado. Adicione o google-services.json."
        } else {
            try {
                registration = repository.listen(
                    { _tools.value = it },
                    { _message.value = it }
                )
            } catch (e: Exception) {
                _message.value = "Firebase não inicializado. Configure o google-services.json."
            }
        }
    }

    fun save(tool: Tool) {
        repository?.save(tool, { _message.value = "Ferramenta salva com sucesso." }, { _message.value = it })
            ?: run { _message.value = "Configure o Firebase antes de salvar." }
    }

    fun delete(tool: Tool) {
        repository?.delete(tool.id, { _message.value = "Ferramenta excluída." }, { _message.value = it })
            ?: run { _message.value = "Configure o Firebase antes de excluir." }
    }

    fun seed() {
        repository?.seed({ _message.value = "Exemplos adicionados ao Firestore." }, { _message.value = it })
            ?: run { _message.value = "Configure o Firebase antes de inserir exemplos." }
    }

    fun clearMessage() { _message.value = null }

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }
}

private val Background = Color(0xFF0B0D10)
private val SurfaceDark = Color(0xFF15181D)
private val CardDark = Color(0xFF1B2027)
private val Accent = Color(0xFF7CFF6B)
private val TextMain = Color(0xFFF2F4F7)
private val TextMuted = Color(0xFF9BA3AF)

@Composable
fun DevInventoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Color.Black,
            background = Background,
            surface = SurfaceDark,
            surfaceVariant = CardDark,
            onBackground = TextMain,
            onSurface = TextMain,
            onSurfaceVariant = TextMuted
        ),
        content = content
    )
}

@Composable
fun App() {
    val vm: ToolViewModel = viewModel()
    val tools by vm.tools.collectAsState()
    val message by vm.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Tool?>(null) }
    var search by remember { mutableStateOf("") }

    val filtered = tools.filter {
        it.name.contains(search, true) ||
        it.type.contains(search, true) ||
        it.category.contains(search, true)
    }

    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showForm = true },
                containerColor = Accent,
                contentColor = Color.Black
            ) { Icon(Icons.Default.Add, "Adicionar") }
        }
    ) { padding ->
        if (showForm) {
            ToolFormScreen(
                initial = editing,
                onBack = { showForm = false },
                onSave = { vm.save(it); showForm = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = Accent, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("DEV INVENTORY", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Inventário de ferramentas", color = TextMuted)
                    }
                }

                Spacer(Modifier.height(22.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar ferramenta...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("TOTAL", tools.size.toString(), Modifier.weight(1f))
                    StatCard("EM USO", tools.count { it.status == "Em uso" }.toString(), Modifier.weight(1f))
                    StatCard("TIPOS", tools.map { it.type }.distinct().size.toString(), Modifier.weight(1f))
                }

                Spacer(Modifier.height(18.dp))

                if (tools.isEmpty()) {
                    EmptyState(onSeed = vm::seed)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filtered, key = { it.id }) { tool ->
                            ToolCard(
                                tool = tool,
                                onEdit = { editing = tool; showForm = true },
                                onDelete = { vm.delete(tool) }
                            )
                        }
                    }
                }
            }
        }

        if (message != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = vm::clearMessage) { Text("OK") } }
            ) { Text(message!!) }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Accent)
        }
    }
}

@Composable
fun EmptyState(onSeed: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 45.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Settings, null, tint = TextMuted, modifier = Modifier.size(50.dp))
        Spacer(Modifier.height(12.dp))
        Text("Nenhuma ferramenta cadastrada", fontWeight = FontWeight.SemiBold)
        Text("Cadastre sua primeira ferramenta ou carregue exemplos.", color = TextMuted)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onSeed) { Text("Carregar exemplos") }
    }
}

@Composable
fun ToolCard(tool: Tool, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tool.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${tool.type} • ${tool.category}", color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar", tint = Accent) }
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Excluir") }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(tool.license) })
                AssistChip(onClick = {}, label = { Text(tool.status) })
            }
            if (tool.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(tool.notes, color = TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir ferramenta?") },
            text = { Text("A ferramenta \"${tool.name}\" será removida do Firestore.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolFormScreen(initial: Tool?, onBack: () -> Unit, onSave: (Tool) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "Software") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var version by remember { mutableStateOf(initial?.version ?: "") }
    var license by remember { mutableStateOf(initial?.license ?: "") }
    var platform by remember { mutableStateOf(initial?.platform ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: "Em uso") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val types = listOf("Software", "IDE", "Plugin", "Biblioteca")
    val statuses = listOf("Em uso", "Teste", "Descontinuado")

    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar") }
            Text(
                if (initial == null) "Nova ferramenta" else "Editar ferramenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                FormField("Nome", name, { name = it }, "Ex.: Android Studio")
                Spacer(Modifier.height(12.dp))
                DropdownField("Tipo", type, types) { type = it }
                Spacer(Modifier.height(12.dp))
                FormField("Categoria", category, { category = it }, "Ex.: Desenvolvimento Mobile")
                Spacer(Modifier.height(12.dp))
                FormField("Versão", version, { version = it }, "Ex.: 2025.1")
                Spacer(Modifier.height(12.dp))
                FormField("Licença", license, { license = it }, "Ex.: Open Source")
                Spacer(Modifier.height(12.dp))
                FormField("Plataforma", platform, { platform = it }, "Ex.: Windows / Linux / macOS")
                Spacer(Modifier.height(12.dp))
                DropdownField("Status", status, statuses) { status = it }
                Spacer(Modifier.height(12.dp))
                FormField("Observações", notes, { notes = it }, "Informações adicionais", minLines = 3)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(Tool(initial?.id ?: "", name, type, category, version, license, platform, status, notes))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)
                ) {
                    Text(if (initial == null) "CADASTRAR NO FIRESTORE" else "SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, value: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DevInventoryTheme { App() }
        }
    }
}
