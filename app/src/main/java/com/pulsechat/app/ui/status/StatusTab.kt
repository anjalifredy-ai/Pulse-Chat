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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.pulsechat.app.ui.theme.PulseBlue
import com.pulsechat.app.ui.theme.PulsePurple
import java.text.SimpleDateFormat
import java.util.Locale

private val BgColors = listOf(
    "#5B8DEF", "#B14EFF", "#FF5C8A", "#FF8A3D", "#1DE9B6", "#0A0A0C"
)

@OptIn(ExperimentalMaterial3Api::class)
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
    var viewerList by remember { mutableStateOf<List<StatusItem>?>(null) }
    var viewerIndex by remember { mutableIntStateOf(0) }
    var editItem by remember { mutableStateOf<StatusItem?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.postMediaStatus(it, StatusType.IMAGE) }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.postMediaStatus(it, StatusType.VIDEO) }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0A0A0C))) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Status", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121218))
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // My status row
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
                                if (myStatuses.isEmpty()) "Tap to add photo, video or text"
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
                                "Add a photo, video or text status.\nIt disappears after 24 hours.",
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

    // Text / edit composer
    if (showComposer || editItem != null) {
        val editing = editItem
        AlertDialog(
            onDismissRequest = {
                showComposer = false
                editItem = null
            },
            title = { Text(if (editing != null) "Edit status" else "Text status") },
            text = {
                Column {
                    OutlinedTextField(
                        value = if (editing != null) statusText else statusText,
                        onValueChange = { statusText = it },
                        placeholder = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Background", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BgColors.forEach { hex ->
                            val c = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (_: Exception) {
                                PulseBlue
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (statusText.isNotBlank()) {
                        if (editing != null) {
                            viewModel.editTextStatus(editing.id, statusText.trim(), selectedColor)
                        } else {
                            viewModel.postQuickTextStatus(statusText.trim(), selectedColor)
                        }
                        statusText = ""
                        showComposer = false
                        editItem = null
                    }
                }) { Text(if (editing != null) "Save" else "Post") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showComposer = false
                    editItem = null
                }) { Text("Cancel") }
            }
        )
    }

    // Full-screen status viewer
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
                    .clickable {
                        if (viewerIndex < list.lastIndex) viewerIndex++
                        else viewerList = null
                    }
            ) {
                when (item.type) {
                    StatusType.IMAGE -> {
                        AsyncImage(
                            model = item.mediaUrl,
                            contentDescription = "Status photo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    StatusType.VIDEO -> {
                        // Thumbnail + label (full video player can be added with ExoPlayer later)
                        AsyncImage(
                            model = item.mediaUrl,
                            contentDescription = "Status video",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            "▶ Video status",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
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
                        )
                    }
                }

                // Top bar: progress dots + close + edit/delete for own
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

                if (!item.text.isNullOrBlank() && item.type != StatusType.TEXT) {
                    Text(
                        item.text!!,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                            .background(Color.Black.copy(0.35f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
