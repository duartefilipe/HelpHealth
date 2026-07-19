package com.duartefilipe.helphealth.repository

import com.duartefilipe.helphealth.data.DatabaseDriverFactory
import com.duartefilipe.helphealth.db.Fabricantes
import com.duartefilipe.helphealth.db.HelpHealthDatabase
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.db.Precos_cmed

data class InterchangeabilityResult(
    val selectedMedicine: Medicamentos,
    val equivalents: List<Medicamentos>,
    val sameManufacturerBadge: Boolean
)

class MedicineRepository(databaseDriverFactory: DatabaseDriverFactory) {

    private val database = HelpHealthDatabase(databaseDriverFactory.createDriver())
    private val dbQueries = database.helpHealthDatabaseQueries

    fun getAllMedicamentos(): List<Medicamentos> {
        return dbQueries.getAllMedicamentos().executeAsList()
    }

    fun searchMedicamentos(query: String): List<Medicamentos> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return getAllMedicamentos()

        var results = dbQueries.searchByText(cleanQuery).executeAsList()

        if (results.isEmpty()) {
            // Normalização e tolerância a erros de digitação (ex: "adivil" -> "advil")
            val normalizedQuery = cleanQuery
                .replace("adivil", "advil", ignoreCase = true)
                .replace("adiv", "adv", ignoreCase = true)

            if (normalizedQuery != cleanQuery) {
                results = dbQueries.searchByText(normalizedQuery).executeAsList()
            }
        }

        return results
    }

    fun checkInterchangeability(medicine: Medicamentos): InterchangeabilityResult {
        val equivalents = dbQueries.findEquivalents(
            principioAtivo = medicine.principio_ativo,
            concentracao = medicine.concentracao,
            formaFarmaceutica = medicine.forma_farmaceutica
        ).executeAsList()

        val referenceMed = equivalents.firstOrNull { it.categoria_regulatoria.equals("REFERENCIA", ignoreCase = true) }
        val isSameManufacturer = if (referenceMed != null && medicine.cnpj_fabricante != null) {
            medicine.cnpj_fabricante == referenceMed.cnpj_fabricante
        } else {
            false
        }

        return InterchangeabilityResult(
            selectedMedicine = medicine,
            equivalents = equivalents,
            sameManufacturerBadge = isSameManufacturer
        )
    }

    fun getFabricante(cnpj: String): Fabricantes? {
        return dbQueries.getFabricanteByCnpj(cnpj).executeAsOneOrNull()
    }

    fun getPrecoState(ean: String, uf: String): Precos_cmed? {
        return dbQueries.getPrecoByEanAndUf(ean, uf.uppercase()).executeAsOneOrNull()
    }
}
