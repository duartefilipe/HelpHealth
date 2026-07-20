package com.duartefilipe.helphealth.repository

import com.duartefilipe.helphealth.data.DatabaseDriverFactory
import com.duartefilipe.helphealth.db.Fabricantes
import com.duartefilipe.helphealth.db.HelpHealthDatabase
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.db.Precos_cmed

import com.duartefilipe.helphealth.db.Alarmes

data class EquivalentMedicine(
    val medicine: Medicamentos,
    val pmc18: Double?
)

data class InterchangeabilityResult(
    val selectedMedicine: Medicamentos,
    val equivalents: List<EquivalentMedicine>,
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

    fun checkInterchangeability(medicine: Medicamentos, uf: String): InterchangeabilityResult {
        val equivalentsDb = dbQueries.findEquivalents(
            principioAtivo = medicine.principio_ativo,
            concentracao = medicine.concentracao,
            formaFarmaceutica = medicine.forma_farmaceutica
        ).executeAsList()

        // Fetch prices and map
        val equivalents = equivalentsDb.map { eq ->
            val preco = eq.ean?.let { getPrecoState(it, uf)?.pmc_18_icms }
            EquivalentMedicine(eq, preco)
        }.sortedBy { it.pmc18 ?: Double.MAX_VALUE }

        val referenceMed = equivalentsDb.firstOrNull { it.categoria_regulatoria.equals("REFERENCIA", ignoreCase = true) }
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

    // --- FAVORITOS ---
    fun isFavorito(ean: String): Boolean {
        return dbQueries.isFavorito(ean).executeAsOne() > 0
    }

    fun toggleFavorito(ean: String, isFavoriting: Boolean) {
        if (isFavoriting) {
            dbQueries.insertFavorito(ean)
        } else {
            dbQueries.deleteFavorito(ean)
        }
    }

    fun getFavoritosPaged(page: Int = 0, pageSize: Int = 20): List<Medicamentos> {
        val offset = (page * pageSize).toLong()
        return dbQueries.getFavoritosPaged(limit = pageSize.toLong(), offset = offset).executeAsList()
    }

    // --- ALARMES ---
    fun addAlarme(ean: String, hora: Int, minuto: Int, nome: String, dias: String) {
        dbQueries.insertAlarme(ean, hora.toLong(), minuto.toLong(), nome, dias, 1L)
    }

    fun getAllAlarmes(): List<Alarmes> {
        return dbQueries.getAllAlarmes().executeAsList()
    }

    fun deleteAlarme(id: Long) {
        dbQueries.deleteAlarme(id)
    }
}
