package com.n.alian.today.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.n.alian.today.ui.theme.Spacing
@Composable
fun EmptyState(title: String, subTitle: String){

    Column(modifier = Modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = title , style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(text = subTitle, style = MaterialTheme.typography.bodyMedium ,
            color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
