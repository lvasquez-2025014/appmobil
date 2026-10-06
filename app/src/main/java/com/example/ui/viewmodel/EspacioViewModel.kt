package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiGenerationResult
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiAiService
import com.example.data.auth.AuthManager
import com.example.data.model.AppIdeaTemplate
import com.example.data.model.AppNavDestination
import com.example.data.model.ExpenseCategory
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.example.data.repository.AiCreationRecord
import com.example.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AiStudioTab(val label: String) {
    CHAT("💬 Chat Pro"),
    SEARCH_MAPS("🔍 Búsqueda & Mapas"),
    IMAGE_VIDEO("🎨 Imágenes & Veo 3"),
    MUSIC_VOICE("🎵 Música & Voz")
}

data class EspacioUiState(
    val currentDestination: AppNavDestination = AppNavDestination.HOME,
    val currentUser: FirebaseUser? = null,
    val isAuthLoading: Boolean = false,
    val tasks: List<TaskItem> = emptyList(),
    val taskFilterCategory: TaskCategory? = null,
    val taskFilterCompletedOnly: Boolean? = null,
    val habits: List<HabitItem> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val notes: List<NoteItem> = emptyList(),
    val noteSearchQuery: String = "",
    val templates: List<AppIdeaTemplate> = emptyList(),
    val selectedTemplate: AppIdeaTemplate? = null,
    val userAppIdeaName: String = "",
    val userAppIdeaCategory: String = "Productividad",
    val selectedAppFeatures: Set<String> = emptySet(),
    val snackbarMessage: String? = null,

    // AI Studio State
    val aiSelectedTab: AiStudioTab = AiStudioTab.CHAT,
    val aiChatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "model",
            text = "¡Hola! Soy tu asistente de IA multimodal impulsado por Gemini. ¿En qué te puedo ayudar hoy? Puedes pedirme redactar código, planear tareas, buscar información actualizada con Google Search, explorar lugares con Google Maps, o crear imágenes, videos y música."
        )
    ),
    val aiChatSelectedModel: String = "gemini-3.5-flash",
    val aiChatSelectedRole: String = "Asistente General",
    val isAiLoading: Boolean = false,
    val lastAiResult: AiGenerationResult? = null,
    val aiCreations: List<AiCreationRecord> = emptyList()
)

class EspacioViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager()
    private val firestoreRepo = FirestoreRepository(application)
    private val geminiService = GeminiAiService()

    private val _uiState = MutableStateFlow(EspacioUiState())
    val uiState: StateFlow<EspacioUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authManager.authStateFlow().collectLatest { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    observeUserData(user.uid)
                }
            }
        }
    }

    private fun observeUserData(userId: String) {
        viewModelScope.launch {
            firestoreRepo.observeTasks(userId).collectLatest { tasks ->
                if (tasks.isNotEmpty()) {
                    _uiState.update { it.copy(tasks = tasks) }
                }
            }
        }
        viewModelScope.launch {
            firestoreRepo.observeHabits(userId).collectLatest { habits ->
                if (habits.isNotEmpty()) {
                    _uiState.update { it.copy(habits = habits) }
                }
            }
        }
        viewModelScope.launch {
            firestoreRepo.observeTransactions(userId).collectLatest { txs ->
                if (txs.isNotEmpty()) {
                    _uiState.update { it.copy(transactions = txs) }
                }
            }
        }
        viewModelScope.launch {
            firestoreRepo.observeNotes(userId).collectLatest { notes ->
                if (notes.isNotEmpty()) {
                    _uiState.update { it.copy(notes = notes) }
                }
            }
        }
        viewModelScope.launch {
            firestoreRepo.observeAiCreations(userId).collectLatest { creations ->
                _uiState.update { it.copy(aiCreations = creations) }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true) }
            val result = authManager.signInWithGoogle(context)
            _uiState.update { it.copy(isAuthLoading = false) }
            result.onSuccess { user ->
                _uiState.update { it.copy(snackbarMessage = "Sesión iniciada como ${user.displayName ?: "Usuario"}") }
                // Sync current state to Firestore
                syncInitialDataToFirestore(user.uid)
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = "Error al iniciar sesión: ${e.message}") }
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _uiState.update { it.copy(snackbarMessage = "Sesión cerrada") }
    }

    private fun syncInitialDataToFirestore(userId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            state.tasks.forEach { firestoreRepo.saveTask(userId, it) }
            state.habits.forEach { firestoreRepo.saveHabit(userId, it) }
            state.transactions.forEach { firestoreRepo.saveTransaction(userId, it) }
            state.notes.forEach { firestoreRepo.saveNote(userId, it) }
        }
    }

    private fun loadInitialData() {
        val initialTasks = listOf(
            TaskItem(
                id = UUID.randomUUID().toString(),
                title = "Definir mi idea de aplicación móvil",
                category = TaskCategory.PROYECTOS,
                priority = Priority.HIGH,
                isCompleted = false,
                dateLabel = "Hoy"
            ),
            TaskItem(
                id = UUID.randomUUID().toString(),
                title = "Diseñar bocetos de pantallas clave",
                category = TaskCategory.TRABAJO,
                priority = Priority.MEDIUM,
                isCompleted = false,
                dateLabel = "Hoy"
            ),
            TaskItem(
                id = UUID.randomUUID().toString(),
                title = "Caminar 30 minutos al aire libre",
                category = TaskCategory.SALUD,
                priority = Priority.LOW,
                isCompleted = true,
                dateLabel = "Hoy"
            )
        )

        val initialHabits = listOf(
            HabitItem(
                id = UUID.randomUUID().toString(),
                name = "Tomar 2L de agua",
                iconName = "water_drop",
                completedDaysThisWeek = 5,
                targetDaysPerWeek = 7,
                completedToday = true,
                streak = 7,
                colorHex = 0xFF0EA5E9
            ),
            HabitItem(
                id = UUID.randomUUID().toString(),
                name = "Leer 20 páginas",
                iconName = "menu_book",
                completedDaysThisWeek = 4,
                targetDaysPerWeek = 5,
                completedToday = false,
                streak = 4,
                colorHex = 0xFF8B5CF6
            )
        )

        val initialTransactions = listOf(
            TransactionItem(
                id = UUID.randomUUID().toString(),
                title = "Salario / Ingreso del mes",
                amount = 1250.00,
                isIncome = true,
                category = ExpenseCategory.TRABAJO,
                dateLabel = "01 Oct"
            ),
            TransactionItem(
                id = UUID.randomUUID().toString(),
                title = "Supermercado semanal",
                amount = 78.50,
                isIncome = false,
                category = ExpenseCategory.COMIDA,
                dateLabel = "Hoy"
            )
        )

        val initialNotes = listOf(
            NoteItem(
                id = UUID.randomUUID().toString(),
                title = "💡 Ideas para mi nueva App",
                content = "Una app móvil debe ser rápida, limpia, con buena tipografía, transiciones fluidas y guardar todo al instante sin complicaciones.",
                colorHex = 0xFFFEF3C7,
                isPinned = true,
                dateLabel = "Hoy"
            )
        )

        val appTemplates = listOf(
            AppIdeaTemplate(
                id = "ecommerce",
                title = "Tienda Online / Catálogo Móvil",
                subtitle = "Venta de productos, carrito y compras",
                iconCategory = "shopping_bag",
                description = "Muestra productos organizados con fotos, carrito de compras, cálculo de envíos, cupones de descuento y confirmación por WhatsApp o pago digital.",
                features = listOf("Catálogo con filtros", "Carrito de compras", "Favoritos", "Pasarela de pago", "Notificaciones de ofertas"),
                audience = "Emprendedores, negocios locales, creadores de marcas",
                difficulty = "Media"
            ),
            AppIdeaTemplate(
                id = "fitness",
                title = "Fitness, Gym & Salud",
                subtitle = "Rutinas de ejercicio y seguimiento de progreso",
                iconCategory = "fitness_center",
                description = "Planificador de entrenamientos semanales, temporizador de descansos, contador de repeticiones y registro de peso corporal con gráficos.",
                features = listOf("Rutinas guiadas", "Temporizador HIIT/Tabata", "Historial de cargas", "Metas de agua y calorías"),
                audience = "Atletas, entusiastas del gym y principiantes",
                difficulty = "Baja-Media"
            ),
            AppIdeaTemplate(
                id = "custom",
                title = "Tu Propia Idea Personalizada",
                subtitle = "Diseña exactamente lo que tienes en mente",
                iconCategory = "auto_awesome",
                description = "Combina cualquier módulo o describe lo que imaginas. ¡Podemos programar la app móvil exacta que estás soñando!",
                features = listOf("Diseño a medida", "Componentes modernos M3", "Persistencia segura", "Lógica personalizada"),
                audience = "Cualquier idea original o necesidad específica",
                difficulty = "Adaptable"
            )
        )

        _uiState.update {
            it.copy(
                tasks = initialTasks,
                habits = initialHabits,
                transactions = initialTransactions,
                notes = initialNotes,
                templates = appTemplates,
                selectedTemplate = appTemplates[0],
                userAppIdeaName = "Mi Tienda Móvil",
                selectedAppFeatures = setOf("Catálogo con filtros", "Carrito de compras", "Favoritos")
            )
        }
    }

    fun navigateTo(destination: AppNavDestination) {
        _uiState.update { it.copy(currentDestination = destination) }
    }

    // TASKS
    fun toggleTask(taskId: String) {
        _uiState.update { state ->
            val updated = state.tasks.map {
                if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            state.copy(tasks = updated)
        }
        val user = _uiState.value.currentUser
        val task = _uiState.value.tasks.find { it.id == taskId }
        if (user != null && task != null) {
            viewModelScope.launch { firestoreRepo.saveTask(user.uid, task) }
        }
    }

    fun addTask(title: String, category: TaskCategory, priority: Priority) {
        if (title.isBlank()) return
        val newTask = TaskItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            category = category,
            priority = priority,
            isCompleted = false,
            dateLabel = "Hoy"
        )
        _uiState.update { state ->
            state.copy(
                tasks = listOf(newTask) + state.tasks,
                snackbarMessage = "Tarea agregada"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.saveTask(user.uid, newTask) }
        }
    }

    fun deleteTask(taskId: String) {
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.filterNot { it.id == taskId },
                snackbarMessage = "Tarea eliminada"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.deleteTask(user.uid, taskId) }
        }
    }

    fun setTaskCategoryFilter(category: TaskCategory?) {
        _uiState.update { it.copy(taskFilterCategory = category) }
    }

    fun setTaskCompletionFilter(completedOnly: Boolean?) {
        _uiState.update { it.copy(taskFilterCompletedOnly = completedOnly) }
    }

    // HABITS
    fun toggleHabitToday(habitId: String) {
        _uiState.update { state ->
            val updated = state.habits.map { habit ->
                if (habit.id == habitId) {
                    val willBeCompleted = !habit.completedToday
                    val newStreak = if (willBeCompleted) habit.streak + 1 else maxOf(0, habit.streak - 1)
                    val newDays = if (willBeCompleted) habit.completedDaysThisWeek + 1 else maxOf(0, habit.completedDaysThisWeek - 1)
                    habit.copy(
                        completedToday = willBeCompleted,
                        streak = newStreak,
                        completedDaysThisWeek = newDays
                    )
                } else habit
            }
            state.copy(habits = updated)
        }
        val user = _uiState.value.currentUser
        val habit = _uiState.value.habits.find { it.id == habitId }
        if (user != null && habit != null) {
            viewModelScope.launch { firestoreRepo.saveHabit(user.uid, habit) }
        }
    }

    fun addHabit(name: String, targetDays: Int, colorHex: Long) {
        if (name.isBlank()) return
        val newHabit = HabitItem(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            completedDaysThisWeek = 0,
            targetDaysPerWeek = targetDays,
            completedToday = false,
            streak = 0,
            colorHex = colorHex
        )
        _uiState.update { state ->
            state.copy(
                habits = state.habits + newHabit,
                snackbarMessage = "Hábito agregado"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.saveHabit(user.uid, newHabit) }
        }
    }

    // TRANSACTIONS
    fun addTransaction(title: String, amount: Double, isIncome: Boolean, category: ExpenseCategory) {
        if (title.isBlank() || amount <= 0.0) return
        val newTx = TransactionItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            amount = amount,
            isIncome = isIncome,
            category = category,
            dateLabel = "Hoy"
        )
        _uiState.update { state ->
            state.copy(
                transactions = listOf(newTx) + state.transactions,
                snackbarMessage = if (isIncome) "+$$amount ingresados" else "-$$amount registrado"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.saveTransaction(user.uid, newTx) }
        }
    }

    fun deleteTransaction(txId: String) {
        _uiState.update { state ->
            state.copy(
                transactions = state.transactions.filterNot { it.id == txId },
                snackbarMessage = "Movimiento eliminado"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.deleteTransaction(user.uid, txId) }
        }
    }

    // NOTES
    fun addNote(title: String, content: String, colorHex: Long) {
        if (title.isBlank() && content.isBlank()) return
        val newNote = NoteItem(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) "Nota sin título" else title.trim(),
            content = content.trim(),
            colorHex = colorHex,
            isPinned = false,
            dateLabel = "Hoy"
        )
        _uiState.update { state ->
            state.copy(
                notes = listOf(newNote) + state.notes,
                snackbarMessage = "Nota guardada"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.saveNote(user.uid, newNote) }
        }
    }

    fun toggleNotePin(noteId: String) {
        _uiState.update { state ->
            val updated = state.notes.map {
                if (it.id == noteId) it.copy(isPinned = !it.isPinned) else it
            }
            state.copy(notes = updated)
        }
        val user = _uiState.value.currentUser
        val note = _uiState.value.notes.find { it.id == noteId }
        if (user != null && note != null) {
            viewModelScope.launch { firestoreRepo.saveNote(user.uid, note) }
        }
    }

    fun deleteNote(noteId: String) {
        _uiState.update { state ->
            state.copy(
                notes = state.notes.filterNot { it.id == noteId },
                snackbarMessage = "Nota eliminada"
            )
        }
        val user = _uiState.value.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.deleteNote(user.uid, noteId) }
        }
    }

    fun setNoteSearchQuery(query: String) {
        _uiState.update { it.copy(noteSearchQuery = query) }
    }

    // APP CREATOR
    fun selectAppTemplate(template: AppIdeaTemplate) {
        _uiState.update {
            it.copy(
                selectedTemplate = template,
                userAppIdeaName = template.title,
                userAppIdeaCategory = template.subtitle,
                selectedAppFeatures = template.features.take(3).toSet()
            )
        }
    }

    fun toggleAppFeature(feature: String) {
        _uiState.update { state ->
            val current = state.selectedAppFeatures.toMutableSet()
            if (current.contains(feature)) current.remove(feature) else current.add(feature)
            state.copy(selectedAppFeatures = current)
        }
    }

    fun updateUserAppIdeaName(name: String) {
        _uiState.update { it.copy(userAppIdeaName = name) }
    }

    // ==========================================
    // GEMINI MULTIMODAL AI CAPABILITIES
    // ==========================================
    fun setAiStudioTab(tab: AiStudioTab) {
        _uiState.update { it.copy(aiSelectedTab = tab) }
    }

    fun setAiChatModel(model: String) {
        _uiState.update { it.copy(aiChatSelectedModel = model) }
    }

    fun setAiChatRole(role: String) {
        _uiState.update { it.copy(aiChatSelectedRole = role) }
    }

    fun sendAiChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(role = "user", text = userText.trim())
        val updatedHistory = _uiState.value.aiChatMessages + userMsg
        _uiState.update {
            it.copy(
                aiChatMessages = updatedHistory,
                isAiLoading = true
            )
        }

        viewModelScope.launch {
            val role = _uiState.value.aiChatSelectedRole
            val model = _uiState.value.aiChatSelectedModel
            val systemInstruction = when (role) {
                "Programador Experto" -> "Eres un ingeniero senior de software Android y Kotlin. Responde con código de calidad y explicaciones claras."
                "Coach de Productividad" -> "Eres un coach motivador enfocado en gestión del tiempo, hábitos y metas alcanzables."
                "Diseñador Creativo" -> "Eres un diseñador UX/UI experto en Material Design 3, paletas cromáticas y animaciones."
                else -> "Eres un asistente inteligente, servicial y profesional de la aplicación móvil Espacio."
            }

            val result = geminiService.sendChatMessage(
                history = updatedHistory,
                systemInstruction = systemInstruction,
                model = model
            )

            val replyMsg = if (result.success) {
                ChatMessage(role = "model", text = result.text)
            } else {
                ChatMessage(role = "model", text = "⚠️ Error: ${result.errorMessage ?: "No se pudo obtener respuesta."}")
            }

            _uiState.update {
                it.copy(
                    aiChatMessages = it.aiChatMessages + replyMsg,
                    isAiLoading = false,
                    lastAiResult = result
                )
            }

            saveAiCreationToFirestore("Chat ($model)", userText, result.text, model)
        }
    }

    fun runSearchGrounding(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.searchGrounding(query.trim())
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Información de Google Search obtenida" else "Error en búsqueda"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Google Search Grounding", query, result.text, "gemini-3.5-flash")
            }
        }
    }

    fun runMapsGrounding(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.mapsGrounding(query.trim())
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Datos de Google Maps obtenidos" else "Error en Maps Grounding"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Google Maps Grounding", query, result.text, "gemini-3.5-flash")
            }
        }
    }

    fun generateAiImage(prompt: String, aspectRatio: String = "1:1") {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.generateImage(prompt.trim(), aspectRatio)
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Imagen creada con gemini-3.1-flash-image-preview" else "Error generando imagen"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Imagen IA", prompt, result.text, "gemini-3.1-flash-image-preview")
            }
        }
    }

    fun generateVeoVideo(prompt: String, aspectRatio: String = "16:9") {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.generateVideo(prompt.trim(), aspectRatio)
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Video solicitado a Veo 3 (${aspectRatio})" else "Error con Veo 3"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Video Veo 3", prompt, result.text, "veo-3.1-fast-generate-preview")
            }
        }
    }

    fun animateImageToVideo(prompt: String, imageBase64: String, aspectRatio: String = "16:9") {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.animateImageToVideo(prompt.trim(), imageBase64, aspectRatio)
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Animación de imagen a video iniciada (${aspectRatio})" else "Error animando video"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Animación de Imagen a Video", prompt, result.text, "veo-3.1-fast-generate-preview")
            }
        }
    }

    fun generateLyriaMusic(prompt: String, isFullTrack: Boolean = false) {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.generateMusic(prompt.trim(), isFullTrack)
            val modelName = if (isFullTrack) "lyria-3-pro-preview" else "lyria-3-clip-preview"
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Música generada con $modelName" else "Error generando música"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Música Lyria", prompt, result.text, modelName)
            }
        }
    }

    fun startLiveVoiceConversation(prompt: String) {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.voiceConversation(prompt.trim())
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Respuesta Live API recibida" else "Error en Live API"
                )
            }
            if (result.success) {
                saveAiCreationToFirestore("Voz Live API", prompt, result.text, "gemini-3.8-live")
            }
        }
    }

    fun transcribeAudioInput(audioBase64: String) {
        _uiState.update { it.copy(isAiLoading = true) }
        viewModelScope.launch {
            val result = geminiService.transcribeAudio(audioBase64)
            _uiState.update {
                it.copy(
                    isAiLoading = false,
                    lastAiResult = result,
                    snackbarMessage = if (result.success) "Transcripción lista con gemini-3.5-transcribe" else "Error al transcribir"
                )
            }
            if (result.success && result.text.isNotBlank()) {
                // Also automatically offer saving as a quick note
                addNote("🎙️ Transcripción de Audio", result.text, 0xFFE0E7FF)
                saveAiCreationToFirestore("Transcripción", "Audio grabado", result.text, "gemini-3.5-transcribe")
            }
        }
    }

    private fun saveAiCreationToFirestore(type: String, prompt: String, result: String, model: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val record = AiCreationRecord(
                id = UUID.randomUUID().toString(),
                type = type,
                title = prompt.take(30),
                prompt = prompt,
                result = result,
                modelUsed = model,
                createdAtLabel = "Hoy"
            )
            firestoreRepo.saveAiCreation(user.uid, record)
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
