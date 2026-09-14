package africa.iag.trace.android.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import africa.iag.trace.android.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val IagCardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun iagTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.background,
    titleContentColor = MaterialTheme.colorScheme.onBackground,
    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
)

@Composable
fun IagMonogram(size: Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape((size.value * 0.28f).dp))
            .background(IagOrange),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "IAG",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.28f).sp,
        )
    }
}

@Composable
fun IagBrandLogo(modifier: Modifier = Modifier, height: Dp = 128.dp) {
    Image(
        painter = painterResource(R.drawable.iag_logo),
        contentDescription = "Inspire Africa Group",
        modifier = modifier.fillMaxWidth().height(height),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun IconWell(icon: ImageVector, color: Color = IagOrange, size: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun StatusChip(text: String) {
    val color = statusColor(text)
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
    )
}

@Composable
fun IagEmptyHint(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
fun IagSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
        ),
    )
}

data class WelcomeStat(val id: String, val label: String, val value: String)

@Composable
fun WelcomeCard(name: String, subtitle: String, stats: List<WelcomeStat>) {
    val onOrange = Color.White
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(IagCardShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${greetingLabel()}, $name",
                    color = onOrange,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = onOrange.copy(alpha = 0.82f), fontSize = 14.sp)
            }
            IagMonogram(36.dp)
        }
        if (stats.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(onOrange.copy(alpha = 0.14f))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                stats.forEachIndexed { index, stat ->
                    if (index > 0) {
                        Box(
                            Modifier
                                .padding(horizontal = 8.dp)
                                .width(1.dp)
                                .height(28.dp)
                                .background(onOrange.copy(alpha = 0.22f)),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stat.value, color = onOrange, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stat.label, color = onOrange.copy(alpha = 0.78f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DeskRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    status: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconWell(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (status != null) {
            Spacer(Modifier.width(8.dp))
            StatusChip(status)
        }
    }
}

@Composable
fun IagGroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = IagCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(content = content)
    }
}

@Composable
fun IagListRow(
    onClick: (() -> Unit)? = null,
    divider: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(16.dp),
        ) { content() }
        if (divider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            )
        }
    }
}
