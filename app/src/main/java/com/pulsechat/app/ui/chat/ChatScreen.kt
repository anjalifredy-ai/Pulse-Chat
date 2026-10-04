package com.pulsechat.app.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.data.model.Message
import com.pulsechat.app.data.model.MessageType
import com.pulsechat.app.media.VoiceRecorder
import com.pulsechat.app.ui.components.CircleAvatar
import com.pulsechat.app.ui.theme.ChatBackground
import com.pulsechat.app.ui.theme.ChatBar
import com.pulsechat.app.ui.theme.ChatInput
import com.pulsechat.app.ui.theme.PulseBlue
import com.pulsechat.app.ui.theme.PulsePurple
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onOpenContact: (String) -> Unit,
    onVoiceCall: (String) -> Unit = {},
    onVideoCall: (String) -> Unit = {},
    onOpenDocument: (url: String, name: String) -> Unit = { _, _ -> },
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(conversationId) { viewModel.load(conversationId) }

    val messages by viewModel.messages.collectAsState()
    val currentUid by viewModel.currentUid.collectAsState()
    val title by viewModel.title.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val uploading by viewModel.uploading.collectAsState()
    val startedCallId by viewModel.startedCallId.collectAsState()
    var input by remember { mutableStateOf("") }
    var showAttach by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var recording by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    val voiceRecorder = remember { VoiceRecorder(context) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    LaunchedEffect(startedCallId) {
        startedCallId?.let { id ->
            onVoiceCall(id)
            viewModel.clearStartedCall()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, MessageType.IMAGE) }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, MessageType.VIDEO) }
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = it.lastPathSegment ?: "document.pdf"
            viewModel.sendMedia(it, MessageType.DOCUMENT, name)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { viewModel.sendMedia(it, MessageType.IMAGE) }
    }
    val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val nameIdx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIdx = c.getColumnIndex(ContactsContract.Contacts._ID)
                    val name = if (nameIdx >= 0) c.getString(nameIdx) else "Contact"
                    val contactId = if (idIdx >= 0) c.getString(idIdx) else null
                    var phone = ""
                    if (contactId != null) {
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?", arrayOf(contactId), null
                        )?.use { p ->
                            if (p.moveToFirst()) {
                                val pIdx = p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (pIdx >= 0) phone = p.getString(pIdx)
                            }
                        }
                    }
                    viewModel.sendContact(name ?: "Contact", phone)
                }
            }
        } catch (_: Exception) { }
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { g ->
        if (g) sendCurrentLocation(context, viewModel)
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val callPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    fun launchCamera() {
        val file = File(context.cacheDir, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    Scaffold(
        containerColor = ChatBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatBar,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircleAvatar(photoUrl = photoUrl, name = title, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                            Text(
                                if (title.contains("You", true)) "Message yourself" else "online",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        callPermission.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                        viewModel.startVideoCall()
                    }) { Icon(Icons.Default.Videocam, contentDescription = "Video") }
                    IconButton(onClick = {
                        callPermission.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        viewModel.startVoiceCall()
                    }) { Icon(Icons.Default.Call, contentDescription = "Call") }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(ChatBar)) {
                if (uploading || recording) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uploading) {
                            CircularProgressIndicator(
                                color = PulseBlue,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (recording) "Recording… release to send" else "Uploading…",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
                if (showAttach) {
                    AttachPanel(
                        onGallery = { showAttach = false; galleryLauncher.launch("image/*") },
                        onVideo = { showAttach = false; videoLauncher.launch("video/*") },
                        onCamera = { showAttach = false; launchCamera() },
                        onDocument = {
                            showAttach = false
                            documentLauncher.launch(arrayOf("application/pdf", "*/*"))
                        },
                        onLocation = {
                            showAttach = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                            ) sendCurrentLocation(context, viewModel)
                            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        onContact = { showAttach = false; contactLauncher.launch(null) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(ChatInput),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.EmojiEmotions, null, tint = Color.White.copy(alpha = 0.55f))
                        }
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message", color = Color.White.copy(alpha = 0.35f)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = PulseBlue
                            )
                        )
                        IconButton(onClick = { showAttach = !showAttach }) {
                            Icon(Icons.Default.AttachFile, null, tint = Color.White.copy(alpha = 0.55f))
                        }
                        IconButton(onClick = { launchCamera() }) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(alpha = 0.55f))
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (recording) Color(0xFFFF4D6A) else PulsePurple)
                            .then(
                                if (input.isBlank()) {
                                    Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                if (ContextCompat.checkSelfPermission(
                                                        context, Manifest.permission.RECORD_AUDIO
                                                    ) != PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                                                    return@detectTapGestures
                                                }
                                                try {
                                                    voiceRecorder.start()
                                                    recording = true
                                                    tryAwaitRelease()
                                                } finally {
                                                    recording = false
                                                    val result = voiceRecorder.stop()
                                                    if (result != null) {
                                                        viewModel.sendVoiceFile(result.first, result.second)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier.clickable {
                                        viewModel.sendMessage(input.trim())
                                        input = ""
                                        showAttach = false
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (input.isBlank()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ChatBackground)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    isMine = message.senderId == currentUid,
                    onOpenDocument = onOpenDocument,
                    onLongPress = { selectedMessage = it }
                )
            }
        }
    }

    // WhatsApp-style delete dialog on long-press
    selectedMessage?.let { msg ->
        val isMine = msg.senderId == currentUid
        AlertDialog(
            onDismissRequest = { selectedMessage = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF4D6A)) },
            title = { Text("Delete message?") },
            text = {
                Text(
                    if (isMine) "Choose how to delete this message."
                    else "This will remove the message from your chat only."
                )
            },
            confirmButton = {
                Column {
                    if (isMine && !msg.deletedForEveryone) {
                        TextButton(onClick = {
                            viewModel.deleteForEveryone(msg.id)
                            selectedMessage = null
                        }) {
                            Text("Delete for everyone", color = Color(0xFFFF4D6A))
                        }
                    }
                    TextButton(onClick = {
                        viewModel.deleteForMe(msg.id)
                        selectedMessage = null
                    }) {
                        Text("Delete for me", color = Color(0xFFFF4D6A))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMessage = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun sendCurrentLocation(context: android.content.Context, viewModel: ChatViewModel) {
    try {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val loc: Location? = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (_: SecurityException) {
            null
        }
        if (loc != null) viewModel.sendLocation(loc.latitude, loc.longitude)
        else viewModel.sendMessage("📍 Location unavailable — enable GPS")
    } catch (_: Exception) {
        viewModel.sendMessage("📍 Location error")
    }
}

@Composable
private fun AttachPanel(
    onGallery: () -> Unit,
    onVideo: () -> Unit,
    onCamera: () -> Unit,
    onDocument: () -> Unit,
    onLocation: () -> Unit,
    onContact: () -> Unit
) {
    val items = listOf(
        Triple(Icons.Default.Image, "Gallery", onGallery to Color(0xFFB14EFF)),
        Triple(Icons.Default.CameraAlt, "Camera", onCamera to Color(0xFFFF5C8A)),
        Triple(Icons.Default.LocationOn, "Location", onLocation to Color(0xFF5B8DEF)),
        Triple(Icons.Default.Person, "Contact", onContact to Color(0xFF7AB8FF)),
        Triple(Icons.Default.Description, "Document", onDocument to Color(0xFFB14EFF)),
        Triple(Icons.Default.Videocam, "Video", onVideo to Color(0xFFFF8A3D))
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (icon, label, pair) ->
            val (onClick, color) = pair
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}
