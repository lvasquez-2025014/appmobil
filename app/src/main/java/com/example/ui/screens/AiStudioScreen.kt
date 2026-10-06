package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AiStudioTab
import com.example.ui.viewmodel.EspacioUiState
import com.example.ui.viewmodel.EspacioViewModel

@Composable
fun AiStudioScreen(
    uiState: EspacioUiState,
    viewModel: EspacioViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_studio_screen")
    ) {
        // Tab Selector Row
        ScrollableTabRow(
            selectedTabIndex = uiState.aiSelectedTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            AiStudioTab.values().forEach { tab ->
                Tab(
                    selected = uiState.aiSelectedTab == tab,
                    onClick = { viewModel.setAiStudioTab(tab) },
                    text = {
                        Text(
                            text = tab.label,
                            fontWeight = if (uiState.aiSelectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("ai_tab_${tab.name}")
                )
            }
        }

        when (uiState.aiSelectedTab) {
            AiStudioTab.CHAT -> AiChatSection(uiState = uiState, viewModel = viewModel)
            AiStudioTab.SEARCH_MAPS -> AiSearchMapsSection(uiState = uiState, viewModel = viewModel)
            AiStudioTab.IMAGE_VIDEO -> AiImageVideoSection(uiState = uiState, viewModel = viewModel)
            AiStudioTab.MUSIC_VOICE -> AiMusicVoiceSection(uiState = uiState, viewModel = viewModel)
        }
    }
}

// 1. CHATBOT MULTI-TURNO
@Composable
fun AiChatSection(
    uiState: EspacioUiState,
    viewModel: EspacioViewModel
) {
    var inputText by remember { mutableStateOf("") }
    val models = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite")
    val roles = listOf("Asistente General", "Programador Experto", "Coach de Productividad", "Diseñador Creativo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Model Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            item { Text("Modelo:", style = MaterialTheme.typography.labelMedium) }
            items(models) { m ->
                FilterChip(
                    selected = uiState.aiChatSelectedModel == m,
                    onClick = { viewModel.setAiChatModel(m) },
                    label = { Text(if (m.contains("pro")) "Pro ($m)" else if (m.contains("lite")) "Lite ($m)" else "Flash ($m)") },
                    modifier = Modifier.testTag("model_chip_$m")
                )
            }
        }

        // Role Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            item { Text("Rol:", style = MaterialTheme.typography.labelMedium) }
            items(roles) { r ->
                FilterChip(
                    selected = uiState.aiChatSelectedRole == r,
                    onClick = { viewModel.setAiChatRole(r) },
                    label = { Text(r) },
                    modifier = Modifier.testTag("role_chip_$r")
                )
            }
        }

        // Scrollable Message Thread
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.aiChatMessages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "Tú" else "Gemini (${uiState.aiChatSelectedModel})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (uiState.isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini está pensando...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Input Field and Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Escribe un mensaje a Gemini...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendAiChatMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = !uiState.isAiLoading && inputText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}

// 2. BÚSQUEDA Y MAPAS GROUNDING
@Composable
fun AiSearchMapsSection(
    uiState: EspacioUiState,
    viewModel: EspacioViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var mapsQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("search_maps_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Grounding Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google Search Grounding",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelo: gemini-3.5-flash con herramienta de búsqueda en tiempo real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Consulta de actualidad o datos web") },
                        placeholder = { Text("Ej: Últimas noticias sobre misiones espaciales") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_grounding_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runSearchGrounding(searchQuery) },
                        enabled = !uiState.isAiLoading && searchQuery.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_grounding_btn")
                    ) {
                        Text("Buscar con Google Grounding")
                    }
                }
            }
        }

        // Maps Grounding Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google Maps Grounding",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelo: gemini-3.5-flash con herramienta de mapas para ubicaciones y lugares.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = mapsQuery,
                        onValueChange = { mapsQuery = it },
                        label = { Text("Lugar, dirección o recomendación") },
                        placeholder = { Text("Ej: Mejores cafeterías cerca del centro") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("maps_grounding_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runMapsGrounding(mapsQuery) },
                        enabled = !uiState.isAiLoading && mapsQuery.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("maps_grounding_btn")
                    ) {
                        Text("Consultar Google Maps Grounding")
                    }
                }
            }
        }

        // Result Card
        uiState.lastAiResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultado (${res.modelUsed}):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = res.text, style = MaterialTheme.typography.bodyMedium)

                        if (res.sources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fuentes & Citas:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            res.sources.take(4).forEach { s ->
                                Text(text = "• $s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. IMÁGENES Y VEO 3 VIDEO
@Composable
fun AiImageVideoSection(
    uiState: EspacioUiState,
    viewModel: EspacioViewModel
) {
    var imagePrompt by remember { mutableStateOf("") }
    var videoPrompt by remember { mutableStateOf("") }
    var selectedVideoAspect by remember { mutableStateOf("16:9") } // 16:9 or 9:16
    var isImageToVideo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("image_video_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image Generation with gemini-3.1-flash-image-preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generar & Editar Imágenes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelo: gemini-3.1-flash-image-preview en alta resolución (1K).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = imagePrompt,
                        onValueChange = { imagePrompt = it },
                        label = { Text("Descripción de la imagen") },
                        placeholder = { Text("Ej: Astronauta flotando sobre una ciudad futurista de neón") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.generateAiImage(imagePrompt) },
                        enabled = !uiState.isAiLoading && imagePrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_image_btn")
                    ) {
                        Text("Crear Imagen con Gemini")
                    }
                }
            }
        }

        // Veo 3 Video Generation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generación de Video (Veo 3)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelo: veo-3.1-fast-generate-preview (Texto a Video & Animar Fotos).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Aspect ratio selector (16:9 or 9:16)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedVideoAspect == "16:9",
                            onClick = { selectedVideoAspect = "16:9" },
                            label = { Text("Horizontal 16:9") },
                            modifier = Modifier.testTag("aspect_16_9_chip")
                        )
                        FilterChip(
                            selected = selectedVideoAspect == "9:16",
                            onClick = { selectedVideoAspect = "9:16" },
                            label = { Text("Vertical 9:16 (Reel/Short)") },
                            modifier = Modifier.testTag("aspect_9_16_chip")
                        )
                    }

                    // Mode: Text-to-Video vs Image-to-Video
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isImageToVideo,
                            onClick = { isImageToVideo = false },
                            label = { Text("Texto a Video") }
                        )
                        FilterChip(
                            selected = isImageToVideo,
                            onClick = { isImageToVideo = true },
                            label = { Text("Animar Imagen a Video") }
                        )
                    }

                    OutlinedTextField(
                        value = videoPrompt,
                        onValueChange = { videoPrompt = it },
                        label = { Text(if (isImageToVideo) "Instrucciones de movimiento para la imagen" else "Prompt del video") },
                        placeholder = { Text("Ej: Cámara cinemática en dron sobre una montaña nevada al atardecer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (isImageToVideo) {
                                // Default sample frame base64
                                val dummyBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="
                                viewModel.animateImageToVideo(videoPrompt, dummyBase64, selectedVideoAspect)
                            } else {
                                viewModel.generateVeoVideo(videoPrompt, selectedVideoAspect)
                            }
                        },
                        enabled = !uiState.isAiLoading && videoPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_veo_video_btn")
                    ) {
                        Text(if (isImageToVideo) "Animar Imagen con Veo 3 ($selectedVideoAspect)" else "Generar Video con Veo 3 ($selectedVideoAspect)")
                    }
                }
            }
        }

        // Preview Output
        uiState.lastAiResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultado (${res.modelUsed}):",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = res.text)

                        // Render Base64 Image if present
                        if (!res.mediaUrl.isNullOrBlank() && res.mimeType == "image/png") {
                            val bitmap = remember(res.mediaUrl) {
                                runCatching {
                                    val bytes = Base64.decode(res.mediaUrl, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }.getOrNull()
                            }
                            bitmap?.let { b ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    bitmap = b.asImageBitmap(),
                                    contentDescription = "Imagen generada",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. MÚSICA, VOZ EN VIVO & TRANSCRIPCIÓN
@Composable
fun AiMusicVoiceSection(
    uiState: EspacioUiState,
    viewModel: EspacioViewModel
) {
    var musicPrompt by remember { mutableStateOf("") }
    var isFullTrack by remember { mutableStateOf(false) }
    var liveVoicePrompt by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("music_voice_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lyria Music Generation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generación de Música (Lyria)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelos: lyria-3-clip-preview (clip hasta 30s) o lyria-3-pro-preview (track completo).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isFullTrack,
                            onClick = { isFullTrack = false },
                            label = { Text("Clip Corto (lyria-3-clip-preview)") }
                        )
                        FilterChip(
                            selected = isFullTrack,
                            onClick = { isFullTrack = true },
                            label = { Text("Track Completo (lyria-3-pro-preview)") }
                        )
                    }

                    OutlinedTextField(
                        value = musicPrompt,
                        onValueChange = { musicPrompt = it },
                        label = { Text("Estilo musical, instrumentos o mood") },
                        placeholder = { Text("Ej: Melodía lofi relajante con piano suave y lluvia de fondo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.generateLyriaMusic(musicPrompt, isFullTrack) },
                        enabled = !uiState.isAiLoading && musicPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_music_btn")
                    ) {
                        Text(if (isFullTrack) "Crear Canción Completa" else "Crear Clip de 30s")
                    }
                }
            }
        }

        // Live Voice Conversations (gemini-3.8-live)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF0EA5E9))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Conversación de Voz en Vivo (Live API)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelo: gemini-3.8-live para interacción fluida de voz en tiempo real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = liveVoicePrompt,
                        onValueChange = { liveVoicePrompt = it },
                        label = { Text("Pregunta o tema a conversar por voz") },
                        placeholder = { Text("Ej: Conversemos sobre cómo estructurar mis hábitos este mes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_voice_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.startLiveVoiceConversation(liveVoicePrompt) },
                        enabled = !uiState.isAiLoading && liveVoicePrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_voice_btn")
                    ) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Sesión Live API")
                    }
                }
            }
        }

        // Audio Transcription (gemini-3.5-transcribe)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transcribir Audio a Texto",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Modelo: gemini-3.5-transcribe. Transcribe grabaciones de voz y las guarda automáticamente en tus Notas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            // Send sample speech audio base64
                            val dummySpeechBase64 = "UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA="
                            viewModel.transcribeAudioInput(dummySpeechBase64)
                        },
                        enabled = !uiState.isAiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transcribe_audio_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grabar y Transcribir Audio")
                    }
                }
            }
        }

        // Result Card
        uiState.lastAiResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultado (${res.modelUsed}):",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = res.text)
                    }
                }
            }
        }
    }
}
