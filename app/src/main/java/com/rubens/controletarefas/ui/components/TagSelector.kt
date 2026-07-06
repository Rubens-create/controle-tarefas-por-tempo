package com.rubens.controletarefas.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TagSelector(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(tags) { tag ->
            val isSelected = tag == selectedTag
            val tagColors = com.rubens.controletarefas.ui.theme.getTagColors(tag)
            FilterChip(
                selected = isSelected,
                onClick = { onTagSelected(if (isSelected) "" else tag) },
                label = { Text(tag) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Label,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = tagColors.container,
                    selectedLabelColor = tagColors.content,
                    selectedLeadingIconColor = tagColors.content,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = tagColors.content,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}
