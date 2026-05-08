const bcrypt = require('bcryptjs');
const db = require('../config/database');

// ==================== PACIENTES ====================
exports.listarPacientes = async (req, res) => {
  try {
    const { busca, status } = req.query;
    let sql = `SELECT p.id, u.nome, u.email, p.cpf, p.telefone, p.status
               FROM pacientes p JOIN usuarios u ON u.id = p.usuario_id WHERE 1=1`;
    const params = [];
    if (busca) { sql += ' AND (u.nome LIKE ? OR u.email LIKE ?)'; params.push(`%${busca}%`, `%${busca}%`); }
    if (status) { sql += ' AND p.status = ?'; params.push(status); }
    sql += ' ORDER BY u.nome';
    const [rows] = await db.query(sql, params);
    res.json(rows);
  } catch (e) { console.error(e); res.status(500).json({ erro: 'Erro' }); }
};

exports.detalhePaciente = async (req, res) => {
  try {
    const [paciente] = await db.query(
      `SELECT p.*, u.nome, u.email FROM pacientes p
       JOIN usuarios u ON u.id = p.usuario_id WHERE p.id = ?`, [req.params.id]);
    if (!paciente.length) return res.status(404).json({ erro: 'Nao encontrado' });

    const [prescricoes] = await db.query(
      `SELECT pr.*, e.titulo FROM prescricoes pr JOIN exercicios e ON e.id = pr.exercicio_id
       WHERE pr.paciente_id = ? ORDER BY pr.criada_em DESC`, [req.params.id]);

    const [execucoes] = await db.query(
      `SELECT ex.*, e.titulo FROM execucoes ex
       JOIN prescricoes pr ON pr.id = ex.prescricao_id
       JOIN exercicios e ON e.id = pr.exercicio_id
       WHERE ex.paciente_id = ? ORDER BY ex.data_execucao DESC LIMIT 30`, [req.params.id]);

    res.json({ paciente: paciente[0], prescricoes, execucoes });
  } catch (e) { console.error(e); res.status(500).json({ erro: 'Erro' }); }
};

// ==================== EXERCICIOS ====================
exports.listarExercicios = async (req, res) => {
  try {
    const [rows] = await db.query(
      `SELECT e.*, c.nome AS categoria FROM exercicios e
       LEFT JOIN categorias c ON c.id = e.categoria_id
       WHERE e.ativo = TRUE ORDER BY e.titulo`);
    res.json(rows);
  } catch (e) { res.status(500).json({ erro: 'Erro' }); }
};

exports.criarExercicio = async (req, res) => {
  try {
    const { titulo, descricao, instrucoes, duracaoMinutos, categoriaId, videoUrl, imagemUrl, tags } = req.body;
    const [r] = await db.query(
      `INSERT INTO exercicios (titulo, descricao, instrucoes, duracao_minutos, categoria_id, video_url, imagem_url, tags)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [titulo, descricao, instrucoes, duracaoMinutos, categoriaId, videoUrl, imagemUrl, tags]
    );
    res.status(201).json({ id: r.insertId });
  } catch (e) { console.error(e); res.status(500).json({ erro: 'Erro' }); }
};

exports.atualizarExercicio = async (req, res) => {
  try {
    const { titulo, descricao, instrucoes, duracaoMinutos, categoriaId, videoUrl, imagemUrl, tags } = req.body;
    await db.query(
      `UPDATE exercicios SET titulo=?, descricao=?, instrucoes=?, duracao_minutos=?,
       categoria_id=?, video_url=?, imagem_url=?, tags=? WHERE id=?`,
      [titulo, descricao, instrucoes, duracaoMinutos, categoriaId, videoUrl, imagemUrl, tags, req.params.id]
    );
    res.json({ ok: true });
  } catch (e) { res.status(500).json({ erro: 'Erro' }); }
};

exports.excluirExercicio = async (req, res) => {
  try {
    await db.query('UPDATE exercicios SET ativo = FALSE WHERE id = ?', [req.params.id]);
    res.json({ ok: true });
  } catch (e) { res.status(500).json({ erro: 'Erro' }); }
};

// ==================== PRESCRICOES ====================
exports.criarPrescricao = async (req, res) => {
  try {
    const { pacienteId, exercicioId, frequencia, orientacoes, dataInicio, dataFim } = req.body;
    const [r] = await db.query(
      `INSERT INTO prescricoes (paciente_id, exercicio_id, profissional_id, frequencia, orientacoes, data_inicio, data_fim)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [pacienteId, exercicioId, req.usuario.id, frequencia, orientacoes, dataInicio, dataFim || null]
    );

    // notifica o paciente
    const [pac] = await db.query('SELECT usuario_id FROM pacientes WHERE id = ?', [pacienteId]);
    if (pac.length > 0) {
      const [ex] = await db.query('SELECT titulo FROM exercicios WHERE id = ?', [exercicioId]);
      await db.query(
        `INSERT INTO notificacoes (usuario_id, tipo, titulo, mensagem)
         VALUES (?, 'LEMBRETE_EXERCICIO', 'Novo exercicio prescrito', ?)`,
        [pac[0].usuario_id, `A Dra. Maya prescreveu: ${ex[0]?.titulo || 'exercicio'} - ${frequencia || ''}`]
      );
    }
    res.status(201).json({ id: r.insertId });
  } catch (e) { console.error(e); res.status(500).json({ erro: 'Erro' }); }
};

// ==================== AGENDAMENTOS ====================
exports.listarAgendamentos = async (req, res) => {
  try {
    const { dataInicio, dataFim } = req.query;
    let sql = `SELECT a.*, u.nome AS paciente, s.nome AS servico
               FROM agendamentos a
               JOIN pacientes p ON p.id = a.paciente_id
               JOIN usuarios u ON u.id = p.usuario_id
               JOIN servicos s ON s.id = a.servico_id WHERE 1=1`;
    const params = [];
    if (dataInicio) { sql += ' AND a.data >= ?'; params.push(dataInicio); }
    if (dataFim) { sql += ' AND a.data <= ?'; params.push(dataFim); }
    sql += ' ORDER BY a.data, a.horario';
    const [rows] = await db.query(sql, params);
    res.json(rows);
  } catch (e) { res.status(500).json({ erro: 'Erro' }); }
};

exports.atualizarStatusAgendamento = async (req, res) => {
  try {
    const { status } = req.body;
    await db.query('UPDATE agendamentos SET status = ? WHERE id = ?', [status, req.params.id]);
    res.json({ ok: true });
  } catch (e) { res.status(500).json({ erro: 'Erro' }); }
};

// ==================== PAGAMENTOS ====================
exports.registrarPagamento = async (req, res) => {
  try {
    const { pacienteId, agendamentoId, descricao, valor, formaPagamento, dataPagamento } = req.body;
    const [r] = await db.query(
      `INSERT INTO pagamentos (paciente_id, agendamento_id, descricao, valor, forma_pagamento, data_pagamento)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [pacienteId, agendamentoId || null, descricao, valor, formaPagamento, dataPagamento]
    );
    res.status(201).json({ id: r.insertId });
  } catch (e) { console.error(e); res.status(500).json({ erro: 'Erro' }); }
};

// ==================== DASHBOARD ADMIN ====================
exports.dashboardAdmin = async (req, res) => {
  try {
    const [stats] = await db.query(`
      SELECT
        (SELECT COUNT(*) FROM pacientes WHERE status = 'ATIVO') AS pacientes_ativos,
        (SELECT COUNT(*) FROM agendamentos WHERE data = CURDATE()) AS consultas_hoje,
        (SELECT COUNT(*) FROM execucoes WHERE data_execucao >= NOW() - INTERVAL 7 DAY) AS execucoes_semana,
        (SELECT COUNT(DISTINCT paciente_id) FROM execucoes WHERE data_execucao >= NOW() - INTERVAL 7 DAY) AS pacientes_ativos_semana
    `);
    res.json(stats[0]);
  } catch (e) { console.error(e); res.status(500).json({ erro: 'Erro' }); }
};
