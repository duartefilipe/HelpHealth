import pandas as pd

excel_file = '/home/anakin/Documentos/Codigos/Anakin/HelpHealth/backend/cmed_anvisa_2026.xlsx'
output_csv = '/home/anakin/Documentos/Codigos/Anakin/HelpHealth/backend/tabela_anvisa_oficial_2026.csv'

print("Lendo planilha oficial da Anvisa (header=42)...")
df = pd.read_excel(excel_file, header=42)

print(f"Total de registros na planilha oficial: {len(df)}")

# Mapeando colunas para o formato esperado pelo IngestionService
renames = {
    'SUBSTÂNCIA': 'SUBSTANCIA',
    'CNPJ': 'CNPJ',
    'LABORATÓRIO': 'LABORATORIO',
    'EAN 1': 'EAN 1',
    'PRODUTO': 'PRODUTO',
    'APRESENTAÇÃO': 'APRESENTACAO',
    'CLASSE TERAPÊUTICA': 'CLASSE TERAPEUTICA',
    'TIPO DE PRODUTO (STATUS DO PRODUTO)': 'TIPO DE PRODUTO (STATUS)',
    'TARJA': 'TARJA',
    'PMC 0 %': 'PMC 0%',
    'PMC 18 %': 'PMC 18%'
}

df_renamed = df.rename(columns=renames)

# Filtrando registros válidos
df_clean = df_renamed.dropna(subset=['SUBSTANCIA', 'PRODUTO'])

# Garantindo que CNPJ e EAN sejam strings limpas
df_clean['CNPJ'] = df_clean['CNPJ'].astype(str).str.replace(r'\D', '', regex=True)
df_clean['EAN 1'] = df_clean['EAN 1'].astype(str).str.replace(r'\D', '', regex=True)

df_clean.to_csv(output_csv, index=False, sep=';', encoding='utf-8')
print(f"Planilha oficial da Anvisa convertida com sucesso! Registros válidos salvos: {len(df_clean)} em {output_csv}")
