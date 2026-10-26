package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.safety.DangerPlaces

interface SafetyRepository {
    /** Nuqta atrofidagi bola uchun xavfli joylar (klub, bar, kazino, alkogol do'koni…), yaqinidan uzog'iga. */
    suspend fun dangerPlaces(lat: Double, lng: Double, radiusM: Int): Outcome<DangerPlaces>
}
