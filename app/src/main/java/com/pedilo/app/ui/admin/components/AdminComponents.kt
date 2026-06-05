package com.pedilo.app.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.pedilo.app.ui.admin.AdminEntry
import com.pedilo.app.ui.admin.AdminRoot
import com.pedilo.app.ui.publicuser.PediloLine
import com.pedilo.app.ui.publicuser.PediloMuted
import com.pedilo.app.ui.publicuser.PediloOrange
import com.pedilo.app.ui.publicuser.PediloPanel
import com.pedilo.app.ui.publicuser.PediloPanelSoft
import com.pedilo.app.ui.publicuser.PediloText
import com.pedilo.app.ui.publicuser.PediloCardBrush
import com.pedilo.app.ui.publicuser.pediloCardDepth

@Composable
fun AdminHeader(
    title: String,
    eyebrow: String,
    summary: String,
    onSignOut: () -> Unit,
    showSignOut: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pediloCardDepth(RoundedCornerShape(15.dp))
            .background(PediloCardBrush, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(eyebrow, color = PediloOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(title, color = PediloText, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(summary, color = PediloMuted, fontSize = 13.sp, lineHeight = 18.sp)
        if (showSignOut) {
            val interactionSource = remember { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (pressed) 0.988f else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (pressed) PediloPanel else PediloPanelSoft, RoundedCornerShape(12.dp))
                    .border(1.dp, if (pressed) PediloOrange.copy(alpha = 0.72f) else PediloLine, RoundedCornerShape(12.dp))
                    .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onSignOut)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = PediloText)
                Text("Cerrar sesión", color = PediloText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminEntryCard(entry: AdminEntry, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.988f else 1f)
            .pediloCardDepth(RoundedCornerShape(15.dp))
            .background(PediloCardBrush, RoundedCornerShape(15.dp))
            .border(1.dp, if (pressed) PediloOrange.copy(alpha = 0.72f) else PediloLine, RoundedCornerShape(15.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(entry.title, color = PediloText, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(entry.note, color = PediloMuted, fontSize = 13.sp, lineHeight = 17.sp)
    }
}

@Composable
fun AdminInfoPanel(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(PediloPanelSoft, RoundedCornerShape(15.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(15.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = PediloText, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(text, color = PediloMuted, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun AdminBottomBar(
    current: AdminRoot,
    onOperation: () -> Unit,
    onConfiguration: () -> Unit,
    onRoleAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(Brush.verticalGradient(listOf(PediloPanelSoft.copy(alpha = 0.96f), PediloPanel)), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .border(1.dp, PediloLine, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AdminBottomItem(Icons.Outlined.Dashboard, "Operación", selected = current == AdminRoot.Operation, onClick = onOperation, modifier = Modifier.weight(1f))
        AdminBottomItem(Icons.Outlined.Settings, "Configuración", selected = current == AdminRoot.Configuration, onClick = onConfiguration, modifier = Modifier.weight(1f))
        AdminBottomItem(Icons.Outlined.AdminPanelSettings, "Alta de roles", selected = current == AdminRoot.RoleAccess, onClick = onRoleAccess, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AdminBottomItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Column(
        modifier = modifier
            .height(68.dp)
            .scale(if (pressed) 0.98f else 1f)
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected || pressed) PediloOrange.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(15.dp))
            .border(1.dp, if (selected || pressed) PediloOrange.copy(alpha = 0.62f) else PediloLine.copy(alpha = 0.45f), RoundedCornerShape(15.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) PediloOrange else PediloMuted,
        )
        Text(
            label,
            color = if (selected) PediloOrange else PediloMuted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
