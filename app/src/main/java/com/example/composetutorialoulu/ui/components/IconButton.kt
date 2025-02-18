// ui/components/IconButton.kt
package com.example.composetutorialoulu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun IconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    alignment: Alignment,
    padding: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = padding),
        contentAlignment = alignment,
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(30.dp),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Options", //Change to parameter
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
