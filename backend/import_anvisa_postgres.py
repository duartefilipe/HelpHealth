#!/usr/bin/env python3
"""
Importa os 25.570 medicamentos oficiais da Anvisa diretamente no PostgreSQL.
Abordagem em 2 fases: primeiro fabricantes, depois medicamentos.
"""
import pandas as pd
import psycopg2
import sys

DB_HOST = "192.168.31.229"
DB_PORT = 15432
DB_NAME = "helphealth"
DB_USER = "postgres"
DB_PASS = "postgres"

EXCEL_FILE = "/home/anakin/Documentos/Codigos/Anakin/HelpHealth/backend/cmed_anvisa_2026.xlsx"

print("=== Importação Direta Anvisa -> PostgreSQL ===")
print(f"Lendo planilha oficial: {EXCEL_FILE}")

df = pd.read_excel(EXCEL_FILE, header=42)
print(f"Total de registros na planilha: {len(df)}")

conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
conn.autocommit = True
cur = conn.cursor()

# Limpar tabelas
print("Limpando tabelas...")
cur.execute("TRUNCATE precos_cmed, medicamentos CASCADE;")
cur.execute("TRUNCATE fabricantes CASCADE;")

# ========= FASE 1: Inserir TODOS os fabricantes =========
print("\n--- FASE 1: Inserindo fabricantes ---")
fabricantes = {}
for idx, row in df.iterrows():
    cnpj = str(row.get('CNPJ', '')).strip().replace('.', '').replace('/', '').replace('-', '')
    laboratorio = str(row.get('LABORATÓRIO', '')).strip()
    if cnpj and cnpj != 'nan' and len(cnpj) >= 8 and cnpj not in fabricantes:
        fabricantes[cnpj] = laboratorio if laboratorio != 'nan' else 'DESCONHECIDO'

fab_count = 0
for cnpj, razao_social in fabricantes.items():
    try:
        cur.execute(
            "INSERT INTO fabricantes (cnpj, razao_social, nome_fantasia) VALUES (%s, %s, %s) ON CONFLICT (cnpj) DO NOTHING",
            (cnpj[:20], razao_social[:250], razao_social[:250])
        )
        fab_count += 1
    except Exception as e:
        print(f"  Erro fabricante {cnpj}: {e}")

print(f"  {fab_count} fabricantes inseridos.")

# ========= FASE 2: Inserir medicamentos =========
print("\n--- FASE 2: Inserindo medicamentos ---")
med_count = 0
err_count = 0

for idx, row in df.iterrows():
    try:
        cnpj = str(row.get('CNPJ', '')).strip().replace('.', '').replace('/', '').replace('-', '')
        produto = str(row.get('PRODUTO', '')).strip()
        substancia = str(row.get('SUBSTÂNCIA', '')).strip()
        apresentacao = str(row.get('APRESENTAÇÃO', '')).strip() if pd.notna(row.get('APRESENTAÇÃO')) else None
        tipo_produto = str(row.get('TIPO DE PRODUTO (STATUS DO PRODUTO)', '')).strip() if pd.notna(row.get('TIPO DE PRODUTO (STATUS DO PRODUTO)')) else 'SIMILAR'
        tarja = str(row.get('TARJA', '')).strip() if pd.notna(row.get('TARJA')) else None
        ean1 = str(row.get('EAN 1', '')).strip() if pd.notna(row.get('EAN 1')) else None

        if not produto or produto == 'nan':
            continue
        if not cnpj or cnpj == 'nan' or len(cnpj) < 8:
            cnpj = None

        ean_val = ean1 if ean1 and ean1 != 'nan' else f"AUTO_{idx}"

        cur.execute(
            """INSERT INTO medicamentos (ean, nome_comercial, principio_ativo, concentracao, forma_farmaceutica,
               categoria_regulatoria, tarja, retencao_receita, precisa_refrigeracao, link_bula_paciente,
               faz_parte_farmacia_popular, cnpj_fabricante, status_registro)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
               ON CONFLICT DO NOTHING""",
            (ean_val[:50], produto[:250], substancia[:250] if substancia != 'nan' else produto[:250],
             apresentacao[:500] if apresentacao else None,
             apresentacao[:500] if apresentacao else None,
             tipo_produto[:100] if tipo_produto else 'SIMILAR',
             tarja[:100] if tarja else None,
             False, False, None, False, cnpj, 'ATIVO')
        )
        med_count += 1

        if med_count % 1000 == 0:
            print(f"  Progresso: {med_count} medicamentos inseridos...")

    except Exception as e:
        err_count += 1
        if err_count <= 5:
            print(f"  Erro na linha {idx}: {e}")
        continue

# ========= FASE 3: Inserir preços =========
print(f"\n--- FASE 3: Inserindo preços ---")
preco_count = 0

for idx, row in df.iterrows():
    try:
        ean1 = str(row.get('EAN 1', '')).strip() if pd.notna(row.get('EAN 1')) else None
        if not ean1 or ean1 == 'nan':
            ean1 = f"AUTO_{idx}"

        def parse_preco(val):
            if pd.isna(val):
                return 0.0
            s = str(val).strip().replace('*', '').replace('.', '').replace(',', '.')
            try:
                return float(s)
            except:
                return 0.0

        pmc_0 = parse_preco(row.get('PMC 0 %'))
        pmc_18 = parse_preco(row.get('PMC 18 %'))

        if pmc_0 > 0 or pmc_18 > 0:
            for uf in ['RS', 'SP']:
                cur.execute(
                    "INSERT INTO precos_cmed (ean, uf, pmc_zero_icms, pmc_18_icms) VALUES (%s, %s, %s, %s)",
                    (ean1[:50], uf, pmc_0, pmc_18)
                )
                preco_count += 1

        if (idx + 1) % 5000 == 0:
            print(f"  Progresso preços: linha {idx+1}...")

    except Exception as e:
        if preco_count < 3:
            print(f"  Erro preço linha {idx}: {e}")
        continue

cur.close()
conn.close()

print(f"\n=== IMPORTAÇÃO CONCLUÍDA ===")
print(f"Fabricantes: {fab_count}")
print(f"Medicamentos: {med_count}")
print(f"Erros: {err_count}")
print(f"Preços (RS+SP): {preco_count}")
