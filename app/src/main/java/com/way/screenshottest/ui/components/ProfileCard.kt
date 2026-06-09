package com.way.screenshottest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.way.screenshottest.ui.theme.ScreenShotTestTheme
import com.way.screenshottest.ui.theme.StatusBusy
import com.way.screenshottest.ui.theme.StatusOffline
import com.way.screenshottest.ui.theme.StatusOnline

enum class UserStatus { Online, Busy, Offline }

data class ProfileData(
    val name: String,
    val role: String,
    val status: UserStatus,
    val followers: Int,
    val following: Int,
)

@Composable
fun ProfileCard(
    profile: ProfileData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = profile.name.take(2).uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(statusColor(profile.status))
                        .align(Alignment.BottomEnd),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = profile.role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusChip(status = profile.status)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatColumn(label = "Followers", count = profile.followers)
                StatColumn(label = "Following", count = profile.following)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Follow")
            }
        }
    }
}

@Composable
private fun StatusChip(status: UserStatus) {
    val (color, label) = when (status) {
        UserStatus.Online -> StatusOnline to "Online"
        UserStatus.Busy -> StatusBusy to "Busy"
        UserStatus.Offline -> StatusOffline to "Offline"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatColumn(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun statusColor(status: UserStatus): Color = when (status) {
    UserStatus.Online -> StatusOnline
    UserStatus.Busy -> StatusBusy
    UserStatus.Offline -> StatusOffline
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardOnlinePreview() {
    ScreenShotTestTheme {
        ProfileCard(
            profile = ProfileData(
                name = "Алибек Т.",
                role = "Android Developer",
                status = UserStatus.Online,
                followers = 342,
                following = 128,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardOfflinePreview() {
    ScreenShotTestTheme {
        ProfileCard(
            profile = ProfileData(
                name = "Алибек Т.",
                role = "Android Developer",
                status = UserStatus.Offline,
                followers = 342,
                following = 128,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
