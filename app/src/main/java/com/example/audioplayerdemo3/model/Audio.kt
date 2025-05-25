package com.example.audioplayerdemo3.model

import android.net.Uri
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    val uri: Uri,
    val name: String,
    val title: String,
    val id: Long,
    val duration: Long,
    val artist: String
): Parcelize