const db = require('../config/database');

// GET /api/pacientes/me/dashboard
exports.dashboard = async (req, res) => {
  try {
    const pacienteId = req.usuario.pacienteId;
    const [rows] = await db.query('SELECT * FROM vw_dashboard_paciente WHERE paciente_id = ?', [pacienteId]);
    const [proximas] = await db.query(
      `SELECT a.id, a.data, a.horario, s.nome AS servico, u.nome AS paciente
       FROM agendamentos a
       JOIN servicos s ON s.id = a.servico_id
       JOIN pacientes p ON p.id = a.paciente_id
       JOIN usuarios u ON u.id = p.usuario_id
       WHERE a.paciente_id = ? AND a.data >= CURDATE() AND a.status IN ('AGENDADO','CONFIRMADO')
       ORDER BY a.data, a.horario LIMIT 5`,
      [pacienteId]
    );
    res.json({ resumo: rows[0] || {}, proximasConsultas: proximas });
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro ao carregar dashboard' });
  }
};

// GET /api/pacientes/me/exercicios
exports.meusExercicios = async (req, res) => {
  try {
    const pacienteId = req.usuario.pacienteId;
    const [rows] = await db.query(
      `SELECT pr.id AS prescricao_id, pr.frequencia, pr.orientacoes, pr.data_inicio,
              e.id AS exercicio_id, e.titulo, e.descricao, e.instrucoes, e.duracao_minutos,
              e.video_url, e.imagem_url, c.nome AS categoria
       FROM prescricoes pr
       JOIN exercicios e ON e.id = pr.exercicio_id
       LEFT JOIN categorias c ON c.id = e.categoria_id
       WHERE pr.paciente_id = ? AND pr.ativa = TRUE
       ORDER BY pr.criada_em DESC`,
      [pacienteId]
    );

    // estatisticas
    const [stats] = await db.query(
      `SELECT
         (SELECT COUNT(*) FROM prescricoes WHERE paciente_id = ? AND ativa = TRUE) AS ativos,
         (SELECT COUNT(*) FROM execucoes WHERE paciente_id = ? AND data_execucao >= NOW() - INTERVAL 7 DAY) AS esta_semana`,
      [pacienteId, pacienteId]
    );
    res.json({ prescricoes: rows, estatisticas: stats[0] });
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro ao carregar exercicios' });
  }
};

// POST /api/execucoes
exports.registrarExecucao = async (req, res) => {
  try {
    const pacienteId = req.usuario.pacienteId;
    const { prescricaoId, nivelDor, observacoes } = req.body;

    if (prescricaoId == null || nivelDor == null) {
      return res.status(400).json({ erro: 'prescricaoId e nivelDor sao obrigatorios' });
    }
    if (nivelDor < 0 || nivelDor > 10) {
      return res.status(400).json({ erro: 'nivelDor deve estar entre 0 e 10' });
    }

    const [r] = await db.query(
      'INSERT INTO execucoes (prescricao_id, paciente_id, nivel_dor, observacoes) VALUES (?, ?, ?, ?)',
      [prescricaoId, pacienteId, nivelDor, observacoes || null]
    );
    res.status(201).json({ id: r.insertId, mensagem: 'Execucao registrada' });
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro ao registrar execucao' });
  }
};

// GET /api/pacientes/me/historico
exports.historico = async (req, res) => {
  try {
    const pacienteId = req.usuario.pacienteId;
    const [execucoes] = await db.query(
      `SELECT ex.id, ex.nivel_dor, ex.observacoes, ex.data_execucao,
              e.titulo, c.nome AS categoria
       FROM execucoes ex
       JOIN prescricoes pr ON pr.id = ex.prescricao_id
       JOIN exercicios e ON e.id = pr.exercicio_id
       LEFT JOIN categorias c ON c.id = e.categoria_id
       WHERE ex.paciente_id = ?
       ORDER BY ex.data_execucao DESC LIMIT 50`,
      [pacienteId]
    );

    const [grafico] = await db.query(
      `SELECT DATE(data_execucao) AS dia, ROUND(AVG(nivel_dor),1) AS dor_media
       FROM execucoes
       WHERE paciente_id = ? AND data_execucao >= NOW() - INTERVAL 7 DAY
       GROUP BY DATE(data_execucao) ORDER BY dia ASC`,
      [pacienteId]
    );

    const [resumo] = await db.query(
      `SELECT
         COUNT(*) AS total,
         ROUND(AVG(nivel_dor),1) AS dor_media
       FROM execucoes WHERE paciente_id = ? AND data_execucao >= NOW() - INTERVAL 7 DAY`,
      [pacienteId]
    );
    res.json({ resumo: resumo[0], grafico, execucoes });
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro ao carregar historico' });
  }
};

// GET /api/pacientes/me/notificacoes
exports.notificacoes = async (req, res) => {
  try {
    const usuarioId = req.usuario.id;
    const [rows] = await db.query(
      `SELECT id, tipo, titulo, mensagem, lida, criada_em
       FROM notificacoes WHERE usuario_id = ? ORDER BY criada_em DESC LIMIT 50`,
      [usuarioId]
    );
    res.json(rows);
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro ao carregar notificacoes' });
  }
};

// PUT /api/notificacoes/:id/lida
exports.marcarLida = async (req, res) => {
  try {
    await db.query('UPDATE notificacoes SET lida = TRUE WHERE id = ? AND usuario_id = ?',
      [req.params.id, req.usuario.id]);
    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ erro: 'Erro' });
  }
};

// DELETE /api/notificacoes (limpar todas)
exports.limparNotificacoes = async (req, res) => {
  try {
    await db.query('UPDATE notificacoes SET lida = TRUE WHERE usuario_id = ?', [req.usuario.id]);
    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ erro: 'Erro' });
  }
};

// GET /api/servicos
exports.listarServicos = async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM servicos WHERE ativo = TRUE');
    res.json(rows);
  } catch (e) {
    res.status(500).json({ erro: 'Erro' });
  }
};

// GET /api/agendamentos/horarios?data=2026-05-15
exports.horariosDisponiveis = async (req, res) => {
  try {
    const { data } = req.query;
    if (!data) return res.status(400).json({ erro: 'Parametro data obrigatorio' });

    const todos = ['08:00','08:30','09:00','09:30','10:00','10:30','11:00','11:30',
                   '14:00','14:30','15:00','15:30','16:00','16:30','17:00','17:30','18:00'];

    const [ocupados] = await db.query(
      `SELECT TIME_FORMAT(horario,'%H:%i') AS h FROM agendamentos
       WHERE data = ? AND status IN ('AGENDADO','CONFIRMADO')`,
      [data]
    );
    const setOc = new Set(ocupados.map(o => o.h));
    res.json(todos.map(h => ({ horario: h, disponivel: !setOc.has(h) })));
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro' });
  }
};

// POST /api/agendamentos
exports.criarAgendamento = async (req, res) => {
  try {
    const pacienteId = req.usuario.pacienteId;
    const { servicoId, data, horario } = req.body;
    if (!servicoId || !data || !horario) {
      return res.status(400).json({ erro: 'Campos obrigatorios faltando' });
    }
    const [r] = await db.query(
      'INSERT INTO agendamentos (paciente_id, servico_id, data, horario) VALUES (?, ?, ?, ?)',
      [pacienteId, servicoId, data, horario]
    );

    // notificacao para o profissional
    await db.query(
      `INSERT INTO notificacoes (usuario_id, tipo, titulo, mensagem)
       SELECT id, 'CONSULTA', 'Novo agendamento', CONCAT('Novo agendamento em ', ?, ' as ', ?)
       FROM usuarios WHERE perfil IN ('PROFISSIONAL','ADMIN')`,
      [data, horario]
    );

    res.status(201).json({ id: r.insertId, mensagem: 'Agendamento criado' });
  } catch (e) {
    if (e.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ erro: 'Horario ja ocupado' });
    }
    console.error(e);
    res.status(500).json({ erro: 'Erro ao agendar' });
  }
};

// GET /api/pacientes/me/perfil
exports.perfil = async (req, res) => {
  try {
    const [rows] = await db.query(
      `SELECT u.id, u.nome, u.email, p.cpf, p.telefone, p.data_nascimento,
              p.endereco, p.cidade, p.estado, p.cep
       FROM usuarios u JOIN pacientes p ON p.usuario_id = u.id
       WHERE p.id = ?`,
      [req.usuario.pacienteId]
    );
    if (rows.length === 0) return res.status(404).json({ erro: 'Paciente nao encontrado' });

    const clinica = {
      nome: 'Maya Yamamoto - Fisioterapia RPG',
      endereco: 'Av. Paulista, 1000 - Conj. 501',
      cidade: 'Sao Paulo - SP',
      cep: '01310-100',
      telefone: '(11) 3456-7890',
      horario: 'Seg-Sex: 8h as 20h | Sab: 8h as 14h'
    };
    res.json({ paciente: rows[0], clinica });
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro' });
  }
};

// GET /api/pacientes/me/pagamentos
exports.pagamentos = async (req, res) => {
  try {
    const [rows] = await db.query(
      `SELECT id, descricao, valor, forma_pagamento, status, data_pagamento
       FROM pagamentos WHERE paciente_id = ?
       ORDER BY data_pagamento DESC`,
      [req.usuario.pacienteId]
    );
    const [tot] = await db.query(
      `SELECT COALESCE(SUM(valor),0) AS total FROM pagamentos WHERE paciente_id = ? AND status='PAGO'`,
      [req.usuario.pacienteId]
    );
    res.json({ total: tot[0].total, pagamentos: rows });
  } catch (e) {
    console.error(e);
    res.status(500).json({ erro: 'Erro' });
  }
};
