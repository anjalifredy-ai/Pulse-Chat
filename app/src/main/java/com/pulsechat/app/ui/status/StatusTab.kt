package com.pulsechat.app.ui.status

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pulsechat.app.data.model.StatusItem
import com.pulsechat.app.data.model.StatusType
import com.pulsechat.app.ui.components.CircleAvatar
import com.pulsechat.app.ui.pulse.PulsePlayer
import com.pulsechat.app.ui.theme.PulseBlue
import com.pulsechat.app.ui.theme.PulsePurple
import java.text.SimpleDateFormat
import java.util.Locale

private val BgColors = listOf(
    "#5B8DEF", "#B14EFF", "#FF5C8A", "#FF8A3D", "#1DE9B6", "#0A0A0C"
)

@Composable
fun StatusTab(
    modifier: Modifier = Modifier,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val statuses by viewModel.statuses.collectAsState()
    val myUid = viewModel.myUid
    val myStatuses = statuses.filter { it.userId == myUid }
    val others = statuses.filter { it.userId != myUid }.groupBy { it.userId }

    var showComposer by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(BgColors[0]) }
    var musicName by remember { mutableStateOf<String?>(null) }
    var viewerList by remember { mutableStateOf<List<StatusItem>?>(null) }
    var viewerIndex by remember { mutableIntStateOf(0) }
    var editItem by remember { mutableStateOf<StatusItem?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.postMediaStatus(it, StatusType.IMAGE) }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.postMediaStatus(it, StatusType.VIDEO) }
    }
    val musicLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            musicName = uri.lastPathSegment ?: "Music attached"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Status",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (myStatuses.isNotEmpty()) {
                                    viewerList = myStatuses
                                    viewerIndex = 0
                                } else {
                                    showComposer = true
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            CircleAvatar(
                                photoUrl = myStatuses.firstOrNull()?.mediaUrl?.takeIf {
                                    myStatuses.first().type == StatusType.IMAGE
                                },
                                name = "Me",
                                size = 56.dp
                            )
                            if (myStatuses.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(PulsePurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .border(2.5.dp, PulsePurple, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My status", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (myStatuses.isEmpty()) "Photo · Video · Text · Music"
                                else "${myStatuses.size} update · tap to view",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp
                            )
                        }
                        IconButton(onClick = { imageLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Image, "Photo", tint = Color.White.copy(alpha = 0.8f))
                        }
                        IconButton(onClick = { videoLauncher.launch("video/*") }) {
                            Icon(Icons.Default.Videocam, "Video", tint = Color.White.copy(alpha = 0.8f))
                        }
                        IconButton(onClick = { showComposer = true }) {
                            Icon(Icons.Default.Edit, "Text", tint = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                if (others.isNotEmpty()) {
                    item {
                        Text(
                            "Recent updates",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                items(others.entries.toList(), key = { it.key }) { (userId, list) ->
                    val first = list.first()
                    val preview = when {
                        first.type == StatusType.IMAGE -> "📷 Photo"
                        first.type == StatusType.VIDEO -> "🎬 Video"
                        !first.text.isNullOrBlank() -> first.text!!
                        else -> "Status"
                    }
                    val time = first.createdAt?.let {
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
                    } ?: ""

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewerList = list
                                viewerIndex = 0
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .border(2.5.dp, PulseBlue, CircleShape)
                                .padding(3.dp)
                        ) {
                            CircleAvatar(
                                photoUrl = first.mediaUrl?.takeIf { first.type == StatusType.IMAGE },
                                name = userId.take(2).uppercase(),
                                size = 50.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${list.size} update${if (list.size > 1) "s" else ""}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("$preview · $time", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        }
                    }
                }

                if (statuses.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No status yet", color = Color.White.copy(alpha = 0.6f))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add photo, video or text.\nDisappears after 24 hours.",
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showComposer = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = PulsePurple
        ) {
            Icon(Icons.Default.Add, "Add status", tint = Color.White)
        }
    }

    // Full edit page style composer
    if (showComposer || editItem != null) {
        val editing = editItem
        Dialog(
            onDismissRequest = {
                showComposer = false
                editItem = null
                musicName = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(selectedColor))
                        } catch (_: Exception) {
                            PulseBlue
                        }
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showComposer = false
                        editItem = null
                    }) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                    Text(
                        if (editing != null) "Edit status" else "New status",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        if (statusText.isNotBlank()) {
                            val finalText = if (musicName != null) {
                                "$statusText\n🎵 $musicName"
                            } else statusText
                            if (editing != null) {
                                viewModel.editTextStatus(editing.id, finalText.trim(), selectedColor)
                            } else {
                                viewModel.postQuickTextStatus(finalText.trim(), selectedColor)
                            }
                            statusText = ""
                            musicName = null
                            showComposer = false
                            editItem = null
                        }
                    }) {
                        Text("Post", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.weight(1f))
                OutlinedTextField(
                    value = statusText,
                    onValueChange = { statusText = it },
                    placeholder = { Text("Type a status…", color = Color.White.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(0.5f),
                        unfocusedBorderColor = Color.White.copy(0.3f)
                    )
                )
                Spacer(Modifier.height(16.dp))

                Text("Background", color = Color.White.copy(0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BgColors.forEach { hex ->
                        val c = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            PulseBlue
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(
                                    if (selectedColor == hex) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { imageLauncher.launch("image/*") },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(0.2f))
                        ) {
                            Icon(Icons.Default.Image, null, tint = Color.White)
                        }
                        Text("Photo", color = Color.White, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { videoLauncher.launch("video/*") },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(0.2f))
                        ) {
                            Icon(Icons.Default.Videocam, null, tint = Color.White)
                        }
                        Text("Video", color = Color.White, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { musicLauncher.launch("audio/*") },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(0.2f))
                        ) {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White)
                        }
                        Text(
                            musicName?.take(10) ?: "Music",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Viewer with real video play
    viewerList?.let { list ->
        if (list.isEmpty()) return@let
        val item = list.getOrNull(viewerIndex) ?: list.first()
        LaunchedEffect(item.id) { viewModel.markViewed(item.id) }

        Dialog(
            onDismissRequest = { viewerList = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (item.type) {
                            StatusType.TEXT -> {
                                val hex = item.backgroundColor ?: "#5B8DEF"
                                try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    PulseBlue
                                }
                            }
                            else -> Color.Black
                        }
                    )
            ) {
                when (item.type) {
                    StatusType.IMAGE -> {
                        AsyncImage(
                            model = item.mediaUrl,
                            contentDescription = "Status photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    if (viewerIndex < list.lastIndex) viewerIndex++
                                    else viewerList = null
                                }
                        )
                    }
                    StatusType.VIDEO -> {
                        if (!item.mediaUrl.isNullOrBlank()) {
                            PulsePlayer(
                                streamUrl = item.mediaUrl!!,
                                autoPlay = true,
                                showControls = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    StatusType.TEXT -> {
                        Text(
                            text = item.text.orEmpty(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                                .clickable {
                                    if (viewerIndex < list.lastIndex) viewerIndex++
                                    else viewerList = null
                                }
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        list.forEachIndexed { i, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (i <= viewerIndex) Color.White
                                        else Color.White.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (item.userId == myUid) "My status" else "Status",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.userId == myUid) {
                            if (item.type == StatusType.TEXT) {
                                IconButton(onClick = {
                                    statusText = item.text.orEmpty()
                                    selectedColor = item.backgroundColor ?: BgColors[0]
                                    editItem = item
                                    viewerList = null
                                }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                                }
                            }
                            IconButton(onClick = {
                                viewModel.deleteStatus(item.id)
                                val remaining = list.filter { it.id != item.id }
                                if (remaining.isEmpty()) viewerList = null
                                else {
                                    viewerList = remaining
                                    viewerIndex = viewerIndex.coerceAtMost(remaining.lastIndex)
                                }
                            }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { viewerList = null }) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
