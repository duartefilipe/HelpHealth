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

    val database = HelpHealthDatabase(databaseDriverFactory.createDriver())
    private val dbQueries = database.helpHealthDatabaseQueries

    fun countMedicamentos(): Long {
        return dbQueries.countMedicamentos().executeAsOne()
    }

    fun getAllMedicamentos(page: Int = 0, pageSize: Int = 20): List<Medicamentos> {
        val offset = (page * pageSize).toLong()
        return dbQueries.getAllMedicamentosPaged(limit = pageSize.toLong(), offset = offset).executeAsList()
    }

    fun searchMedicamentos(query: String, page: Int = 0, pageSize: Int = 20): List<Medicamentos> {
        val cleanQuery = query.trim()
        val offset = (page * pageSize).toLong()
        if (cleanQuery.isBlank()) return getAllMedicamentos(page, pageSize)

        var results = dbQueries.searchByTextPaged(query = cleanQuery, limit = pageSize.toLong(), offset = offset).executeAsList()

        if (results.isEmpty() && page == 0) {
            // Normalização e tolerância a erros de digitação (ex: "adivil" -> "advil", "monjaro" -> "mounjaro")
            val normalizedQuery = cleanQuery
                .replace("adivil", "advil", ignoreCase = true)
                .replace("adiv", "adv", ignoreCase = true)
                .replace("monjaro", "mounjaro", ignoreCase = true)

            if (normalizedQuery != cleanQuery) {
                results = dbQueries.searchByTextPaged(query = normalizedQuery, limit = pageSize.toLong(), offset = offset).executeAsList()
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
