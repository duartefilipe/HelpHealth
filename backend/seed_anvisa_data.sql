-- População Inicial de Dados Oficiais da Anvisa / CMED

INSERT INTO fabricantes (cnpj, razao_social, nome_fantasia) VALUES
('33018514000128', 'SANOFI MEDLEY FARMACEUTICA LTDA', 'Sanofi / Medley'),
('00394544000185', 'MEDLEY FARMACEUTICA LTDA', 'Medley'),
('61082004000112', 'EMS S.A.', 'EMS Farmacêutica'),
('57507378000101', 'EUROFARMA LABORATORIOS S.A.', 'Eurofarma'),
('49179261000121', 'NEO QUIMICA / HYPERA S.A.', 'Neo Química')
ON CONFLICT (cnpj) DO NOTHING;

INSERT INTO medicamentos (ean, nome_comercial, principio_ativo, concentracao, forma_farmaceutica, categoria_regulatoria, tarja, retencao_receita, precisa_refrigeracao, link_bula_paciente, faz_parte_farmacia_popular, cnpj_fabricante, status_registro) VALUES
('7891010000011', 'TYLENOL', 'PARACETAMOL', '750 MG', 'COMPRIMIDO', 'REFERENCIA', 'ISENTO', false, false, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000001', false, '33018514000128', 'ATIVO'),
('7896422500022', 'PARACETAMOL', 'PARACETAMOL', '750 MG', 'COMPRIMIDO', 'GENERICO', 'ISENTO', false, false, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000002', false, '00394544000185', 'ATIVO'),
('7896004700033', 'PARALGEN', 'PARACETAMOL', '750 MG', 'COMPRIMIDO', 'SIMILAR INTERCAMBIAVEL', 'ISENTO', false, false, null, false, '61082004000112', 'ATIVO'),

('7891059000144', 'NOVALGINA', 'DIPIRONA SODICA', '500 MG/ML', 'SOLUCAO ORAL GOTAS', 'REFERENCIA', 'ISENTO', false, false, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000004', false, '33018514000128', 'ATIVO'),
('7896422500155', 'DIPIRONA SODICA', 'DIPIRONA SODICA', '500 MG/ML', 'SOLUCAO ORAL GOTAS', 'GENERICO', 'ISENTO', false, false, null, false, '00394544000185', 'ATIVO'),
('7896004700166', 'ANADOR', 'DIPIRONA SODICA', '500 MG/ML', 'SOLUCAO ORAL GOTAS', 'SIMILAR INTERCAMBIAVEL', 'ISENTO', false, false, null, false, '49179261000121', 'ATIVO'),

('7891059000274', 'AMOXIL', 'AMOXICILINA', '500 MG', 'CAPSULA DURA', 'REFERENCIA', 'VERMELHA', true, false, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000007', false, '57507378000101', 'ATIVO'),
('7896422500281', 'AMOXICILINA', 'AMOXICILINA', '500 MG', 'CAPSULA DURA', 'GENERICO', 'VERMELHA', true, false, null, false, '61082004000112', 'ATIVO'),

('7891059000397', 'LOSEC', 'OMEPRAZOL', '20 MG', 'CAPSULA DURA', 'REFERENCIA', 'VERMELHA', false, false, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000009', false, '57507378000101', 'ATIVO'),
('7896422500304', 'OMEPRAZOL', 'OMEPRAZOL', '20 MG', 'CAPSULA DURA', 'GENERICO', 'VERMELHA', false, false, null, false, '00394544000185', 'ATIVO'),

('7891059000410', 'COZAAR', 'LOSARTANA POTASSICA', '50 MG', 'COMPRIMIDO REVESTIDO', 'REFERENCIA', 'VERMELHA', false, false, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000011', true, '57507378000101', 'ATIVO'),
('7896422500423', 'LOSARTANA POTASSICA', 'LOSARTANA POTASSICA', '50 MG', 'COMPRIMIDO REVESTIDO', 'GENERICO', 'VERMELHA', false, false, null, true, '61082004000112', 'ATIVO'),

('7891059000533', 'HUMULIN N', 'INSULINA NPH', '100 UI/ML', 'SUSPENSAO INJETAVEL', 'REFERENCIA', 'VERMELHA', false, true, 'https://consultas.anvisa.gov.br/api/consulta/bula/25351.000013', true, '57507378000101', 'ATIVO'),
('7896422500540', 'INSULINA NPH', 'INSULINA NPH', '100 UI/ML', 'SUSPENSAO INJETAVEL', 'GENERICO', 'VERMELHA', false, true, null, true, '61082004000112', 'ATIVO')
ON CONFLICT (ean) DO NOTHING;

INSERT INTO precos_cmed (ean, uf, pmc_zero_icms, pmc_18_icms) VALUES
('7891010000011', 'SP', 15.50, 18.90),
('7896422500022', 'SP', 7.20, 8.80),
('7896004700033', 'SP', 8.10, 9.90),

('7891059000144', 'SP', 22.00, 26.80),
('7896422500155', 'SP', 9.50, 11.60),
('7896004700166', 'SP', 10.20, 12.40),

('7891059000274', 'SP', 45.00, 54.90),
('7896422500281', 'SP', 18.30, 22.30),

('7891059000397', 'SP', 38.00, 46.30),
('7896422500304', 'SP', 14.20, 17.30),

('7891059000410', 'SP', 29.00, 35.40),
('7896422500423', 'SP', 0.00, 0.00),

('7891059000533', 'SP', 65.00, 79.30),
('7896422500540', 'SP', 0.00, 0.00)
ON CONFLICT DO NOTHING;
