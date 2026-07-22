package com.n.alian.today.ui.tasklist

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nedal.today.R
import com.n.alian.today.data.local.Bucket

/** Localized display label for a bucket tab — keeps enum names out of the UI. */
@Composable
fun Bucket.label(): String = when (this) {
    Bucket.TODAY -> stringResource(R.string.bucket_today)
    Bucket.TOMORROW -> stringResource(R.string.bucket_tomorrow)
    Bucket.LATER -> stringResource(R.string.bucket_later)
}
