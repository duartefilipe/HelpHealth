#!/usr/bin/env python3
import pandas as pd
import psycopg2
import sys

DB_HOST = "192.168.31.229"
DB_PORT = 15432
DB_NAME = "helphealth"
DB_USER = "postgres"
DB_PASS = "postgres"
EXCEL_FILE = "/home/anakin/Documentos/Codigos/Anakin/HelpHealth/backend/cmed_anvisa_2026.xlsx"

print("Lendo planilha oficial...")
df = pd.read_excel(EXCEL_FILE, header=42)
conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
conn.autocommit = True
cur = conn.cursor()

print("Inserindo preços...")
cur.execute("TRUNCATE precos_cmed;")
preco_count = 0

for idx, row in df.iterrows():
    try:
        ean1 = str(row.get('EAN 1', '')).strip() if pd.notna(row.get('EAN 1')) else f"AUTO_{idx}"
        
        def parse_preco(val):
            if pd.isna(val): return 0.0
            try: return float(str(val).strip().replace('*', '').replace('.', '').replace(',', '.'))
            except: return 0.0

        pmc_0 = parse_preco(row.get('PMC 0 %'))
        pmc_18 = parse_preco(row.get('PMC 18 %'))

        if pmc_0 > 0 or pmc_18 > 0:
            for uf in ['RS', 'SP']:
                cur.execute(
                    "INSERT INTO precos_cmed (ean, uf, pmc_zero_icms, pmc_18_icms) VALUES (%s, %s, %s, %s) ON CONFLICT DO NOTHING",
                    (ean1[:50], uf, pmc_0, pmc_18)
                )
                preco_count += 1
        
        if (idx + 1) % 5000 == 0:
            print(f"Progresso preços: linha {idx+1}...")
    except Exception as e:
        if preco_count < 3: print(f"Erro preço linha {idx}: {e}")
        continue

cur.close()
conn.close()
print(f"Preços inseridos: {preco_count}")
