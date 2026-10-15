package uz.tikoncha_parent.presentation.live

/**
 * Jonli sessiya davomida qabul qilinayotgan ovozni karnay (loudspeaker)ga yo'naltiradi.
 *
 * iOS'da WebRTC standart holatda ovozni quloqqa tutiladigan dinamik (earpiece)ga beradi —
 * telefon qo'lda ushlab turilsa "ovoz yo'q"dek tuyuladi. Shu sabab sessiya boshlanganda
 * audio-sessiyani `playAndRecord` + `defaultToSpeaker` ga sozlaymiz, tugaganda tiklaymiz.
 */
expect object LiveAudio {
    /** Route the incoming live audio to the loudspeaker. Called when a session starts. */
    fun startPlayback()

    /** Restore default audio routing. Called when the session ends. */
    fun stop()
}
