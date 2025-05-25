package com.example.audioplayerdemo3.model

import com.example.audioplayerdemo3.contentResolver.AudioContentResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepo @Inject constructor(
    private val audioContentResolver: AudioContentResolver
) {

    suspend fun fetchAudioList(): List<Audio> = withContext(Dispatchers.IO){
        audioContentResolver.getAudioList()
    }


}