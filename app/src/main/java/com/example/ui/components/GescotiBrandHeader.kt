package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GescotiBrandHeader(
    modifier: Modifier = Modifier,
    tenantName: String = "OPPE",
    paroisseName: String = "Paroisse Cœur Immaculé de Marie",
    showSubtitle: Boolean = true
) {
    Surface(
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emblème abstrait aux 4 couleurs emblématiques du logo (Bleu Ciel, Rose Fuchsia, Or Solaire, Rouge Vif)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GescotiMarianNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(GescotiSkyBlue))
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(GescotiMagentaPink))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(GescotiSunlitGold))
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(GescotiCrimsonRed))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Lettres GESCOTI aux couleurs vives du logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("E", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("S", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("C", color = GescotiCrimsonRed, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("O", color = GescotiSkyBlue, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("T", color = GescotiMagentaPink, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("I", color = GescotiSunlitGold, fontWeight = FontWeight.Black, fontSize = 24.sp)

                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GescotiNavyContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tenantName,
                                color = GescotiMarianNavy,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (showSubtitle) {
                        Text(
                            text = paroisseName,
                            style = MaterialTheme.typography.labelSmall,
                            color = GescotiTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
