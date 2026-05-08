-- =====================================================================
-- Banco de Dados - Clinica Maya Yoshiko Yamamoto (RPG)
-- Projeto Interdisciplinar 3ADS - 2026
-- =====================================================================

DROP DATABASE IF EXISTS maya_rpg;
CREATE DATABASE maya_rpg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE maya_rpg;

-- ---------------------------------------------------------------------
-- USUARIOS (Admin / Profissional / Paciente)
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nome            VARCHAR(120) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    senha_hash      VARCHAR(255) NOT NULL,
    perfil          ENUM('ADMIN','PROFISSIONAL','PACIENTE') NOT NULL DEFAULT 'PACIENTE',
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    aceitou_lgpd    BOOLEAN NOT NULL DEFAULT FALSE,
    data_lgpd       DATETIME NULL,
    criado_em       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- PACIENTES (dados clinicos extras)
-- ---------------------------------------------------------------------
CREATE TABLE pacientes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      INT NOT NULL UNIQUE,
    cpf             VARCHAR(14) UNIQUE,
    data_nascimento DATE,
    telefone        VARCHAR(20),
    endereco        VARCHAR(255),
    cidade          VARCHAR(100),
    estado          VARCHAR(2),
    cep             VARCHAR(10),
    queixa_principal TEXT,
    status          ENUM('ATIVO','INATIVO') NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- CATEGORIAS DE EXERCICIOS (Alongamento, Fortalecimento, Respiracao, Mobilidade)
-- ---------------------------------------------------------------------
CREATE TABLE categorias (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nome    VARCHAR(80) NOT NULL UNIQUE,
    icone   VARCHAR(80),
    cor     VARCHAR(7)
);

-- ---------------------------------------------------------------------
-- BANCO DE EXERCICIOS (criado pela Dra. Maya no site admin)
-- ---------------------------------------------------------------------
CREATE TABLE exercicios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    titulo          VARCHAR(150) NOT NULL,
    descricao       TEXT,
    instrucoes      TEXT,
    duracao_minutos INT,
    video_url       VARCHAR(500),
    imagem_url      VARCHAR(500),
    categoria_id    INT,
    tags            VARCHAR(255),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exercicios_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- PRESCRICOES (Dra. Maya prescreve um exercicio para um paciente)
-- ---------------------------------------------------------------------
CREATE TABLE prescricoes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id     INT NOT NULL,
    exercicio_id    INT NOT NULL,
    profissional_id INT NOT NULL,
    frequencia      VARCHAR(80),
    orientacoes     TEXT,
    data_inicio     DATE NOT NULL,
    data_fim        DATE,
    ativa           BOOLEAN NOT NULL DEFAULT TRUE,
    criada_em       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_presc_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_presc_exercicio FOREIGN KEY (exercicio_id) REFERENCES exercicios(id) ON DELETE CASCADE,
    CONSTRAINT fk_presc_profissional FOREIGN KEY (profissional_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- EXECUCOES (paciente registra que fez o exercicio - check-in)
-- ---------------------------------------------------------------------
CREATE TABLE execucoes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    prescricao_id   INT NOT NULL,
    paciente_id     INT NOT NULL,
    nivel_dor       INT NOT NULL CHECK (nivel_dor BETWEEN 0 AND 10),
    observacoes     TEXT,
    data_execucao   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sincronizado    BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_exec_prescricao FOREIGN KEY (prescricao_id) REFERENCES prescricoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_exec_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- SERVICOS (RPG, Pilates, Osteopatia, etc)
-- ---------------------------------------------------------------------
CREATE TABLE servicos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    descricao   TEXT,
    duracao_min INT NOT NULL DEFAULT 60,
    valor       DECIMAL(10,2),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- AGENDAMENTOS / CONSULTAS
-- ---------------------------------------------------------------------
CREATE TABLE agendamentos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    servico_id  INT NOT NULL,
    data        DATE NOT NULL,
    horario     TIME NOT NULL,
    status      ENUM('AGENDADO','CONFIRMADO','REALIZADO','CANCELADO','FALTOU') NOT NULL DEFAULT 'AGENDADO',
    observacoes TEXT,
    criado_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_data_horario (data, horario),
    CONSTRAINT fk_ag_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_ag_servico FOREIGN KEY (servico_id) REFERENCES servicos(id) ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- PAGAMENTOS (Dra. Maya registra pelo admin -> aparece no app)
-- ---------------------------------------------------------------------
CREATE TABLE pagamentos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    agendamento_id  INT,
    paciente_id     INT NOT NULL,
    descricao       VARCHAR(150) NOT NULL,
    valor           DECIMAL(10,2) NOT NULL,
    forma_pagamento ENUM('PIX','CARTAO','DEBITO','DINHEIRO','BOLETO') NOT NULL,
    status          ENUM('PENDENTE','PAGO','CANCELADO') NOT NULL DEFAULT 'PAGO',
    data_pagamento  DATE NOT NULL,
    criado_em       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pag_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id) ON DELETE SET NULL,
    CONSTRAINT fk_pag_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- NOTIFICACOES
-- ---------------------------------------------------------------------
CREATE TABLE notificacoes (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT NOT NULL,
    tipo        ENUM('LEMBRETE_EXERCICIO','CONSULTA','PROGRESSO','SISTEMA') NOT NULL,
    titulo      VARCHAR(150) NOT NULL,
    mensagem    TEXT NOT NULL,
    lida        BOOLEAN NOT NULL DEFAULT FALSE,
    criada_em   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- DADOS INICIAIS
-- ---------------------------------------------------------------------

-- Admin/Profissional padrao (senha: maya123 - hash bcrypt)
INSERT INTO usuarios (nome, email, senha_hash, perfil, aceitou_lgpd, data_lgpd) VALUES
('Maya Yoshiko Yamamoto', 'maya@clinica.com', '$2b$10$rXqW8XQK4wKZhqPZ6vJxXuHGzXqV5xGPzFOZqXqW8XQK4wKZhqPZ6', 'PROFISSIONAL', TRUE, NOW()),
('Admin Sistema', 'admin@clinica.com', '$2b$10$rXqW8XQK4wKZhqPZ6vJxXuHGzXqV5xGPzFOZqXqW8XQK4wKZhqPZ6', 'ADMIN', TRUE, NOW());

-- Categorias
INSERT INTO categorias (nome, icone, cor) VALUES
('Alongamento', 'stretching', '#3DB5B0'),
('Fortalecimento', 'dumbbell', '#F08372'),
('Respiracao', 'lungs', '#0E5A7C'),
('Mobilidade', 'body', '#7A4A2A');

-- Servicos
INSERT INTO servicos (nome, descricao, duracao_min, valor) VALUES
('RPG - Reeducacao Postural Global', 'Sessao de RPG', 60, 180.00),
('Pilates Terapeutico', 'Sessao de pilates', 50, 150.00),
('Fisioterapia Ortopedica', 'Atendimento ortopedico', 50, 160.00),
('Osteopatia', 'Sessao de osteopatia', 60, 200.00),
('Avaliacao Postural', 'Avaliacao inicial completa', 90, 250.00);

-- Paciente exemplo (Maria Santos - senha: paciente123)
INSERT INTO usuarios (nome, email, senha_hash, perfil, aceitou_lgpd, data_lgpd) VALUES
('Maria Santos', 'maria.santos@email.com', '$2b$10$rXqW8XQK4wKZhqPZ6vJxXuHGzXqV5xGPzFOZqXqW8XQK4wKZhqPZ6', 'PACIENTE', TRUE, NOW());

INSERT INTO pacientes (usuario_id, cpf, data_nascimento, telefone, endereco, cidade, estado, cep, queixa_principal) VALUES
(3, '123.456.789-00', '1990-05-15', '(11) 98765-4321', 'Rua das Flores, 123 - Jardim Paulista', 'Sao Paulo', 'SP', '01234-567', 'Dor cervical e lombar');

-- Exercicios exemplo
INSERT INTO exercicios (titulo, descricao, instrucoes, duracao_minutos, categoria_id, video_url) VALUES
('Alongamento Cervical', 'Exercicio para alivio de tensao no pescoco e melhora da postura cervical', 'Sente-se com a coluna ereta\nIncline a cabeca lentamente para o lado direito\nMantenha por 20 segundos\nRepita para o lado esquerdo\nExecute 3 repeticoes de cada lado', 5, 1, 'https://example.com/videos/alongamento_cervical.mp4'),
('Ponte Glutea', 'Fortalecimento de gluteos e estabilizacao lombar', 'Deite-se de costas com os joelhos flexionados\nEleve o quadril ate formar uma linha reta\nMantenha por 5 segundos\nRetorne lentamente\nFaca 3 series de 10 repeticoes', 8, 2, 'https://example.com/videos/ponte_glutea.mp4'),
('Respiracao Diafragmatica', 'Exercicio de respiracao para relaxamento e oxigenacao', 'Sente-se confortavelmente\nInspire pelo nariz por 4 segundos\nSegure por 2 segundos\nExpire pela boca por 6 segundos\nRepita por 5 minutos', 5, 3, 'https://example.com/videos/respiracao.mp4'),
('Mobilidade de Quadril', 'Melhora da amplitude articular do quadril', 'Em pe, pernas afastadas\nFlexione o tronco lateralmente\nGire suavemente o quadril\n10 repeticoes para cada lado', 6, 4, 'https://example.com/videos/mobilidade_quadril.mp4');

-- Prescricoes para Maria Santos (paciente_id=1)
INSERT INTO prescricoes (paciente_id, exercicio_id, profissional_id, frequencia, orientacoes, data_inicio) VALUES
(1, 1, 1, '3x ao dia', 'Realizar pela manha, tarde e noite', CURDATE() - INTERVAL 7 DAY),
(1, 2, 1, '2x ao dia', 'Antes de dormir e ao acordar', CURDATE() - INTERVAL 7 DAY),
(1, 3, 1, '1x ao dia', 'Antes de dormir', CURDATE() - INTERVAL 5 DAY),
(1, 4, 1, '2x na semana', 'Em dias alternados', CURDATE() - INTERVAL 5 DAY);

-- Execucoes simulando historico de dor
INSERT INTO execucoes (prescricao_id, paciente_id, nivel_dor, observacoes, data_execucao) VALUES
(1, 1, 5, 'Senti tensao no inicio',                CURDATE() - INTERVAL 5 DAY),
(2, 1, 4, 'Mais leve hoje',                       CURDATE() - INTERVAL 4 DAY),
(1, 1, 2, 'Otimo!',                               CURDATE() - INTERVAL 3 DAY),
(3, 1, 3, 'Relaxante',                            CURDATE() - INTERVAL 2 DAY),
(2, 1, 1, 'Quase sem dor',                        CURDATE() - INTERVAL 1 DAY),
(1, 1, 2, 'Senti menos tensao hoje',              CURDATE()),
(4, 1, 2, 'Bom',                                  CURDATE());

-- Pagamentos exemplo
INSERT INTO pagamentos (paciente_id, descricao, valor, forma_pagamento, data_pagamento) VALUES
(1, 'Sessao RPG',         180.00, 'CARTAO',   '2026-03-15'),
(1, 'Sessao RPG',         180.00, 'PIX',      '2026-03-08'),
(1, 'Avaliacao + RPG',    250.00, 'DINHEIRO', '2026-03-01'),
(1, 'Sessao RPG',         180.00, 'DEBITO',   '2026-02-22'),
(1, 'Sessao RPG',         180.00, 'PIX',      '2026-02-15');

-- Agendamento futuro
INSERT INTO agendamentos (paciente_id, servico_id, data, horario, status) VALUES
(1, 1, CURDATE() + INTERVAL 1 DAY, '10:00:00', 'AGENDADO');

-- Notificacoes exemplo
INSERT INTO notificacoes (usuario_id, tipo, titulo, mensagem, lida) VALUES
(3, 'LEMBRETE_EXERCICIO', 'Lembrete de Exercicio', 'Hora de fazer o Alongamento Cervical - 3x ao dia', FALSE),
(3, 'CONSULTA',           'Consulta amanha',        'Voce tem consulta com Dra. Maya as 10:00',        FALSE),
(3, 'PROGRESSO',          'Otimo progresso!',       'Voce completou 5 exercicios esta semana',         FALSE),
(3, 'SISTEMA',            'Bem-vinda!',             'Seu plano de exercicios esta disponivel',         FALSE);

-- ---------------------------------------------------------------------
-- VIEWS uteis
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_dashboard_paciente AS
SELECT
    p.id AS paciente_id,
    u.nome,
    (SELECT COUNT(*) FROM prescricoes WHERE paciente_id = p.id AND ativa = TRUE) AS total_exercicios,
    (SELECT COUNT(*) FROM execucoes WHERE paciente_id = p.id AND DATE(data_execucao) = CURDATE()) AS exercicios_hoje,
    (SELECT ROUND(AVG(nivel_dor),1) FROM execucoes WHERE paciente_id = p.id AND data_execucao >= NOW() - INTERVAL 7 DAY) AS dor_media_7d
FROM pacientes p
JOIN usuarios u ON u.id = p.usuario_id;

SELECT 'Banco maya_rpg criado com sucesso!' AS status;
