package com.example.audioplayerdemo3.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.audioplayerdemo3.audioService.AudioServiceHandler
import com.example.audioplayerdemo3.audioService.AudioState
import com.example.audioplayerdemo3.audioService.PlayerEvent
import com.example.audioplayerdemo3.model.Audio
import com.example.audioplayerdemo3.model.AudioRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private val dummyAudio = Audio(
    "".toUri(),
    "",
    "",
    0L,
    0,
    ""
)


@HiltViewModel
class AudioViewmodel @Inject constructor(
    private val audioServiceHandler: AudioServiceHandler,
    private val audioRepo: AudioRepo,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    var duration by savedStateHandle.saveable{ mutableStateOf(0L) }
    var progress by savedStateHandle.saveable{mutableStateOf(0f)}
    var progressString by savedStateHandle.saveable{mutableStateOf("00:00")}
    var isPlaying by savedStateHandle.saveable{mutableStateOf(false)}
    var currentSelectedAudio by savedStateHandle.saveable{mutableStateOf(dummyAudio)}
    var audioList by savedStateHandle.saveable{mutableStateOf(listOf<Audio>())}

    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        loadAudioData()
    }

    init {
        viewModelScope.launch{
            audioServiceHandler.audioState.collectLatest {mediaState->

                when(mediaState){
                     AudioState.Initial -> _uiState.value = UIState.Initial
                    is AudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is AudioState.Playing -> isPlaying = mediaState.isPlaying
                    is AudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioState.CurrentPlaying ->{
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                    }
                    is AudioState.Ready ->{
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }

            }
        }
    }

    private fun loadAudioData(){
        viewModelScope.launch{
            val audio = audioRepo.fetchAudioList()
            audioList = audio
            setAudioMediaItems()
        }
    }

    private fun setAudioMediaItems(){
        audioList.map {audio->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setDisplayTitle(audio.title)
                        .setSubtitle(audio.name)
                        .build()


                )
                .build()
        }.also {
            audioServiceHandler.setMediaItemList(it)
        }
    }

    private fun calculateProgressValue(currentProgress: Long){
        progress = if (currentProgress > 0) {
            ((currentProgress.toFloat() / duration.toFloat()) * 100)
        }
        else 0f

        progressString = formatDuration(currentProgress)
    }


    fun onUIEvents(uiEvents: UIEvents) = viewModelScope.launch{
        when(uiEvents){
            UIEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekTONext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            is UIEvents.PlayPause ->{
                audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            }
            is UIEvents.SeekTo ->{
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }
            is UIEvents.SelectedAudioChanged ->{
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChanged,
                    selectedAudioIndex = uiEvents.index
                )
            }
            is UIEvents.UpdateProgress ->{
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(uiEvents.newProgress)
                )
            }

        }
    }




    fun formatDuration(duration: Long): String{
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MINUTES)
        val second = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d",minute,second)
    }


}

sealed class UIEvents{
    object PlayPause: UIEvents()
    data class SelectedAudioChanged(val index: Int): UIEvents()
    data class SeekTo(val position: Float): UIEvents()
    object SeekTONext: UIEvents()
    object Backward: UIEvents()
    object Forward: UIEvents()
    data class UpdateProgress(val newProgress: Float): UIEvents()
}


sealed class UIState{
    object Initial: UIState()
    object Ready: UIState()
}