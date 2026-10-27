package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.safety.DangerPlaces
import uz.tikoncha_parent.domain.model.safety.SafetySwitch
import uz.tikoncha_parent.domain.model.safety.SafetySwitches

interface SafetyRepository {
    /** Nuqta atrofidagi bola uchun xavfli joylar (klub, bar, kazino, alkogol do'koni…), yaqinidan uzog'iga. */
    suspend fun dangerPlaces(lat: Double, lng: Double, radiusM: Int): Outcome<DangerPlaces>

    /** Farzand uçun maxsus himoyalar (ota-ona yoqqanlari). */
    suspend fun switches(childId: String): Outcome<SafetySwitches>

    suspend fun setSwitch(childId: String, switch: SafetySwitch, on: Boolean): Outcome<SafetySwitches>
}
