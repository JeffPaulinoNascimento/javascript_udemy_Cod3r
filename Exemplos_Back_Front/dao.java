package br.com.preventsenior.plantao.dao;

import br.com.preventsenior.core.client.dao.generico.DAO;
import br.com.preventsenior.core.client.dao.generico.DAOException;
import br.com.preventsenior.corporativo.client.model.Especialidade;
import br.com.preventsenior.corporativo.client.model.SubEspecialidade;
import br.com.preventsenior.corporativo.client.model.enums.TelefoneDispositivoEnum;
import br.com.preventsenior.corporativo.client.utils.MaskUtils;
import br.com.preventsenior.credenciamento.model.LocalAtendimento;
import br.com.preventsenior.credenciamento.model.Profissional;
import br.com.preventsenior.plantao.dto.PlantaoDTO;
import br.com.preventsenior.plantao.dto.ProfissionalDTO;
import br.com.preventsenior.plantao.enums.TipoPagamentoUnidadeEnum;
import br.com.preventsenior.plantao.externo.vo.ResumoPlanejadoVO;
import br.com.preventsenior.plantao.model.*;
import br.com.preventsenior.plantao.model.enums.*;
import br.com.preventsenior.plantao.utils.CorEventoUtils;
import br.com.preventsenior.plantao.utils.CorEventoVO;
import br.com.preventsenior.plantao.vo.*;
import br.com.preventsenior.portalweb.client.vo.ChaveValorVO;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.inject.Inject;
import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlantaoPlanejadoDAO extends DAO<PlantaoPlanejado, Long> {

    @Inject
    private CorEventoUtils corEventoUtils;

    @Inject
    private LocalAtendimentoPlantaoDAO localAtendimentoPlantaoDAO;

    @Inject
    private ControleExtratoPagamentoDetalhadoDAO controleExtratoPagamentoDetalhadoDAO;

    @Override
    @Inject
    public void setEntityManager(EntityManager em) {
        this.setInternalEntityManager(em);
    }

    public Long buscarUsuarioPortal(String nome) {
        try {
            String queryString = "SELECT u.id FROM PW2.USUARIO u where u.nome = :nome and u.ativo = 1";
            Query q = this.em.createNativeQuery(queryString);
            q.setParameter("nome", nome);
            List<Object> result = q.getResultList();
            return result.isEmpty() ? 0 : Long.parseLong(result.get(0).toString());
        } catch(Exception e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public void procedureAusente(String dataInicio, String dataFim) throws DAOException {
        try {
            StoredProcedureQuery query = this.em.createNamedStoredProcedureQuery("RATEIO_ATUALIZA_AUSENTES");
            query.setParameter("INICIOPERIODO", dataInicio);
            query.setParameter("FIMPERIODO", dataFim);
            query.execute();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPlantoesSemDataCriacao() throws Exception {
        String jpql = " FROM " + this.clazz.getSimpleName() + " pp where pp.dataCriacao is null and pp.dataInicio >= :data";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
        query.setParameter("data", sdf.parse("01/12/2018"));

        return query.getResultList();
    }

    public PlantaoPlanejado buscarPlanejadoPorInformacoes(Date dataInicio, Date dataFim, Long idLocal, Long idProfissional) throws DAOException {
        try {
            String jpql = "FROM " + this.clazz.getSimpleName() + " pp where pp.localAtendimento.id = :idLocal and pp.dataInicio = :dataInicio  and pp.dataFim = :dataFim and pp.id.profissional.id = :idProfissional";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idLocal", idLocal);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idProfissional", idProfissional);

            PlantaoPlanejado planejado = query.getSingleResult();

            return planejado;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<HorasProgramadasVO> buscarPlanejadosPorProfissionalLocalPeriodo(Long idLocal, Date dataInicio, Date dataFim, Long idProfissional) {
        String jpql = "Select pp From " + this.clazz.getSimpleName() + " pp join pp.plantaoRealizado where pp.localAtendimento.id = :idLocal and (pp.dataInicio between :dataInicio and :dataFim or pp.dataFim between :dataInicio and :dataFim) and pp.profissional.id = :idProfissional";
        List<HorasProgramadasVO> planejados = new ArrayList<HorasProgramadasVO>();

        TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

        query.setParameter("idLocal", idLocal);
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);
        query.setParameter("idProfissional", idProfissional);

        List<PlantaoPlanejado> resultList = query.getResultList();

        for (PlantaoPlanejado result : resultList) {
            HorasProgramadasVO vo = new HorasProgramadasVO();
            vo.setDia(result.getDataInicio());
            vo.setQuantidadeHoras((result.getDataFim().getTime() - result.getDataInicio().getTime()) / (60 * 60 * 1000.00) % 24);
            planejados.add(vo);
        }

        return planejados;
    }

    public List<PlantaoPlanejado> buscarPorData(Date dataInicio, Date dataFim, Long idLocal) throws DAOException {
        try {
            String jpql = "FROM " + this.clazz.getSimpleName() + " pp where pp.localAtendimento.id = :idLocal and (pp.dataInicio between :dataInicio and :dataFim or pp.dataFim between :dataInicio and :dataFim)";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idLocal", idLocal);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorData(Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = "FROM " + this.clazz.getSimpleName() + " pp where (pp.dataInicio between :dataInicio and :dataFim or pp.dataFim between :dataInicio and :dataFim)";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ProfissionalPlantaoPlanejadoVO> buscarProfissionalPlantaoPorUnidadePorPeriodo(Date dataInicio, Date dataFim, Long idLocal) {
        String jpql = "select new br.com.preventsenior.plantao.vo.ProfissionalPlantaoPlanejadoVO(p.nome, p.id, count(pp)) from "
                + PlantaoPlanejado.class.getSimpleName()
                + " pp join pp.profissional p where pp.localAtendimento.id = :idLocal and pp.dataInicio between :dataInicio and :dataFim group by p.id, p.nome";
        Query query = this.em.createQuery(jpql);

        query.setParameter("idLocal", idLocal);
        query.setParameter("dataInicio", dataInicio);
        query.setParameter("dataFim", dataFim);

        return query.getResultList();
    }

    public List<PlantaoPlanejado> buscarPorDataProfissional(Date dataInicio, Date dataFim, Long idLocal, Long idProfissional) throws DAOException {
        try {
            String jpql = "FROM " + this.clazz.getSimpleName() + " pp where pp.localAtendimento.id = :idLocal and pp.dataInicio between :dataInicio and :dataFim and pp.profissional.id = :idProfissional order by pp.dataInicio";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idLocal", idLocal);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> checarSeExisteAgendamento(Date dataInicio, Date dataFim, Long idProfissional, Long idPlantao) throws DAOException {
        try {
            String jpql = "FROM " + PlantaoPlanejado.class.getSimpleName() + " pp "
                    + "where pp.id.profissional.id = :idProfissional "
                    + "and pp.id <> :idPlantao "
                    + "and ((:dataInicio between pp.dataInicio and pp.dataFim or :dataFim between pp.dataInicio and pp.dataFim) "
                    + "or (pp.dataInicio between :dataInicio and :dataFim or pp.dataFim between :dataInicio and :dataFim))";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idProfissional", idProfissional);
            query.setParameter("idPlantao", idPlantao == null ? 0 : idPlantao);
            query.setParameter("dataInicio", dataInicio, TemporalType.TIMESTAMP);
            query.setParameter("dataFim", dataFim, TemporalType.TIMESTAMP);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> checarSeExisteAgendamentoNaUnidade(Date dataInicio, Date dataFim, Long idProfissional, Long idUnidade) throws DAOException {
        try {
            String jpql = "FROM " + PlantaoPlanejado.class.getSimpleName() + " pp "
                    + "where pp.profissional.id = :idProfissional "
                    + "and ((:dataInicio between pp.dataInicio and pp.dataFim or :dataFim between pp.dataInicio and pp.dataFim) "
                    + "or (pp.dataInicio between :dataInicio and :dataFim or pp.dataFim between :dataInicio and :dataFim))";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idProfissional", idProfissional);
            query.setParameter("dataInicio", dataInicio, TemporalType.TIMESTAMP);
            query.setParameter("dataFim", dataFim, TemporalType.TIMESTAMP);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorDataLocalTipo(Date dataInicio, Date dataFim, Long idLocal, Long idTipoPlantao) throws DAOException {
        try {
            String jpql = "FROM " + PlantaoPlanejado.class.getSimpleName() + " pp where pp.localAtendimento.id = :idLocal and pp.tipoPlantao.id.tipoPlantao.id = :idTipoPlantao and pp.dataInicio >= :dataInicio and pp.dataFim <= :dataFim) ";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);

            query.setParameter("idTipoPlantao", idTipoPlantao);
            query.setParameter("idLocal", idLocal);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarCompromissos(Date dataInicio, Long idProfissional, Long idLocal) throws DAOException {
        try {
            String jpql = "FROM " + PlantaoPlanejado.class.getSimpleName() + " pp where pp.tipoPlantao.localAtendimento.id = :idLocal and pp.id.profissional.id = :idProfissional and pp.dataInicio >= :dataInicio) order by pp.dataInicio asc";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);

            query.setParameter("idProfissional", idProfissional);
            query.setParameter("idLocal", idLocal);
            query.setParameter("dataInicio", dataInicio);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorData(Date dataInicio, Date dataFim, Long idLocal, long idProfissional, Boolean atendeNow) throws DAOException {
        try {
            String jpql = "FROM " + this.clazz.getSimpleName() + " pp "
                    + "where pp.localAtendimento.id = :idLocal "
                    + "and pp.id.profissional.id = :idProfissional "
                    + "and pp.dataInicio between :dataInicio and :dataFim " + (atendeNow != null ? " and pp.atendeNow = :atendeNow" : "");

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idLocal", idLocal);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);

            if (atendeNow != null) {
                query.setParameter("atendeNow", TipoMarcacaoEnum.INDICACAO.getId());
            }

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<EventoCalendarioVO> buscarNativePorDataLocalTipo(Date dataInicio, Date dataFim, Long idLocal, List<Long> profissionais, List<Long> subespecialidades, List<Long> criadores) throws DAOException {

        if (profissionais != null) {
            profissionais.removeIf(Objects::isNull);
        }

        if (subespecialidades != null) {
            subespecialidades.removeIf(Objects::isNull);
        }

        if (criadores != null) {
            criadores.removeIf(Objects::isNull);
        }

        try {

            String queryString = "select distinct "
                    + "pp.ID,"
                    + "to_char(pp.NOME),"
                    + "pp.DT_HR_INICIO,"
                    + "pp.DT_HR_FIM,"
                    + "to_char(pp.NOME_COR),"
                    + "PP.ID_PROFISSIONAL,"
                    + "to_char(PF.NOME),"
                    + "pr.ID_SITUACAO_PLANTAO_REALIZADO, "
                    + "pp.ID_TIPO_MARCACAO, "
                    + "pp.ID_TIPO_ATENDIMENTO, "
                    + "pp.ID_STATUS_PLANTAO, "
                    + "tp.NOME "
                    + "from PLANTAO.PLANTAO_PLANEJADO pp "
                    + "LEFT JOIN PLANTAO.TIPO_PLANTAO_LOCAL_ATENDIMENTO tp on pp.ID_TIPO_PLANTAO_LOCAL_ATDM = tp.ID "
                    + "LEFT JOIN PLANTAO.PLANTAO_REALIZADO pr ON pp.ID = pr.ID "
                    + "LEFT JOIN CORPORATIVO.PESSOA_FISICA PF ON PF.ID = PP.ID_PROFISSIONAL "
                    + "LEFT JOIN PLANTAO.PLANTAO_PLANEJADO_MEDICO_ESPLD sb ON sb.ID_PLANTAO_PLANEJADO = pp.ID "
                    + "LEFT JOIN CORPORATIVO.SUB_ESPECIALIDADE_SAUDE ses ON ses.ID = sb.ID_SUB_ESPECIALIDADE "
                    + "LEFT JOIN CORPORATIVO.ESPECIALIDADE_SAUDE esp ON ESP.ID = ses.ID_ESPECIALIDADE_SAUDE "
                    + "where PP.ID_LOCAL_ATENDIMENTO_PLANTAO = :idLocal "
                    + (profissionais != null && !profissionais.isEmpty() && profissionais.size() > 0 ? " AND PP.ID_PROFISSIONAL IN (:profissionais) " : "")
                    + (subespecialidades != null && !subespecialidades.isEmpty() && subespecialidades.size() > 0 ? " AND SB.ID_SUB_ESPECIALIDADE IN (:subespecialidades) " : "")
                    + (criadores != null && !criadores.isEmpty() && criadores.size() > 0 ? " AND PP.ID_USUARIO IN (:criadores) " : "")
                    + "AND (pp.DT_HR_INICIO BETWEEN :dataInicio AND :dataFim "
                    + "OR  pp.DT_HR_FIM BETWEEN :dataInicio AND :dataFim "
                    + "OR  (pp.DT_HR_INICIO < :dataInicio AND :dataFim < pp.DT_HR_FIM ))";

            Query nativeQuery = em.createNativeQuery(queryString);

            nativeQuery.setParameter("dataInicio", dataInicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("dataFim", dataFim, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("idLocal", idLocal);

            if (profissionais != null && !profissionais.isEmpty() && profissionais.size() > 0) {
                nativeQuery.setParameter("profissionais", profissionais);
            }

            if (subespecialidades != null && !subespecialidades.isEmpty() && subespecialidades.size() > 0) {
                nativeQuery.setParameter("subespecialidades", subespecialidades);
            }

            if (criadores != null && !criadores.isEmpty() && criadores.size() > 0) {
                nativeQuery.setParameter("criadores", criadores);
            }


            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<EventoCalendarioVO> retorno = new ArrayList<>();
            DateTime agora = new DateTime();

            LocalAtendimentoPlantao local = localAtendimentoPlantaoDAO.find(idLocal);

            boolean pgtoAutomatico = !local.isApontamentoManual();
            DateTime ultimoFechamento = null;
            boolean unidadeAntecipacao = local.getTipoPagamentoUnidade().getId().equals(TipoPagamentoUnidadeEnum.ANTECIPACAO_DE_PAGAMENTO.getCodigo());

            if (unidadeAntecipacao) {
                Date dtFechamento = controleExtratoPagamentoDetalhadoDAO.verificarUltimaCompetencia();
                ultimoFechamento = new DateTime(dtFechamento).dayOfMonth().withMaximumValue().withTime(23, 59, 59, 999);
            }


            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    EventoCalendarioVO evento = new EventoCalendarioVO();

                    BigDecimal idPlantao = (BigDecimal) o[0];
                    Date inicio = (Date) o[2];
                    Date fim = (Date) o[3];
                    BigDecimal idProfissional = (BigDecimal) o[5];
                    BigDecimal statusPlanejado = (BigDecimal) o[7];
                    BigDecimal idTipoMarcacao = (BigDecimal) o[8];
                    BigDecimal tipoAtendimento = (BigDecimal) o[9];
                    BigDecimal idStatus = (BigDecimal) o[10];

                    evento.setId(idPlantao.longValue());
                    evento.setTitle((String) o[6] + ((String) o[1] == null ? "" : " - " + (String) o[1]));
                    evento.setStartLong(inicio.getTime());
                    evento.setEndLong(fim.getTime());
                    evento.setClassColor((String) o[4]);
                    evento.setNomeProfissional((String) o[6]);
                    evento.setIdTipoMarcacao(idTipoMarcacao != null ? idTipoMarcacao.longValue() : null);
                    evento.setMarcacaoIcone(idTipoMarcacao != null ? TipoMarcacaoEnum.get(idTipoMarcacao.longValue()).getIcone() : null);
                    evento.setIdProfissional(idProfissional.longValue());
                    evento.setTipoAtendimentoIcone((tipoAtendimento == null ? "" : TipoAtendimentoEnum.get(tipoAtendimento.longValue()).getIcone()));
                    evento.setTipoAtendimento(tipoAtendimento == null ? "naotem" : TipoAtendimentoEnum.get(tipoAtendimento.longValue()).getDescricao());
                    Long status = idStatus.longValue();

                    Long id = idPlantao.longValue();

                    evento.setTipoPlantao((String) o[11]);

                    //evento.setSubEspecialidade((String) o[12]);
                    //evento.setEspecialidade((String) o[13]);

                    evento.setAtivo(status == StatusPlantaoEnum.OCULTO.getCodigo() ? true : false);

                    DateTime inicioEvento = new DateTime(inicio);

                    /**REGRA ENTROU PARA VALIDAR O FECHAMENTO DO RATEIO, PROVAVELMENTE SERA REMOVIDA NO FUTURO***/

                    if (unidadeAntecipacao && ultimoFechamento != null) {
                        if (inicioEvento.isBefore(ultimoFechamento)) {
                            evento.setEditable(false);
                        }
                    } else {
                        if (agora.isAfter(inicioEvento)) {
                            evento.setEditable(false);
                        }

                        if (!pgtoAutomatico && (statusPlanejado != null && !SituacaoPlantaoEnum.get(statusPlanejado.longValue()).equals(SituacaoPlantaoEnum.AGUARDANDO_ENTRADA))) {
                            evento.setEditable(false);
                        }
                    }

                    if (!Strings.isNullOrEmpty((String) o[4])) {
                        CorEventoVO cor = corEventoUtils.buscarDadosDaClasse((String) o[4]);
                        evento.setColor(cor.getBackgroundColor());
                        evento.setTextColor(cor.getColor());
                    }

                    retorno.add(evento);
                }
            }
            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public Integer buscarQuantidadePlanejamentos(Long idUnidadeCredenciamento, Long idSubEspecialidade, Date data) throws DAOException {
        try {
            String jpql = "select count(pe.id) from " + PlantaoEspecialidade.class.getSimpleName() + " pe 	    " +
                    " where pe.subespecialidade.id = :idSubEspecialidade 								        " +
                    " and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento 		    " +
                    " and pe.plantao.dataInicio <= :data and pe.plantao.dataFim >= :data				        " +
                    "  and pe.plantao.tipoMarcacao.id <> :atendimentoVirtual 		                            " +
                    "  and pe.plantao.status.id in (:status)                                                    ";

            TypedQuery<Long> query = this.em.createQuery(jpql, Long.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);

            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());

            query.setParameter("data", data);
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());


            Integer qtd = query.getSingleResult().intValue();
            return qtd;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    public List<SubEspecialidade> buscarSubEspecialidadesPlanejadas(Date dataInicio, Date dataFim, Boolean atendeNow, Long idEspecialidade) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 							" +
                    "			where pe.plantao.dataInicio >= :dataInicio 												" +
                    " 			and   pe.plantao.dataFim <= :dataFim													" +
                    "           and   pe.plantao.status.id = :status                                                    ";
//					"			where ((pe.plantao.dataInicio between :dataInicio and :dataInicio) 						" +
//					"			or (pe.plantao.dataFim between :dataInicio and :dataFim) 								" +
//					"			or (pe.plantao.dataInicio <= :dataInicio and pe.plantao.dataFim >= :dataFim))			" ;

            if (atendeNow) {
                jpql += " and pe.plantao.tipoMarcacao.id <> :tipoMarcacao ";
            }

            if (idEspecialidade != null) {
                jpql += " and pe.subespecialidade.especialidade.id = :idEspecialidade									";
            }

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());
            if (atendeNow) {
                query.setParameter("tipoMarcacao", TipoMarcacaoEnum.VIRTUAL.getId());
            }

            if (idEspecialidade != null) {
                query.setParameter("idEspecialidade", idEspecialidade);
            }

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorDataEspecialidade(Long idSubEspecialidade, Long idLocalAtendimento, Date dataInicio, Date dataFim, Boolean atendeNow) throws DAOException {
        try {
            String jpql = "select distinct pe.plantao from " + PlantaoEspecialidade.class.getSimpleName() + " pe left join fetch pe.plantao.pausas pa "
                    + "where pe.plantao.localAtendimento.id = :idLocal "
                    + "and pe.subespecialidade.id = :idSubEspecialidade "
                    + "and ("
                    + "		(pe.plantao.dataInicio between :dataInicio and :dataFim) or "
                    + "		(pe.plantao.dataFim between :dataInicio and :dataFim) or "
                    + "		(pe.plantao.dataInicio <= :dataInicio and pe.plantao.dataFim >= :dataFim)"
                    + ")";

            if (atendeNow != null) {
                jpql += " and pe.plantao.tipoMarcacao.id = :tipoMarcacao ";
            }

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);

            query.setParameter("idLocal", idLocalAtendimento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            if (atendeNow != null) {
                if (atendeNow) {
                    query.setParameter("tipoMarcacao", TipoMarcacaoEnum.INDICACAO.getId());
                } else {
                    query.setParameter("tipoMarcacao", TipoMarcacaoEnum.AGENDAMENTO.getId());
                }
            }

            List<PlantaoPlanejado> planejamentos = query.getResultList();
            return planejamentos;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<AgendaVO> buscarEscalaPorPeriodo(Long idLocal, Date dataInicio, Date dataFim)
            throws DAOException {
        try {

            String query =
                    " SELECT"
                            + "     PP.DT_HR_INICIO,"
                            + "     PP.DT_HR_FIM,"
                            + "     PP.ID_PROFISSIONAL,"
                            + "     PF.NOME,"
                            + "		(SELECT CON.SIGLA_CONSELHO_REGL_NOME_MERC || '-' || CON.NUMERO_DOCUMENTO FROM CREDENCIAMENTO.PROFISSIONAL_DCTO_CNSLH_REGL CON WHERE CON.ID_PROFISSIONAL = PP.ID_PROFISSIONAL AND ROWNUM <= 1) AS CONSELHO,"
                            + "		(SELECT TL.CODIGO_DDD || TL.NUMERO_DISPOSITIVO FROM CORPORATIVO.TELEFONE TL " +
                            "           WHERE TL.ID = (SELECT MAX(TP.ID_TELEFONE) FROM CORPORATIVO.TELEFONE_PESSOA TP "
                            +"                          INNER JOIN CORPORATIVO.TELEFONE TLL ON TLL.ID = TP.ID_TELEFONE"+
                            "               WHERE TP.ID_PESSOA = PP.ID_PROFISSIONAL AND TLL.ID_TIPO_TELEFONE =" + TelefoneDispositivoEnum.CELULAR.getCodigo() + ")) AS TEL1,"
                            + "		(SELECT TL.CODIGO_DDD || TL.NUMERO_DISPOSITIVO FROM CORPORATIVO.TELEFONE TL " +
                            "           WHERE TL.ID = (SELECT MAX(TP.ID_TELEFONE) FROM CORPORATIVO.TELEFONE_PESSOA TP "
                            +"                          INNER JOIN CORPORATIVO.TELEFONE TLL ON TLL.ID = TP.ID_TELEFONE"+
                            "               WHERE TP.ID_PESSOA = PP.ID_PROFISSIONAL AND TLL.ID_TIPO_TELEFONE =" + TelefoneDispositivoEnum.COMERCIAL.getCodigo() + ")) AS TEL2,"
                            + "		PP.NOME_COR, "
                            + "     case when PR.ID_SITUACAO_PLANTAO_REALIZADO = :statusAusente then 1 else 0 end,"
                            + "     PA.id, PA.DT_HR_INICIO as gora_aprov "
                            + " FROM"
                            + "     PLANTAO.PLANTAO_PLANEJADO PP "
                            + "   LEFT JOIN PLANTAO.PLANTAO_REALIZADO PR on PR.ID = PP.ID "
                            + "   LEFT JOIN PLANTAO.PLANTAO_APROVACAO PA on PA.ID = PP.ID "
                            + " INNER JOIN"
                            + "     CORPORATIVO.PESSOA_FISICA PF"
                            + " ON"
                            + "		PP.ID_PROFISSIONAL = PF.ID"
                            + " WHERE"
                            + "     PP.ID_LOCAL_ATENDIMENTO_PLANTAO = :idLocal"
                            + " AND PP.DT_HR_INICIO BETWEEN :dataInicio AND :dataFim"
                            + " ORDER BY"
                            + "     PP.DT_HR_INICIO ASC";

                Query nativeQuery = em.createNativeQuery(query);
                nativeQuery.setParameter("idLocal", idLocal);
                nativeQuery.setParameter("dataInicio", dataInicio, TemporalType.TIMESTAMP);
                nativeQuery.setParameter("dataFim", dataFim, TemporalType.TIMESTAMP);
                nativeQuery.setParameter("statusAusente", SituacaoPlantaoEnum.AUSENTE.getCodigo());

                @SuppressWarnings("unchecked")
                List<Object[]> resultados = nativeQuery.getResultList();
                List<AgendaVO> retorno = new ArrayList<>();

                if (!resultados.isEmpty()) {
                    for (Object[] o : resultados) {
                        AgendaVO linha = new AgendaVO();
                        BigDecimal idProf = (BigDecimal) o[2];
                        linha.setIdProfissional(idProf.longValue());
                        linha.setNomeProfissional((String) o[3]);

                        linha.setDataInicioHorario((Date) o[0]);
                        linha.setDataFimHorario((Date) o[1]);

                        linha.setCrm((String) o[4]);
                        linha.setTelefone1((String) o[5]);
                        linha.setTelefone2((String) o[6]);

                        linha.setColor((String) o[7]);

                        if (!Strings.isNullOrEmpty(linha.getTelefone1())) {
                            if (linha.getTelefone1().length() == 11) {
                                linha.setTelefone1(MaskUtils.formatarString(linha.getTelefone1().trim().replaceAll("[^0-9.]", ""), "(##) #####-####"));
                            } else {
                                linha.setTelefone1(MaskUtils.formatarString(linha.getTelefone1().trim().replaceAll("[^0-9.]", ""), "(##) ####-####"));
                            }
                        }

                        if (!Strings.isNullOrEmpty(linha.getTelefone2())) {
                            if (linha.getTelefone2().length() == 11) {
                                linha.setTelefone2(MaskUtils.formatarString(linha.getTelefone2().replaceAll("[^0-9.]", ""), "(##) #####-####"));
                            } else {
                                linha.setTelefone2(MaskUtils.formatarString(linha.getTelefone2().replaceAll("[^0-9.]", ""), "(##) ####-####"));
                            }
                        }

                        if ((o[9] != null && o[10] == null) || (o[9] == null && o[8].toString().equals("1"))) {
                            linha.setAusente(true);
                        }

                        retorno.add(linha);
                    }
                }

                return retorno;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoAprovacaoVO> buscarPlantoesNaoApontados(Date dataInicio, Date dataFim, Long idLocal) throws DAOException {

        try {
            String queryString =
                    " SELECT"
                            + "     PP.ID_PROFISSIONAL,"
                            + "     CONSELHO.NUMERO_DOCUMENTO,"
                            + "     PF.NOME ,"
                            + "     COUNT (PP.ID) AS NAO_APONTADOS"
                            + " FROM"
                            + "     PLANTAO.PLANTAO_PLANEJADO PP"
                            + " INNER JOIN"
                            + "     CREDENCIAMENTO.PROFISSIONAL PROF"
                            + " ON"
                            + "     PP.ID_PROFISSIONAL = PROF.ID"
                            + " INNER JOIN"
                            + "     CORPORATIVO.PESSOA_FISICA PF"
                            + " ON"
                            + "     PROF.ID = PF.ID"
                            + " INNER JOIN"
                            + "     CREDENCIAMENTO.PROFISSIONAL_DCTO_CNSLH_REGL CONSELHO"
                            + " ON"
                            + "     CONSELHO.ID_PROFISSIONAL = PROF.ID"
                            + " WHERE"
                            + "     pp.ID_LOCAL_ATENDIMENTO_PLANTAO = :local"
                            + " AND PP.ID NOT IN"
                            + "     ("
                            + "         SELECT"
                            + "             PR.ID"
                            + "         FROM"
                            + "             PLANTAO.PLANTAO_REALIZADO PR"
                            + "         WHERE"
                            + "             PR.ID = PP.ID"
                            + "         AND pr.ID_SITUACAO_PLANTAO_REALIZADO IN (:status) )"
                            + " AND PP.DT_HR_INICIO BETWEEN :dataInicio AND :dataFim"
                            + " GROUP BY"
                            + "     pp.ID_PROFISSIONAL,"
                            + "     CONSELHO.NUMERO_DOCUMENTO,"
                            + "     PF.NOME";

            Query nativeQuery = em.createNativeQuery(queryString);

            nativeQuery.setParameter("dataInicio", dataInicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("dataFim", dataFim, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("local", idLocal);

            List<Long> status = new ArrayList<>();
            status.add(SituacaoPlantaoEnum.ENCERRADO.getCodigo());
            status.add(SituacaoPlantaoEnum.AUSENTE.getCodigo());

            nativeQuery.setParameter("status", status);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<PlantaoAprovacaoVO> retorno = new ArrayList<>();

            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    PlantaoAprovacaoVO linha = new PlantaoAprovacaoVO();

                    BigDecimal idProf = (BigDecimal) o[0];
                    BigDecimal naoApontados = (BigDecimal) o[3];

                    linha.setIdProfissional(idProf.longValue());
                    linha.setCrm((String) o[1]);
                    linha.setNomeProfissional((String) o[2]);
                    linha.setPlantoesNaoApontados(naoApontados.longValue());
                    linha.setPlantoesNaoAprovados(0l);
                    linha.setPlantoesNaoLiberados(0l);

                    retorno.add(linha);
                }
            }

            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<ProfissionalDTO> buscarProfissionaisPorEspecialidade(Date dataInicio, Date dataFim, Long idSubEspecialidade, String sexo) throws DAOException {
        try {
            String jpql = " select distinct new br.com.preventsenior.plantao.dto.ProfissionalDTO(					" +
                    "			profissional.id, 																	" +
                    "			profissional.nome, 																	" +
                    "			profissional.sexo, 																	" +
                    "			cons.numero) 																		" +
                    "			from PlantaoEspecialidade pe 														" +
                    "			join pe.plantao.profissional profissional							 				" +
                    "			join profissional.conselhos cons													" +
                    " 			join cons.id.conselhoRegionalNomeMercado csr 										" +
                    " 			join pe.plantao pl                           										" +
                    "			where pe.plantao.dataInicio >= :dataInicio 							  				" +
                    "			and pe.plantao.dataFim <= :dataFim 													" +
                    "			and pe.subespecialidade.id = :idSubEspecialidade			      					" +
                    "			and pl.tipoMarcacao.id <> :atendimentoVirtual			      					    " +
                    "           and pe.plantao.status.id = :status                                                  ";

            if(sexo != null){
                jpql += " and pe.plantao.profissional.sexo = :sexo ";
            }


            TypedQuery<ProfissionalDTO> query = this.em.createQuery(jpql, ProfissionalDTO.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            if(sexo != null){
                query.setParameter("sexo", sexo.toUpperCase());
            }

            List<ProfissionalDTO> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorDataEspecialidadeProfissional(Long idSubEspecialidade, Long idLocalAtendimentoCredenciamento, Date dataInicio, Date dataFim, Long idProfissional, boolean atendeNow, boolean atendeAgendamento, String sexo, List<Long> idsGrupos) throws DAOException {
        try {
            String jpql = "select distinct pl from PlantaoEspecialidade pe 											" +
                    " 			join pe.plantao pl 																	" +
                    "           left join pl.grupos grupo                                    " +
                    "			left join fetch pl.pausas pa 														" +
                    "        	join fetch pl.profissional pf 														" +
                    "			where pl.localAtendimento.localAtendimento.id = :idLocalAtendimentoCredenciamento	" +
                    "			and pe.subespecialidade.id = :idSubEspecialidade 									" +
                    "           and pl.status.id = :status                                                          " +
                    "			and (grupo is null " + (idsGrupos == null || idsGrupos.isEmpty() ? "" : " or grupo.id.grupoMarcacao.id in :idsGrupos") + ")" +
                    "			and (																				" +
                    "				(pl.dataInicio between :dataInicio and :dataFim) or 							" +
                    "				(pl.dataFim between :dataInicio and :dataFim) or 								" +
                    "				(pl.dataInicio <= :dataInicio and pl.dataFim >= :dataFim)						" +
                    "			)																					";


            if (atendeNow && !atendeAgendamento) {
                jpql += "           and  pe.plantao.tipoMarcacao.id = :atendeNow                             ";
            }

            if (!atendeNow && atendeAgendamento) {
                jpql += "           and  pe.plantao.tipoMarcacao.id = :atendeAgendamento                     ";
            }

            if (atendeNow && atendeAgendamento) {
                jpql += "           and ( pe.plantao.tipoMarcacao.id = :atendeAgendamento or                  ";
                jpql += "                 pe.plantao.tipoMarcacao.id = :atendeNow  )	                         ";

            }

            if (idProfissional != null) {
                jpql += " 	and pf.id = :idProfissional												";
            }

            if (sexo != null) {
                jpql += " 	and pf.sexo = :sexo												";
            }

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);

            query.setParameter("idLocalAtendimentoCredenciamento", idLocalAtendimentoCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());
            if (idsGrupos != null && !idsGrupos.isEmpty()) {
                query.setParameter("idsGrupos", idsGrupos);
            }

            if (atendeAgendamento) {
                query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            }
            if (atendeNow) {
                query.setParameter("atendeNow", TipoMarcacaoEnum.INDICACAO.getId());
            }

            if (idProfissional != null) {
                query.setParameter("idProfissional", idProfissional);
            }

            if (sexo != null) {
                query.setParameter("sexo", sexo.toUpperCase());
            }

            List<PlantaoPlanejado> planejamentos = query.getResultList();
            //fetch horas extras
            planejamentos.forEach(pp -> pp.getHorasExtras().size());
            return planejamentos;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorDataEspecialidadeMedico(Long idSubEspecialidade, Long idLocalAtendimento, Date dataInicio, Date dataFim, Boolean atendeNow, String crm) throws DAOException {
        try {
            String jpql = "select distinct pe.plantao from PlantaoEspecialidade pe 									" +
                    "			left join fetch pe.plantao.pausas pa 												" +
                    "			where pe.plantao.localAtendimento.id = :idLocalAtendimento 		                    " +
                    "			and pe.subespecialidade.id = :idSubEspecialidade 									" +
                    "           and pe.plantao.status.id = :status                                                  " +
                    "			and (																				" +
                    "				(pe.plantao.dataInicio between :dataInicio and :dataFim) or 					" +
                    "				(pe.plantao.dataFim between :dataInicio and :dataFim) or 						" +
                    "				(pe.plantao.dataInicio <= :dataInicio and pe.plantao.dataFim >= :dataFim)		" +
                    "			)																					";
            if (atendeNow != null) {
                jpql += " and pe.plantao.tipoMarcacao.id = :tipoMarcacao  ";
            }

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);

            query.setParameter("idLocalAtendimento", idLocalAtendimento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            if (atendeNow != null) {
                if (atendeNow) {
                    query.setParameter("tipoMarcacao", TipoMarcacaoEnum.INDICACAO.getId());
                } else {
                    query.setParameter("tipoMarcacao", TipoMarcacaoEnum.AGENDAMENTO.getId());
                }
            }

            List<PlantaoPlanejado> planejamentos = query.getResultList();
            return planejamentos;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public PlantaoPlanejado buscarPlantao(Long idPlantao) throws DAOException {
        try {
            String jpql = "	select pl from PlantaoEspecialidade pe 					" +
                    " 			join pe.plantao pl 									" +
                    "        	join fetch pl.profissional pf 						" +
                    "			join fetch pf.conselhos cons						" +
                    "			where pl.id = :idPlantao							";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("idPlantao", idPlantao);

            PlantaoPlanejado plantao = query.getSingleResult();
            return plantao;

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Especialidade> buscarEspecialidadesPlanejadas(Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade.especialidade from PlantaoEspecialidade pe 				" +
                    "			where pe.plantao.dataInicio >= :dataInicio 												" +
                    " 			and   pe.plantao.dataFim <= :dataFim													" +
                    "           and   pe.plantao.tipoMarcacao.id <> :tipoMarcacao                                       " +
                    "           and   pe.plantao.status.id = :status                                                    " +
                    "		   order by pe.subespecialidade.especialidade.nome 											";

            TypedQuery<Especialidade> query = this.em.createQuery(jpql, Especialidade.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("tipoMarcacao", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());


            List<Especialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarEspecialidadeESubEspecialidadePlanejadas(Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct sub from PlantaoEspecialidade pe 									    " +
                    "			inner join pe.plantao pl														    " +
                    "			inner join pe.subespecialidade sub												    " +
                    "			inner join fetch sub.especialidade esp											    " +
                    "			where pl.dataInicio >= :dataInicio 												    " +
                    " 			and pl.dataFim <= :dataFim														    " +
                    " 			and (pl.tipoMarcacao.id = :atendeNow or pl.tipoMarcacao.id = :atendeAgendamento)  	" +
                    "           and pl.status.id = :status                                                          " +
                    "			order by esp.nome																    ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("atendeNow", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    public List<SubEspecialidade> buscarEspecialidadePorIdProfissional(Date dataInicio, Date dataFim, Long idProfissional) throws DAOException {
        try {
            String jpql = " select distinct sub from PlantaoEspecialidade pe 									    " +
                    "			inner join pe.plantao pl														    " +
                    "			inner join pe.subespecialidade sub												    " +
                    "			inner join pl.profissional pf													    " +
                    "			inner join fetch sub.especialidade esp											    " +
                    "			where pl.dataInicio >= :dataInicio 												    " +
                    " 			and pl.dataFim <= :dataFim														    " +
                    " 			and (pl.tipoMarcacao.id = :atendeNow or pl.tipoMarcacao.id = :atendeAgendamento)    " +
                    " 			and pf.id = :idProfissional														    " +
                    "           and pl.status.id = :status                                                          " +
                    "			order by esp.nome																    ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("atendeNow", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPlanejados(Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct pf from PlantaoEspecialidade pe 					                    " +
                    "			inner join pe.plantao pl										                    " +
                    "			inner join pl.profissional pf									                    " +
                    "			where pl.dataInicio >= :dataInicio 								                    " +
                    " 			and pl.dataFim <= :dataFim										                    " +
                    " 			and (pl.tipoMarcacao.id = :atendeNow or pl.tipoMarcacao.id = :atendeAgendamento)    " +
                    "           and pl.status.id = :status                                                          " +
                    "			order by pf.nome												                    ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("atendeNow", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<Profissional> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    public List<PlantaoPlanejado> verificarPlantaoDeSubEspecialidade(Date dataInicio, Long idProfissional, Long idLocal) throws DAOException {
        try {
            String jpql = "FROM " + PlantaoPlanejado.class.getSimpleName() + " pp where pp.tipoPlantao.localAtendimento.id = :idLocal and pp.id.profissional.id = :idProfissional and pp.dataInicio >= :dataInicio) order by pp.dataInicio asc";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);

            query.setParameter("idProfissional", idProfissional);
            query.setParameter("idLocal", idLocal);
            query.setParameter("dataInicio", dataInicio);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Long> buscarPlantoesSemSubEspecialidade() throws DAOException {
        try {
            String jpql = "select pp.id FROM " + PlantaoPlanejado.class.getSimpleName() + " pp left join pp.especialidades esp where esp is null order by pp.id asc";

            TypedQuery<Long> query = this.em.createQuery(jpql, Long.class);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    public List<LocalAtendimentoPlantao> buscarTodasUnidadesComPlantao() throws DAOException {
        try {
            String jpql = " select distinct pp.localAtendimento from PlantaoPlanejado pp 			" +
                    "				where pp.dataFim > :hoje										" +
                    "				and pp.tipoMarcacao.id <> :tipoMarcacao						    " +
                    "               and pp.status.id = :status                                      ";

            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("hoje", new Date());
            query.setParameter("tipoMarcacao", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<LocalAtendimentoPlantao> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPorUnidade(Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoPlanejado pp 						                    " +
                    "				where pp.dataFim > :hoje												                    " +
                    "				and pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	                    " +
                    "				and (pp.tipoMarcacao.id = :atendimentoIndicacao or pp.tipoMarcacao.id = :atendeAgendamento)	" +
                    "               and pp.status.id = :status                                                                  ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("hoje", new Date());
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoIndicacao", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());
            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubEspecialidadesPlanejadasPorUnidade(Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						                                    " +
                    "			where pe.plantao.dataFim > :hoje													                                    " +
                    "			and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento		                                    " +
                    "			and (pe.plantao.tipoMarcacao.id = :atendimentoIndicacao or pe.plantao.tipoMarcacao.id = :atendeAgendamento)				" +
                    "           and pe.plantao.status.id = :status                                                                                      ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("hoje", new Date());
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoIndicacao", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubEspecialidadesPlanejadasPorMedico(Long idMedico, Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						                                " +
                    "			where pe.plantao.dataFim > :hoje													                                " +
                    "			and pe.plantao.profissional.id = :idMedico											                                " +
                    "			and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento					                    " +
                    "			and (pe.plantao.tipoMarcacao.id = :atendimentoIndicacao or pe.plantao.tipoMarcacao.id = :atendeAgendamento)			" +
                    "           and pe.plantao.status.id = :status                                                                                  ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("hoje", new Date());
            query.setParameter("idMedico", idMedico);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoIndicacao", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPorSubEspecialidade(Long idSubEspecialidade, Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.plantao.profissional from PlantaoEspecialidade pe 					                                " +
                    "				where pe.plantao.dataFim > :hoje												                                " +
                    "				and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	                                " +
                    "				and pe.subespecialidade.id = :idSubEspecialidade								                                " +
                    "				and (pe.plantao.tipoMarcacao.id = :atendimentoIndicacao or pe.plantao.tipoMarcacao.id = :atendeAgendamento)		" +
                    "               and pe.plantao.status.id = :status                                                                              ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("hoje", new Date());
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("atendimentoIndicacao", TipoMarcacaoEnum.INDICACAO.getId());
            query.setParameter("atendeAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    /**
     * Busca plantao com preferencia
     * limitando no maximo 1 resultado, pq o profissional pode ter dois plantao da mesma especialidade
     * ex:
     * PLANTAO A: 7:00 até 12:00
     * PLANTAO B: 12:00 até 17:00
     * <p>
     * uma indicação para as 12 deve ser para o plantao B que inicia as 12
     */
    public PlantaoPlanejado buscarPlantaoPlanejado(Long idUnidadeCredenciamento, Long idSubEspecialidade, Long idProfissional, Date data, List<Long> idsGrupos) throws DAOException {

        try {
            String jpql = " select distinct pp from PlantaoEspecialidade pe 								" +
                    "			join pe.plantao pp															" +
                    "               left join pp.grupos grupo                                    " +
                    "			join fetch  pp.localAtendimento la											" +
                    "			join fetch  pp.profissional prof											" +
                    "			join fetch  prof.conselhos c     											" +
                    "			where la.localAtendimento.id = :idUnidadeCredenciamento		                " +
                    "			and pe.subespecialidade.id = :idSubEspecialidade							" +
                    "			and pp.profissional.id = :idProfissional									" +
                    "			and (grupo is null " + (idsGrupos == null || idsGrupos.isEmpty() ? "" : " or grupo.id.grupoMarcacao.id in :idsGrupos") + ")" +
                    "			and :data >= pp.dataInicio													" +
                    "           and :data < pp.dataFim														" +
                    "           and pp.status.id in (:status)                                               ";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("data", data);
            if (idsGrupos != null && !idsGrupos.isEmpty()) {
                query.setParameter("idsGrupos", idsGrupos);
            }
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());
            query.setMaxResults(1);

            PlantaoPlanejado plantaoPlanejado = query.getSingleResult();

            return plantaoPlanejado;

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    /**
     * usado no encaixe de indicação
     */
    public List<LocalAtendimentoPlantao> buscarUnidadesPorDataHora(Date data, Boolean atendimentoVirtual) throws DAOException {
        try {
            String jpql = " select distinct pp.localAtendimento from PlantaoPlanejado pp 			" +
                    "				where :data between pp.dataInicio and pp.dataFim				" +
                    "               and pp.status.id in (:status)                                   ";
            if (atendimentoVirtual != null && atendimentoVirtual) {
                jpql += "				and pp.tipoMarcacao.id = :atendimentoVirtual					";
            }

            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("data", data);
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            if (atendimentoVirtual != null && atendimentoVirtual) {
                query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            }

            List<LocalAtendimentoPlantao> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    /**
     * usado no encaixe de indicações
     */
    public List<Profissional> buscarMedicosPorUnidadeDataHora(Long idUnidadeCredenciamento, Date data, Boolean atendimentoVirtual) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoPlanejado pp 						" +
                    "				where :data between pp.dataInicio and pp.dataFim						" +
                    "				and pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	" +
                    "               and pp.status.id in (:status)                                           ";

            if (atendimentoVirtual != null && atendimentoVirtual) {
                jpql += "				and pp.tipoMarcacao.id = :atendimentoVirtual					";
            }


            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("data", data);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            if (atendimentoVirtual) {
                query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            }

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubPorMedicoUnidadeDataHora(Long idMedico, Long idUnidadeCredenciamento, Date data, Boolean atendimentoVirtual) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						" +
                    "			where :data between pe.plantao.dataInicio and pe.plantao.dataFim					" +
                    "			and pe.plantao.profissional.id = :idMedico											" +
                    "			and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento		" +
                    "           and  pe.plantao.status.id in (:status)                                              ";

            if (atendimentoVirtual != null && atendimentoVirtual) {
                jpql += "				and pe.plantao.tipoMarcacao.id = :atendimentoVirtual					";
            }


            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("data", data);
            query.setParameter("idMedico", idMedico);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());
            if (atendimentoVirtual) {
                query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            }

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoDTO> buscarPlantoesParaMigracao(Long idMedico, Long idUnidadeCredenciamento, Long idSubEspecialidade, Date data) throws DAOException {

        try {

            String jpql = " select new br.com.preventsenior.plantao.dto.PlantaoDTO(										" +
                    "				p.id, 																				" +
                    "				prof.nome, 																            " +
                    "				prof.sexo,																            " +
                    "				prof.id,																	        " +
                    "				sub.nome,															                " +
                    "				sub.id,																                " +
                    "				p.dataInicio,																		" +
                    "				p.dataFim,																		   	" +
                    "				lc.apelido,									                                        " +
                    "				lp.id,                                                                              " +
                    "               lc.id,                                                                              " +
                    "              CASE WHEN tm is NULL THEN 0L ELSE tm.id END,                                         " +
                    "               p.duracaoConsulta )	 									                            " +
                    "			from PlantaoEspecialidade pe 															" +
                    "			join pe.plantao p 	                                                                    " +
                    "           join p.profissional prof	                                                            " +
                    "           join p.localAtendimento lp                                                              " +
                    "           join lp.localAtendimento lc	                                                            " +
                    "           join pe.subespecialidade sub	                                                        " +
                    "           left join p.tipoMarcacao tm														        " +
                    "			where sub.id = :idSubEspecialidade										                " +
                    "			and prof.id = :idProfissional												            " +
                    "			and lc.id = :idUnidadeCredenciamento	        		                                " +
                    "			and (tm.id <> :atendimentoVirtual or tm is null)				                        " +
                    "			and trunc(p.dataInicio) = trunc(:data)													" +
                    "           and p.status.id in (:status)                                                            ";

            TypedQuery<PlantaoDTO> query = this.em.createQuery(jpql, PlantaoDTO.class);
            query.setParameter("idProfissional", idMedico);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("data", data);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<PlantaoDTO> plantoes = query.getResultList();

            return plantoes;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<LocalAtendimentoPlantao> buscarUnidadesPorData(Date data) throws DAOException {
        try {
            String jpql = " select distinct pp.localAtendimento from PlantaoPlanejado pp 			                    " +
                    "				where trunc(pp.dataInicio) = trunc(:data)						                    " +
                    "				and (pp.tipoMarcacao.id <> :atendimentoVirtual or pp.tipoMarcacao is null)          " +
                    "               and pp.status.id in (:status)                                                       ";

            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("data", data);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<LocalAtendimentoPlantao> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPorUnidadeData(Long idUnidadeCredenciamento, Date data) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoPlanejado pp 						                        " +
                    "				where trunc(pp.dataInicio) = trunc(:data) 								                        " +
                    "				and (pp.tipoMarcacao.id <> :atendimentoVirtual	or pp.tipoMarcacao is null)						" +
                    "				and pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	                        " +
                    "               and pp.status.id in (:status)                                                                   ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("data", data);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubPorMedicoUnidadeData(Long idMedico, Long idUnidadeCredenciamento, Date data) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						                                    " +
                    "			where trunc(pe.plantao.dataInicio) = trunc(:data)									                                    " +
                    "			and pe.plantao.profissional.id = :idMedico											                                    " +
                    "			and (pe.plantao.tipoMarcacao.id <> :atendimentoVirtual or pe.plantao.tipoMarcacao is null)								" +
                    "			and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento		                                    " +
                    "           and pe.plantao.status.id in (:status)                                                                                   ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("data", data);
            query.setParameter("idMedico", idMedico);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<LocalAtendimentoPlantao> buscarUnidadesPorRangeData(Long idPlantao, Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct pp.localAtendimento from PlantaoPlanejado pp 				                            " +
                    "				where pp.dataInicio <= :dataInicio									                            " +
                    "				and pp.dataFim >= :dataFim											                            " +
                    "				and (pp.tipoMarcacao.id <> :atendimentoVirtual or pp.tipoMarcacao is null)						" +
                    "				and pp.id <> :idPlantao												                            " +
                    "               and pp.status.id in (:status)                                                                   ";

            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idPlantao", idPlantao);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<LocalAtendimentoPlantao> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPorUnidadeRageData(Long idPlantao, Long idUnidadeCredenciamento, Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoPlanejado pp 						                        " +
                    "				where pp.dataInicio <= :dataInicio		 								                        " +
                    "				and pp.dataFim >= :dataFim												                        " +
                    "				and pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	                        " +
                    "				and (pp.tipoMarcacao.id <> :atendimentoVirtual	or pp.tipoMarcacao is null)						" +
                    "				and pp.id <> :idPlantao													                        " +
                    "               and pp.status.id in (:status)                                                                   ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idPlantao", idPlantao);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubPorMedicoUnidadeRageData(Long idPlantao, Long idMedico, Long idUnidadeCredenciamento, Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						            " +
                    "			where pe.plantao.dataInicio <= :dataInicio											            " +
                    "			and pe.plantao.dataFim >= :dataFim													            " +
                    "			and pe.plantao.profissional.id = :idMedico											            " +
                    "			and pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento		            " +
                    "			and (pe.plantao.tipoMarcacao.id <> :atendimentoVirtual	or 	pe.plantao.tipoMarcacao is null)	" +
                    "			and pe.plantao.id <> :idPlantao														            " +
                    "           and pe.plantao.status.id in (:status)                                                           ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("idMedico", idMedico);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idPlantao", idPlantao);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoDTO> buscarPlantoesDestinoParaMigracao(Long idMedico, Long idUnidadeCredenciamento, Long idSubEspecialidade, Date dataInicio, Date dataFim) throws DAOException {
        try {

            String jpql = " select new br.com.preventsenior.plantao.dto.PlantaoDTO(										" +
                    "				p.id, 																				" +
                    "				prof.nome, 																           	" +
                    "				prof.sexo,																           	" +
                    "				prof.id,																	        " +
                    "				sub.nome,															               	" +
                    "				sub.id,																               	" +
                    "				p.dataInicio,																	    " +
                    "				p.dataFim,																		    " +
                    "				lc.apelido,									                         			    " +
                    "				lp.id,                                                                              " +
                    "               lc.id,                                                                              " +
                    "               CASE WHEN tm is NULL THEN 0L ELSE tm.id END,                                        " +
                    "               p.duracaoConsulta )	 									                            " +
                    "			from PlantaoEspecialidade pe 															" +
                    "			join pe.plantao p                                                                       " +
                    "           join p.profissional prof	                                                            " +
                    "           join p.localAtendimento lp                                                              " +
                    "			join lp.localAtendimento lc																" +
                    "           join pe.subespecialidade sub                                                            " +
                    "           left join p.tipoMarcacao tm	                                                            " +
                    "			where sub.id = :idSubEspecialidade										                " +
                    "			and prof.id = :idMedico													            	" +
                    "			and lc.id = :idUnidadeCredenciamento					                                " +
                    "			and p.dataInicio <= :dataInicio															" +
                    "			and (tm.id <> :atendimentoVirtual or tm is null)     			                        " +
                    "			and p.dataFim >= :dataFim																" +
                    "           and p.status.id in (:status)                                                            ";

            TypedQuery<PlantaoDTO> query = this.em.createQuery(jpql, PlantaoDTO.class);
            query.setParameter("idMedico", idMedico);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.getCodigoVisiveis());

            List<PlantaoDTO> plantoes = query.getResultList();

            return plantoes;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public LocalAtendimentoPlantao buscarUnidadePorCnpj(String cnpj) throws DAOException {
        try {
            String jpql = " select distinct pp.localAtendimento from PlantaoPlanejado pp 				" +
                    "				where pp.localAtendimento.localAtendimento.documento = :cnpj		";

            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("cnpj", cnpj);
            query.setMaxResults(1);


            LocalAtendimentoPlantao local = query.getSingleResult();
            return local;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubEspecialidadesPrestadorExterno(Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						" +
                    "			join pe.subespecialidade sub 														" +
                    "			join fetch sub.especialidade esp													" +
                    "			where pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento  	" +
                    "			and trunc(pe.plantao.dataInicio) >= trunc(:hoje)									" +
                    "			order by esp.nome																	";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("hoje", new Date());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPrestadorExterno(Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoPlanejado pp 						 " +
                    "				where pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento " +
                    "				and trunc(pp.dataInicio) >= trunc(:hoje)								 " +
                    "				order by pp.profissional.nome											 ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("hoje", new Date());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosSubUnidadeExterno(Long idSub, Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoEspecialidade pe					 " +
                    "				join pe.plantao pp 														 " +
                    "				where pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento " +
                    "				and pe.subespecialidade.id = :idSub										 " +
                    "				and trunc(pp.dataInicio) >= trunc(:hoje)								 " +
                    "				order by pp.profissional.nome											 ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSub", idSub);
            query.setParameter("hoje", new Date());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubEspecialidadesMedicoPrestadorExterno(Long idMedico, Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						" +
                    "			join pe.subespecialidade sub 														" +
                    "			join fetch sub.especialidade esp													" +
                    "			where pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	" +
                    "			and pe.plantao.profissional.id = :idMedico											" +
                    "			and trunc(pe.plantao.dataInicio) >= trunc(:hoje)									" +
                    "			order by esp.nome																	";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idMedico", idMedico);
            query.setParameter("hoje", new Date());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPorFiltros(Date dataInicio, Date dataFim, Long idLocal, Long idProfissional, Long marcacao, List<TipoAtendimentoEnum> tipoAtendimento, Long sala, Long status, Long residente, Long idProfissionalResponsavel, Long especialidade, List<Long> subespecialidade, List<String> horariosExtras, List<Long> grupos) throws DAOException {
        try {

            List<Long> listaStatus = new ArrayList<>();
            String jpql = "select distinct pp FROM " + this.clazz.getSimpleName() + " pp left join pp.plantaoRealizado pr join pp.especialidades esp " +
                    " left join pp.plantaoRealizado.plantaoAprovado pa"
                    //+ (grupos != null && !grupos.isEmpty() ? " left join fetch pp.grupos grupo": "")
                    + " where pp.localAtendimento.id = :idLocal and pp.status.id in (:status) and "
                    + " ( pp.localAtendimento.apontamentoManual = 1 and pr is null or "
                    + "   pp.localAtendimento.apontamentoManual = 0 and pr is not null and pa.situacaoIntegracaoLegado = 'A' or "
                    + "   pp.localAtendimento.apontamentoManual = 0 and pr is null and pa.situacaoIntegracaoLegado = 'A' ) ";

            if (idProfissional != null) {
                jpql += " and pp.profissional.id = :idProfissional ";
            }

            if (marcacao == null) {
                jpql += " and pp.tipoMarcacao.id is null ";
            }
            if (marcacao != null && marcacao > 0) {
                jpql += " and pp.tipoMarcacao.id = :marcacao ";
            }

            if (tipoAtendimento != null) {
                jpql += " and pp.tipoAtendimento.id in(:tipoAtendimento) ";
            }

            if (sala != null) {
                jpql += " and pp.sala = :sala ";
            }

            if (residente != null) {
                if (residente == 1) {
                    jpql += " and pp.profissionalResponsavel.id is not null ";
                } else {
                    jpql += " and pp.profissionalResponsavel.id is null ";
                }
            }

            if (idProfissionalResponsavel != null) {
                jpql += " and pp.profissionalResponsavel.id = :idProfissionalResponsavel ";
            }

            if (especialidade != null) {
                jpql += " and esp.subespecialidade.especialidade.id = :idEspecialidade ";
            }

            if (subespecialidade != null && subespecialidade.size() > 0 && subespecialidade.get(0) != null) {
                jpql += " and esp.subespecialidade.id in (:idSubEspecialidade) ";
            }

            if (horariosExtras != null && (horariosExtras.size() > 0 && horariosExtras.get(0) != null)) {
                for (int i = 0; i < horariosExtras.size(); i++) {
                    jpql += " and (select count(pphe) from " + PlantaoPlanejadoHoraExtra.class.getSimpleName() + " pphe where pphe.id.plantaoPlanejado.id = pp.id and to_char(pphe.id.horarioExtra,'HH24:mi')= '" + horariosExtras.get(i) + "' ) > 0 ";
                }
            }

            if (grupos != null && !grupos.isEmpty()) {
                for (Long grupo : grupos) {
                    jpql += " and (select count(grupo) from " + PlantaoPlanejadoGrupoMarcacao.class.getSimpleName() + " grupo where grupo.id.plantaoPlanejado.id = pp.id and grupo.id.grupoMarcacao.id = " + grupo + ") > 0 ";
                }
            }

            jpql += " and (pp.dataInicio between :dataInicio and :dataFim or pp.dataFim between :dataInicio and :dataFim)";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("idLocal", idLocal);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);

            if (especialidade != null) {
                query.setParameter("idEspecialidade", especialidade);
            }

            if (subespecialidade != null && subespecialidade.size() > 0 && subespecialidade.get(0) != null) {
                query.setParameter("idSubEspecialidade", subespecialidade);
            }

            if (idProfissional != null) {
                query.setParameter("idProfissional", idProfissional);
            }

            if (marcacao != null && marcacao > 0) {
                query.setParameter("marcacao", marcacao);
            }

            if (sala != null) {
                query.setParameter("sala", sala);
            }

            if (status != null) {
                listaStatus.add(status);
            } else {
                List<StatusPlantaoEnum> enumList = StatusPlantaoEnum.getVisiveis();
                for (StatusPlantaoEnum item : enumList) {
                    listaStatus.add(item.getCodigo());
                }
            }

            query.setParameter("status", listaStatus);

            if (tipoAtendimento != null) {
                List<Long> tipoAtendimentoIds = new ArrayList<>();
                for (TipoAtendimentoEnum tp : tipoAtendimento) {
                    tipoAtendimentoIds.add(tp.getCodigo());
                }
                query.setParameter("tipoAtendimento", tipoAtendimentoIds);
            }

            if (idProfissionalResponsavel != null) {
                query.setParameter("idProfissionalResponsavel", idProfissionalResponsavel);
            }

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<LocalAtendimentoPlantao> buscarUnidadesComPlantoesDoProfissional(Long idProfissional) throws DAOException {

        try {
            String jpql = "select distinct pp.localAtendimento FROM " + this.clazz.getSimpleName() + " pp where pp.profissional.id = :idProfissional";

            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("idProfissional", idProfissional);

            return query.getResultList();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<EventoCalendarioVO> buscarNativePorPeriodoProfissional(Date dataInicio, Date dataFim, List<Long> idsProfissionais, List<Long> idsUnidades) throws DAOException {

        try {
            String queryString =
                    "SELECT"
                            + "    pp.ID,"
                            + "    TO_CHAR(pp.NOME),"
                            + "    pp.DT_HR_INICIO,"
                            + "    pp.DT_HR_FIM,"
                            + "    TO_CHAR(pp.NOME_COR),"
                            + "    TO_CHAR(PF.NOME),"
                            + "    pp.ID_TIPO_MARCACAO,"
                            + "    pp.ID_TIPO_ATENDIMENTO,"
                            + "    PP.ID_LOCAL_ATENDIMENTO_PLANTAO," +
                            "      case when PR.ID_SITUACAO_PLANTAO_REALIZADO = :statusAusente then 1 else 0 end, " +
                            "      PA.ID as id_aprovado, PA.DT_HR_INICIO as hora_aprov"
                            + " FROM"
                            + "    PLANTAO.PLANTAO_PLANEJADO pp"
                            + " LEFT JOIN PLANTAO.PLANTAO_REALIZADO PR on pp.id = PR.id "
                            + " LEFT JOIN PLANTAO.PLANTAO_APROVACAO PA on PA.ID = PP.ID "
                            + " LEFT JOIN"
                            + "    PLANTAO.TIPO_PLANTAO_LOCAL_ATENDIMENTO tp"
                            + " ON"
                            + "    pp.ID_TIPO_PLANTAO_LOCAL_ATDM = tp.ID"
                            + " INNER JOIN"
                            + "    CORPORATIVO.PESSOA_FISICA PF"
                            + " ON"
                            + "    PP.ID_PROFISSIONAL = PF.ID"
                            + " WHERE"
                            + "    PP.ID_PROFISSIONAL IN (:idsProfissionais)"
                            + " AND PP.ID_LOCAL_ATENDIMENTO_PLANTAO IN (:idsUnidades)"
                            + " AND ("
                            + "        pp.DT_HR_INICIO BETWEEN :dataInicio AND :dataFim"
                            + "    OR  pp.DT_HR_FIM BETWEEN :dataInicio AND :dataFim"
                            + "    OR  ("
                            + "            pp.DT_HR_INICIO < :dataInicio"
                            + "        AND :dataFim < pp.DT_HR_FIM ))";
            Query nativeQuery = em.createNativeQuery(queryString);

            nativeQuery.setParameter("dataInicio", dataInicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("dataFim", dataFim, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("idsProfissionais", idsProfissionais);
            nativeQuery.setParameter("idsUnidades", idsUnidades);
            nativeQuery.setParameter("statusAusente", SituacaoPlantaoEnum.AUSENTE.getCodigo());

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<EventoCalendarioVO> retorno = new ArrayList<>();
            DateTime agora = new DateTime();

            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    EventoCalendarioVO evento = new EventoCalendarioVO();

                    BigDecimal idPlantao = (BigDecimal) o[0];
                    String nomePlantao = (String) o[1];
                    Date inicio = (Date) o[2];
                    Date fim = (Date) o[3];
                    String corPlantao = (String) o[4];
                    String nomeProfissional = (String) o[5];
                    BigDecimal idTipoMarcacao = (BigDecimal) o[6];
                    BigDecimal tipoAtendimento = (BigDecimal) o[7];
                    BigDecimal idUnidade = (BigDecimal) o[8];

                    evento.setId(idPlantao.longValue());

                    if (idsProfissionais.size() > 1) {
                        evento.setTitle(nomeProfissional);
                    }

                    evento.setStartLong(inicio.getTime());
                    evento.setEndLong(fim.getTime());
                    evento.setColor("grey");
                    evento.setNomeProfissional(nomeProfissional);
                    evento.setIdTipoMarcacao(idTipoMarcacao != null ? idTipoMarcacao.longValue() : null);
                    evento.setTipoAtendimentoIcone((tipoAtendimento == null ? "" : TipoAtendimentoEnum.get(tipoAtendimento.longValue()).getIcone()));
                    evento.setIdLocalAtendimento(idUnidade.longValue());

                    if ((o[10] != null && o[11] == null) || (o[10] == null && o[9].toString().equals("1"))) {
                        evento.setAusente(true);
                        evento.setColor("white");
                        evento.setTextColor("black");
                    }

                    evento.setEditable(false);

                    retorno.add(evento);
                }
            }
            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Long> buscarSubsEspecialidadesPorProficional(Long idProfissional, Date data) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 			" +
                    "			join pe.subespecialidade sub 											" +
                    "			where pe.plantao.profissional.id = :idProfissional						" +
                    "			and :data between pe.plantao.dataInicio and pe.plantao.dataFim			";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("data", data);

            List<SubEspecialidade> especialidades = query.getResultList();

            List<Long> ids = new ArrayList<>();
            for (SubEspecialidade especialidade : especialidades) {
                ids.add(especialidade.getId());
            }

            return ids;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPlanejadoPorIds(List<Long> ids) throws DAOException {
        try {
            String jpql = "FROM " + this.clazz.getSimpleName() + " pp where pp.id in(:ids)";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, this.clazz);

            query.setParameter("ids", ids);

            List<PlantaoPlanejado> plantoes = query.getResultList();

            return plantoes;
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubEspecialidadesPorLocal(Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						" +
                    "			where pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	" +
                    "			and pe.plantao.tipoMarcacao.id <> :atendimentoVirtual								" +
                    "           and pe.plantao.status.id = :status                                                  ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPorLocal(Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pp.profissional from PlantaoPlanejado pp 						        " +
                    "				where pp.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento        " +
                    "				and pp.tipoMarcacao.id <> :atendimentoVirtual							        " +
                    "               and pp.status.id = :status                                                      ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> buscarMedicosPorSubEspecialidadeEUnidade(Long idSubEspecialidade, Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.plantao.profissional from PlantaoEspecialidade pe 					                " +
                    "				where pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento				" +
                    "				and pe.plantao.tipoMarcacao.id <> :atendimentoVirtual							                " +
                    "				and pe.subespecialidade.id = :idSubEspecialidade								                " +
                    "               and pe.plantao.status.id = :status                                                              ";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<SubEspecialidade> buscarSubEspecialidadesPorProfissionalELocal(Long idProfissional, Long idUnidadeCredenciamento) throws DAOException {
        try {
            String jpql = " select distinct pe.subespecialidade from PlantaoEspecialidade pe 						" +
                    "           join pe.plantao p                                                                   " +
                    "			where pe.plantao.localAtendimento.localAtendimento.id = :idUnidadeCredenciamento	" +
                    "           and p.tipoMarcacao.id <> :atendimentoVirtual                                        " +
                    "           and p.profissional.id = :idProfissional                                             " +
                    "           and p.status.id = :status                                                           ";

            TypedQuery<SubEspecialidade> query = this.em.createQuery(jpql, SubEspecialidade.class);
            query.setParameter("idUnidadeCredenciamento", idUnidadeCredenciamento);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<SubEspecialidade> especialidades = query.getResultList();
            return especialidades;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> listaPlantoesEntrePeriodos(Date dtMinima, Date dtMaxima) throws DAOException {
        try {
            String jpql = "select pp from PlantaoPlanejado pp where pp.dataInicio BETWEEN :dtMinima AND :dtMaxima";
            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("dtMinima", dtMinima);
            query.setParameter("dtMaxima", dtMaxima);
            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> listarPlantoesFuturosPorIdLocalAtdm(long idTipoPlantao) throws DAOException {
        try {
            String jpql = "select pp from PlantaoPlanejado pp where pp.tipoPlantao.id = :idTipoPlantao and pp.dataInicio > sysdate";
            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("idTipoPlantao", idTipoPlantao);
            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public Date buscarDataInicioPorId(Long idPlantao) throws DAOException {
        try {
            String jpql = "select pp.dataInicio from PlantaoPlanejado pp where pp.id = :idPlantao";
            TypedQuery<Date> query = this.em.createQuery(jpql, Date.class);
            query.setParameter("idPlantao", idPlantao);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public Date buscarDataFimPorId(Long idPlantao) throws DAOException {
        try {
            String jpql = "select pp.dataFim from PlantaoPlanejado pp where pp.id = :idPlantao";
            TypedQuery<Date> query = this.em.createQuery(jpql, Date.class);
            query.setParameter("idPlantao", idPlantao);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public Map<Profissional, List<SubEspecialidade>> getProfissionaisPlantaoAgendamento(Date dataInicio, Date dataFim) throws DAOException {
        try {
            String jpql = " select distinct pl.profissional, sub from PlantaoPlanejado pl 					        " +
                    "				join pl.especialidades e						                                " +
                    "				join e.subespecialidade sub						                                " +
                    "				left join pl.grupos grupo						                                " +
                    "				where pl.tipoMarcacao.id = :atendimentoAgendamento						        " +
                    "               and   pl.status.id = :status                                                    " +
                    "               and   grupo is null                                                             " +
                    "			    and (																			" +
                    "				(pl.dataInicio between :dataInicio and :dataFim) or 					        " +
                    "				(pl.dataFim between :dataInicio and :dataFim) or 						        " +
                    "				(pl.dataInicio <= :dataInicio and pl.dataFim >= :dataFim)		                " +
                    "			)																					";

            TypedQuery<Object[]> query = this.em.createQuery(jpql, Object[].class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("atendimentoAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<Object[]> retorno = query.getResultList();
            Map<Profissional, List<SubEspecialidade>> map = new HashMap<>();
            for (Object[] profEsp : retorno) {
                Profissional p = (Profissional) profEsp[0];
                p.getConselhos().size(); // fech
                SubEspecialidade subEsp = (SubEspecialidade) profEsp[1];

                List<SubEspecialidade> espDoMedico = map.get(p);
                if (espDoMedico == null) {
                    List<SubEspecialidade> listaMedico = new ArrayList<>();
                    listaMedico.add(subEsp);
                    map.put(p, listaMedico);
                } else {
                    espDoMedico.add(subEsp);
                }

            }
            return map;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<Profissional> getProfissionaisPlantaoAgendamentoPorSubEspecialidade(Date dataInicio, Date dataFim, Long idSubEspecialidade) throws DAOException {
        try {
            String jpql = " select distinct pl.profissional from PlantaoEspecialidade pe 					        " +
                    "               inner join pe.plantao pl                                                        " +
                    "               join fetch pl.profissional.conselhos c                                          " +
                    "				where pl.tipoMarcacao.id = :atendimentoAgendamento						        " +
                    "               and   pl.status.id = :status                                                    " +
                    "               and pe.subespecialidade.id = :idSubEspecialidade                                " +
                    "			    and (																			" +
                    "				(pl.dataInicio between :dataInicio and :dataFim) or 					        " +
                    "				(pl.dataFim between :dataInicio and :dataFim) or 						        " +
                    "				(pl.dataInicio <= :dataInicio and pl.dataFim >= :dataFim)		                " +
                    "			)																					";

            TypedQuery<Profissional> query = this.em.createQuery(jpql, Profissional.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("atendimentoAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<Profissional> profissionais = query.getResultList();
            return profissionais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<LocalAtendimento> getUnidadesPlantaoAgendamentoPorSubEspecialidade(Date dataInicio, Date dataFim, Long idSubEspecialidade) throws DAOException {
        try {
            String jpql = " select distinct localCred from PlantaoEspecialidade pe 			                        " +
                    "               inner join pe.plantao pp                                                        " +
                    "               inner join pp.localAtendimento local                                            " +
                    "               inner join local.localAtendimento localCred                                     " +
                    "				where pp.tipoMarcacao.id = :atendimentoAgendamento						        " +
                    "               and   pp.status.id = :status                                                    " +
                    "               and pe.subespecialidade.id = :idSubEspecialidade                                " +
                    "			    and (																			" +
                    "				(pp.dataInicio between :dataInicio and :dataFim) or 					        " +
                    "				(pp.dataFim between :dataInicio and :dataFim) or 						        " +
                    "				(pp.dataInicio <= :dataInicio and pp.dataFim >= :dataFim)		                " +
                    "			)																					";

            TypedQuery<LocalAtendimento> query = this.em.createQuery(jpql, LocalAtendimento.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("atendimentoAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<LocalAtendimento> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException("Erro ao buscar Unidade de um plantão de agendamento por subEspecialidade:[" + idSubEspecialidade + "]. \n", e);
        }
    }

    public List<LocalAtendimento> getUnidadesPlantaoAgendamentoPorSubEspecialidadeProfissional(Date dataInicio, Date dataFim, Long idSubEspecialidade, Long idProfissional) throws DAOException {
        try {
            String jpql = " select distinct localCred from PlantaoEspecialidade pe 			            " +
                    "               inner join pe.plantao pp                                            " +
                    "               inner join pp.localAtendimento local                                " +
                    "               inner join local.localAtendimento localCred                         " +
                    "				where pp.tipoMarcacao.id = :atendimentoAgendamento					" +
                    "               and pp.status.id = :status                                          " +
                    "               and pe.subespecialidade.id = :idSubEspecialidade                    " +
                    "               and pp.profissional.id = :idProfissional                            " +
                    "			    and (																" +
                    "				(pp.dataInicio between :dataInicio and :dataFim) or 				" +
                    "				(pp.dataFim between :dataInicio and :dataFim) or 					" +
                    "				(pp.dataInicio <= :dataInicio and pp.dataFim >= :dataFim)		    " +
                    "			)																		";

            TypedQuery<LocalAtendimento> query = this.em.createQuery(jpql, LocalAtendimento.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("atendimentoAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<LocalAtendimento> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException("Erro ao buscar Unidade de um plantão de agendamento por subEspecialidade:[" + idSubEspecialidade + "] e profissional[" + idProfissional + "]. \n", e);
        }
    }

    public List<PlantaoPlanejado> buscarPlantoesPorIds(List<Long> idsPlantao) throws DAOException {
        try {
            String jpql = " select distinct pp from PlantaoPlanejado pp      		                            " +
                    "           left join fetch pp.horasExtras                                                  " +
                    "				where pp.id in (:idsPlantao)	                    				        ";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("idsPlantao", idsPlantao);

            List<PlantaoPlanejado> plantoes = query.getResultList();
            return plantoes;
        } catch (Exception e) {
            throw new DAOException("Erro ao buscar Plantoes por ids:[" + idsPlantao.toString() + "]. \n", e);
        }

    }

    public List<PlantaoPlanejado> buscarPlantoesAgendamentoPorProfissionalEEspecialidade(Date dataInicio, Date dataFim, Long idProfissional, Long idSubEspecialidade) throws DAOException {
        try {
            String jpql = " select distinct pp from PlantaoEspecialidade pe 			                " +
                    "               inner join pe.plantao pp                                            " +
                    "               left join pp.grupos grupo                                    " +
                    "               inner join fetch pp.especialidades ppe                              " +
                    "               inner join fetch ppe.subespecialidade sub                           " +
                    "               inner join fetch pp.profissional prof                               " +
                    "               inner join fetch pp.localAtendimento local                          " +
                    "               inner join fetch local.localAtendimento localCred                   " +
                    "				where pp.tipoMarcacao.id = :atendimentoAgendamento					" +
                    "               and   pp.status.id = :status                                        " +
                    "               and pe.subespecialidade.id = :idSubEspecialidade                    " +
                    "               and pp.profissional.id = :idProfissional                            " +
                    "               and grupo is null                                                    " +
                    "			    and (																" +
                    "				(pp.dataInicio between :dataInicio and :dataFim) or 				" +
                    "				(pp.dataFim between :dataInicio and :dataFim) or 					" +
                    "				(pp.dataInicio <= :dataInicio and pp.dataFim >= :dataFim)		    " +
                    "			)																		";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("atendimentoAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<PlantaoPlanejado> locais = query.getResultList();
            locais.forEach(local -> local.getPausas().size());
            //fetch horas extras
            locais.forEach(local -> local.getHorasExtras().size());

            return locais;
        } catch (Exception e) {
            throw new DAOException("Erro ao buscar Plantoes planejados de agendamento por subEspecialidade:[" + idSubEspecialidade + "] e profissional[" + idProfissional + "]. \n", e);
        }

    }

    public List<PlantaoPlanejado> buscarPlantoesAgendamentoPorLocalEEspecialidade(Date dataInicio, Date dataFim, Long idLocal, Long idSubEspecialidade) throws DAOException {
        try {
            String jpql = " select distinct pp from PlantaoEspecialidade pe 			                " +
                    "               inner join pe.plantao pp                                            " +
                    "               left join pp.grupos grupo                                    " +
                    "               inner join fetch pp.profissional prof                               " +
                    "               inner join fetch prof.conselhos cons                                " +
                    "               inner join fetch pp.localAtendimento local                          " +
                    "               inner join fetch local.localAtendimento localCred                   " +
                    "				where pp.tipoMarcacao.id = :atendimentoAgendamento					" +
                    "               and   pp.status.id = :status                                        " +
                    "               and pe.subespecialidade.id = :idSubEspecialidade                    " +
                    "               and localCred.id = :idLocal                                         " +
                    "               and grupo is null                                                    " +
                    "			    and (																" +
                    "				(pp.dataInicio between :dataInicio and :dataFim) or 				" +
                    "				(pp.dataFim between :dataInicio and :dataFim) or 					" +
                    "				(pp.dataInicio <= :dataInicio and pp.dataFim >= :dataFim)		    " +
                    "			)																		";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);
            query.setParameter("idLocal", idLocal);
            query.setParameter("atendimentoAgendamento", TipoMarcacaoEnum.AGENDAMENTO.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<PlantaoPlanejado> locais = query.getResultList();
            locais.forEach(local -> local.getPausas().size());
            //fetch horas extras
            locais.forEach(local -> local.getHorasExtras().size());

            return locais;
        } catch (Exception e) {
            throw new DAOException("Erro ao buscar Plantoes planejados de agendamento por subEspecialidade:[" + idSubEspecialidade + "] e Local[" + idLocal + "]. \n", e);
        }
    }

    public List<LocalAtendimentoPlantao> buscarLocaisAtendimentosPorEspecialidade(Date dataInicio, Date dataFim, Long idSubEspecialidade, Long idMedico, String sexo, List<Long> idsGrupos) throws DAOException {
        try {
            String jpql = " select distinct lp from PlantaoEspecialidade pe 					    " +
                    "			inner join pe.plantao pl	                                        " +
                    "           left join pl.grupos grupo                                    " +
                    "			inner join pl.localAtendimento lp 									" +
                    "			inner join fetch lp.localAtendimento l 								" +
                    "			inner join pe.subespecialidade sub                                  " +
                    "			inner join pl.profissional prof                                     " +
                    "			where pl.dataInicio >= :dataInicio									" +
                    "			and pl.dataFim <= :dataFim											" +
                    "			and (grupo is null " + (idsGrupos == null || idsGrupos.isEmpty() ? "" : " or grupo.id.grupoMarcacao.id in :idsGrupos") + ")" +
                    "			and sub.id = :idSubEspecialidade								    " +
                    "   and pl.tipoMarcacao.id <> :atendimentoVirtual                               " +
                    "   and pl.status.id = :status                                                  ";


            if (idMedico != null) {
                jpql += " and prof.id = :idMedico";
            }

            if (sexo != null) {
                jpql += " and prof.sexo = :sexo";
            }


            TypedQuery<LocalAtendimentoPlantao> query = this.em.createQuery(jpql, LocalAtendimentoPlantao.class);
            query.setParameter("dataInicio", dataInicio);
            query.setParameter("dataFim", dataFim);
            query.setParameter("idSubEspecialidade", idSubEspecialidade);

            if (idMedico != null) {
                query.setParameter("idMedico", idMedico);
            }

            if (idsGrupos != null && !idsGrupos.isEmpty()) {
                query.setParameter("idsGrupos", idsGrupos);
            }

            if(sexo != null){
                query.setParameter("sexo", sexo.toUpperCase());
            }

            query.setParameter("atendimentoVirtual", TipoMarcacaoEnum.VIRTUAL.getId());
            query.setParameter("status", StatusPlantaoEnum.ATIVO.getCodigo());

            List<LocalAtendimentoPlantao> locais = query.getResultList();
            return locais;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<ResumoPlanejadoVO> buscarResumoPlanejadoUnidade(Long idLocal, Date inicio, Date fim) throws DAOException {

        try {
            String queryString =
                    " SELECT"
                            + "     TAB.ID_EMPRESA,"
                            + "     TAB.CNPJ_EMPRESA,"
                            + "     TAB.APELIDO_EMPRESA,"
                            + "     TAB.ID_PROFISSIONAL,"
                            + "     TAB.NOME,"
                            + "     TAB.CONSELHO,"
                            + "     SUM(TAB.VALOR_PAGAMENTO),"
                            + "     SUM(TAB.HORAS_PROGRAMADAS)"
                            + " FROM"
                            + "     ("
                            + "         SELECT"
                            + "             ("
                            + "                 SELECT"
                            + "                     VIG.ID_PRESTADOR"
                            + "                 FROM"
                            + "                     PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "                 INNER JOIN"
                            + "                     CORPORATIVO.PESSOA_JURIDICA PJ"
                            + "                 ON"
                            + "                     PJ.ID = VIG.ID_PRESTADOR"
                            + "                 WHERE"
                            + "                     VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "                 AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "                 AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS ID_EMPRESA,"
                            + "             ("
                            + "                 SELECT"
                            + "                     PJ.NUMERO_CNPJ"
                            + "                 FROM"
                            + "                     PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "                 INNER JOIN"
                            + "                     CORPORATIVO.PESSOA_JURIDICA PJ"
                            + "                 ON"
                            + "                     PJ.ID = VIG.ID_PRESTADOR"
                            + "                 WHERE"
                            + "                     VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "                 AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "                 AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS CNPJ_EMPRESA"
                            + "             ,"
                            + "             PP.ID_PROFISSIONAL,"
                            + "             PF.NOME,"
                            + "             ("
                            + "                 SELECT"
                            + "                     PREST.NOME_APELIDO_PRESTADOR_SERVICO"
                            + "                 FROM"
                            + "                     PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "                 INNER JOIN"
                            + "                     CREDENCIAMENTO.PRESTADOR_SERVICO_SAUDE PREST"
                            + "                 ON"
                            + "                     PREST.ID = VIG.ID_PRESTADOR"
                            + "                 WHERE"
                            + "                     VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "                 AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "                 AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS"
                            + "                                                                                    APELIDO_EMPRESA,"
                            + "             PF.NOME                                                            AS NOME_PROFISSIONAL,"
                            + "             CONS.SIGLA_CONSELHO_REGL_NOME_MERC || ': ' || CONS.NUMERO_DOCUMENTO AS CONSELHO,"
                            + "             ("
                            + "                 SELECT"
                            + "                     MAX("
                            + "                         CASE"
                            + "                             WHEN VIG.IN_TIPO_CALCULO_HR = 1"
                            + "                             THEN (VIG.VL_PAGAMENTO * (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24) - ("
                            + "                                 ("
                            + "                                     SELECT"
                            + "                                         COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)"
                            + "                                     FROM"
                            + "                                         PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP"
                            + "                                     WHERE"
                            + "                                         PPP.ID_PLANTAO_PLANEJADO = PP.ID ) * VIG.VL_PAGAMENTO)"
                            + "                             WHEN VIG.IN_TIPO_CALCULO_HR = 0"
                            + "                             THEN VIG.VL_PAGAMENTO"
                            + "                         END)"
                            + "                 FROM"
                            + "                     PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "                 WHERE"
                            + "                     VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "                 AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "                 AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS"
                            + "                                                      VALOR_PAGAMENTO,"
                            + "             (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24 AS HORAS_PROGRAMADAS"
                            + "         FROM"
                            + "             PLANTAO.PLANTAO_PLANEJADO PP"
                            + "         INNER JOIN"
                            + "             CORPORATIVO.PESSOA_FISICA PF"
                            + "         ON"
                            + "             PP.ID_PROFISSIONAL = PF.ID"
                            + "         INNER JOIN"
                            + "             PLANTAO.LOCAL_ATENDIMENTO_PLANTAO LAP"
                            + "         ON"
                            + "             PP.id_local_atendimento_plantao = LAP.id"
                            + "         INNER JOIN"
                            + "             CREDENCIAMENTO.LOCAL_ATENDIMENTO LA"
                            + "         ON"
                            + "             LAP.id_local_atendimento = la.id"
                            + "         INNER JOIN"
                            + "             CREDENCIAMENTO.PROFISSIONAL PROF"
                            + "         ON"
                            + "             PP.ID_PROFISSIONAL = PROF.ID"
                            + "         INNER JOIN"
                            + "             CREDENCIAMENTO.PROFISSIONAL_DCTO_CNSLH_REGL CONS"
                            + "         ON"
                            + "             PROF.ID = CONS.ID_PROFISSIONAL"
                            + "         WHERE"
                            + "             PP.DT_HR_INICIO >= :inicio"
                            + "         AND PP.DT_HR_INICIO <= :fim"
                            + "         AND PP.ID_LOCAL_ATENDIMENTO_PLANTAO = :idLocal ) TAB"
                            + " GROUP BY"
                            + "     TAB.ID_EMPRESA,"
                            + "     TAB.CNPJ_EMPRESA,"
                            + "     TAB.APELIDO_EMPRESA,"
                            + "     TAB.ID_PROFISSIONAL,"
                            + "     TAB.NOME,"
                            + "     TAB.CONSELHO"
                            + " ORDER BY"
                            + "     TAB.APELIDO_EMPRESA ASC,"
                            + "     TAB.NOME ASC";

            Query nativeQuery = em.createNativeQuery(queryString);
            nativeQuery.setParameter("idLocal", idLocal);
            nativeQuery.setParameter("inicio", inicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("fim", fim, TemporalType.TIMESTAMP);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<ResumoPlanejadoVO> retorno = new ArrayList<>();

            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    ResumoPlanejadoVO unidade = new ResumoPlanejadoVO();

                    BigDecimal idEmpresa = (BigDecimal) o[0];

                    if (idEmpresa == null) {
                        continue;
                    }

                    String cnpjEmpresa = (String) o[1];
                    String apelidoEmpresa = (String) o[2];
                    BigDecimal idProfissional = (BigDecimal) o[3];
                    String nomeProfissional = (String) o[4];
                    String conselho = (String) o[5];

                    BigDecimal valorLocal = (BigDecimal) o[6];
                    BigDecimal horasProgramadas = (BigDecimal) o[7];
                    unidade.setIdEmpresa(idEmpresa.longValue());
                    unidade.setCnpjEmpresa(MaskUtils.formatarString(cnpjEmpresa, "##.###.###/####-##"));
                    unidade.setApelidoEmpresa(apelidoEmpresa);
                    unidade.setIdProfissional(idProfissional.longValue());
                    unidade.setNomeProfissional(nomeProfissional);
                    unidade.setConselho(conselho);
                    unidade.setValorPlanejado(valorLocal == null ? 0f : valorLocal.floatValue());

                    Long iPart = (long) horasProgramadas.doubleValue();
                    Double fPart = horasProgramadas.doubleValue() - iPart;
                    unidade.setHorasProgramadasDescricao(iPart + " hora(s)" + (fPart != 0 ? " e " + Math.round(fPart * 60) + " minuto(s)" : ""));

                    retorno.add(unidade);
                }
            }
            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public PlantaoPlanejado findPlantaoComLocalEProfissional(Long idPlantaoPlanejado) throws DAOException {
        try {
            String jpql = " select pp from PlantaoPlanejado pp 		    	                            " +
                    "               inner join fetch pp.profissional prof                               " +
                    "               inner join fetch prof.conselhos cons                                " +
                    "               inner join fetch pp.localAtendimento local                          " +
                    "               inner join fetch local.localAtendimento localCred                   " +
                    "				where pp.id = :idPlantaoPlanejado			                		";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("idPlantaoPlanejado", idPlantaoPlanejado);
            query.setMaxResults(1);

            PlantaoPlanejado plantao = query.getSingleResult();

            return plantao;
        } catch (Exception e) {
            throw new DAOException("Erro ao buscar Plantão planejado por ID:[" + idPlantaoPlanejado + "]. \n", e);
        }
    }

    public List<ResumoPlanejadoVO> buscarResumoPlanejadoGeral(boolean locaisPrevent, Date inicio, Date fim) throws DAOException {

        try {
            String queryString =
                    " SELECT"
                            + "     PP.ID_LOCAL_ATENDIMENTO_PLANTAO,"
                            + "     LA.NUMERO_DOCUMENTO_IDTF_PROPT AS CNPJ_LOCAL,"
                            + "     LA.APELIDO || CASE WHEN DIV.NOME IS NOT NULL THEN ' - (' || DIV.NOME || ')' END AS LOCAL_ATENDIMENTO,"
                            + "     SUM("
                            + "          ("
                            + "          SELECT"
                            + "              MAX("
                            + "                  CASE"
                            + "                      WHEN VIG.IN_TIPO_CALCULO_HR = 1"
                            + "                      THEN (VIG.VL_PAGAMENTO * (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24) - ("
                            + "                          ("
                            + "                              SELECT"
                            + "                                  COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)"
                            + "                              FROM"
                            + "                                  PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP"
                            + "                              WHERE"
                            + "                                  PPP.ID_PLANTAO_PLANEJADO = PP.ID ) * VIG.VL_PAGAMENTO)"
                            + "                      WHEN VIG.IN_TIPO_CALCULO_HR = 0"
                            + "                      THEN VIG.VL_PAGAMENTO"
                            + "                  END)"
                            + "          FROM"
                            + "              PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "          WHERE"
                            + "              VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "          AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "          AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM)) AS VALOR_PAGAMENTO"
                            + " FROM"
                            + "     PLANTAO.PLANTAO_PLANEJADO PP"
                            + " INNER JOIN"
                            + "     CORPORATIVO.PESSOA_FISICA PF"
                            + " ON"
                            + "     PP.ID_PROFISSIONAL = PF.ID"
                            + " INNER JOIN"
                            + "     PLANTAO.LOCAL_ATENDIMENTO_PLANTAO LAP"
                            + " ON"
                            + "     PP.id_local_atendimento_plantao = LAP.id"
                            + " INNER JOIN"
                            + "     CREDENCIAMENTO.LOCAL_ATENDIMENTO LA"
                            + " ON"
                            + "     LAP.id_local_atendimento = la.id"
                            + " LEFT JOIN"
                            + "     CORPORATIVO.DIVISAO_OPERACIONAL DIV"
                            + " ON"
                            + "   LAP.ID_DIVISAO_OPERACIONAL = DIV.ID"
                            + " WHERE"
                            + "     PP.DT_HR_INICIO >= :inicio"
                            + " AND PP.DT_HR_INICIO <= :fim"
                            + " AND LAP.ID_TIPO_PAGAMENTO_UNIDADE = :tipoPagamento"
                            + " GROUP BY"
                            + "     PP.ID_LOCAL_ATENDIMENTO_PLANTAO,"
                            + "     LA.NUMERO_DOCUMENTO_IDTF_PROPT,"
                            + "     LA.APELIDO,"
                            + "     DIV.NOME"
                            + " ORDER BY"
                            + "     LA.APELIDO ASC";

            Query nativeQuery = em.createNativeQuery(queryString);
            nativeQuery.setParameter("inicio", inicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("fim", fim, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("tipoPagamento", TipoPagamentoUnidadeEnum.ANTECIPACAO_DE_PAGAMENTO.getCodigo());

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<ResumoPlanejadoVO> retorno = new ArrayList<>();

            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    ResumoPlanejadoVO unidade = new ResumoPlanejadoVO();

                    BigDecimal idLocal = (BigDecimal) o[0];
                    String cnpjLocal = (String) o[1];
                    String nomeLocal = (String) o[2];
                    BigDecimal valorLocal = (BigDecimal) o[3];
                    unidade.setIdLocalAtendimento(idLocal.longValue());
                    unidade.setCnpjLocalAtendimento(MaskUtils.formatarString(cnpjLocal, "##.###.###/####-##"));
                    unidade.setNomeLocalAtendimento(nomeLocal);
                    unidade.setValorPlanejado(valorLocal == null ? 0f : valorLocal.floatValue());

                    retorno.add(unidade);
                }
            }
            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<ResumoPlanejadoVO> buscarResumoDetalhadoPorProfissional(Long idLocal, Long idEmpresa, Long idProfissional, Date inicio, Date fim) throws DAOException {

        try {
            String queryString =
                    " SELECT * FROM (SELECT"
                            + "     ("
                            + "         SELECT"
                            + "             VIG.ID_PRESTADOR"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS ID_EMPRESA,"
                            + "     PP.NOME                                                                 AS NOME_PLANTAO,"
                            + "     TO_CHAR(PP.DT_HR_INICIO,'dd/mm/yyyy HH24:MI:ss')                       AS ENTRADA_PLANEJADA,"
                            + "     TO_CHAR(PP.DT_HR_FIM,'dd/mm/yyyy HH24:MI:ss')                          AS SAIDA_PLANEJADA,"
                            + "     ("
                            + "         SELECT"
                            + "             MAX("
                            + "                 CASE"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 1"
                            + "                     THEN (VIG.VL_PAGAMENTO * (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24) - ("
                            + "                         ("
                            + "                             SELECT"
                            + "                                 COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)"
                            + "                             FROM"
                            + "                                 PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP"
                            + "                             WHERE"
                            + "                                 PPP.ID_PLANTAO_PLANEJADO = PP.ID ) * VIG.VL_PAGAMENTO)"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 0"
                            + "                     THEN VIG.VL_PAGAMENTO"
                            + "                 END)"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS VALOR_PAGAMENTO,"
                            + "     (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24                                   AS HORAS_PROGRAMADAS,"
                            + "     ("
                            + "     ("
                            + "         SELECT"
                            + "             COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)"
                            + "         FROM"
                            + "             PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP"
                            + "         WHERE"
                            + "             PPP.ID_PLANTAO_PLANEJADO = PP.ID )) AS PAUSAS,"
                            + "     ("
                            + "         SELECT"
                            + "             MAX("
                            + "                 CASE"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 1"
                            + "                     THEN 'HORAS'"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 0"
                            + "                     THEN 'VALOR FECHADO'"
                            + "                 END)"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS TIPO_PAGAMENTO,"

                            + "     TO_CHAR(PP.DT_CRIACAO,'dd/mm/yyyy')                                                                 AS DT_CRIACAO"

                            + " FROM"
                            + "     PLANTAO.PLANTAO_PLANEJADO PP"
                            + " INNER JOIN"
                            + "     CORPORATIVO.PESSOA_FISICA PF"
                            + " ON"
                            + "     PP.ID_PROFISSIONAL = PF.ID"
                            + " INNER JOIN"
                            + "     PLANTAO.LOCAL_ATENDIMENTO_PLANTAO LAP"
                            + " ON"
                            + "     PP.id_local_atendimento_plantao = LAP.id"
                            + " INNER JOIN"
                            + "     CREDENCIAMENTO.LOCAL_ATENDIMENTO LA"
                            + " ON"
                            + "     LAP.id_local_atendimento = la.id"
                            + " WHERE"
                            + "     PP.DT_HR_INICIO >= :inicio"
                            + " AND PP.DT_HR_INICIO <= :fim"
                            + " AND PP.ID_PROFISSIONAL = :idProfissional"
                            + " AND PP.ID_LOCAL_ATENDIMENTO_PLANTAO = :idLocal"
                            + " ORDER BY PP.DT_HR_INICIO ASC"
                            + " ) TAB WHERE TAB.ID_EMPRESA = :idEmpresa";

            Query nativeQuery = em.createNativeQuery(queryString);
            nativeQuery.setParameter("idLocal", idLocal);
            nativeQuery.setParameter("idProfissional", idProfissional);
            nativeQuery.setParameter("idEmpresa", idEmpresa);
            nativeQuery.setParameter("inicio", inicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("fim", fim, TemporalType.TIMESTAMP);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<ResumoPlanejadoVO> retorno = new ArrayList<>();

            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    ResumoPlanejadoVO unidade = new ResumoPlanejadoVO();

                    unidade.setNomePlantao((String) o[1]);
                    unidade.setEntradaProgramada((String) o[2]);
                    unidade.setSaidaProgramada((String) o[3]);

                    BigDecimal valorLocal = (BigDecimal) o[4];

                    unidade.setValorPlanejado(valorLocal == null ? 0f : valorLocal.floatValue());

                    BigDecimal horasProgramadas = (BigDecimal) o[5];
                    Long iPart = (long) horasProgramadas.doubleValue();
                    Double fPart = horasProgramadas.doubleValue() - iPart;
                    unidade.setHorasProgramadas(horasProgramadas.doubleValue());
                    unidade.setHorasProgramadasDescricao(iPart + " hora(s)" + (fPart != 0 ? " e " + Math.round(fPart * 60) + " minuto(s)" : ""));

                    BigDecimal pausasProgramadas = (BigDecimal) o[6];
                    Long iPart2 = (long) pausasProgramadas.doubleValue();
                    Double fPart2 = pausasProgramadas.doubleValue() - iPart2;
                    unidade.setPausasProgramadas(pausasProgramadas.doubleValue());
                    unidade.setPausasProgramadasDescricao(iPart2 + " hora(s)" + (fPart2 != 0 ? " e " + Math.round(fPart2 * 60) + " minuto(s)" : ""));

                    unidade.setTipoPagamento((String) o[7]);


                    unidade.setDataCriacao((String) o[8]);
                    retorno.add(unidade);
                }
            }
            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<ResumoPlanejadoVO> buscarResumoDetalhadoPlanejadoPorProfissionalTipoPlantao(Long idLocal, Long idTipoPlantao, Long idProfissional, Date inicio, Date fim) throws DAOException {

        try {
            String queryString =
                    " SELECT * FROM (SELECT"
                            + "     ("
                            + "         SELECT"
                            + "             vig.id_tipo_plantao_local_atdm"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS ID_TIPO_PLANTAO,"
                            + "     PP.NOME                                                                 AS NOME_PLANTAO,"
                            + "     TO_CHAR(PP.DT_HR_INICIO,'dd/mm/yyyy HH24:MI:ss')                       AS ENTRADA_PLANEJADA,"
                            + "     TO_CHAR(PP.DT_HR_FIM,'dd/mm/yyyy HH24:MI:ss')                          AS SAIDA_PLANEJADA,"
                            + "     ("
                            + "         SELECT"
                            + "             MAX("
                            + "                 CASE"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 1"
                            + "                     THEN (VIG.VL_PAGAMENTO * (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24) - ("
                            + "                         ("
                            + "                             SELECT"
                            + "                                 COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)"
                            + "                             FROM"
                            + "                                 PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP"
                            + "                             WHERE"
                            + "                                 PPP.ID_PLANTAO_PLANEJADO = PP.ID ) * VIG.VL_PAGAMENTO)"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 0"
                            + "                     THEN VIG.VL_PAGAMENTO"
                            + "                 END)"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS VALOR_PAGAMENTO,"
                            + "     (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24                                   AS HORAS_PROGRAMADAS,"
                            + "     ("
                            + "     ("
                            + "         SELECT"
                            + "             COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)"
                            + "         FROM"
                            + "             PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP"
                            + "         WHERE"
                            + "             PPP.ID_PLANTAO_PLANEJADO = PP.ID )) AS PAUSAS,"
                            + "     ("
                            + "         SELECT"
                            + "             MAX("
                            + "                 CASE"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 1"
                            + "                     THEN 'HORAS'"
                            + "                     WHEN VIG.IN_TIPO_CALCULO_HR = 0"
                            + "                     THEN 'VALOR FECHADO'"
                            + "                 END)"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS TIPO_PAGAMENTO,"
                            + "     ("
                            + "         SELECT"
                            + "             VIG.VL_PAGAMENTO"
                            + "         FROM"
                            + "             PROFISSIONAL_TIPO_PLANTAO_VIG VIG"
                            + "         WHERE"
                            + "             VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL"
                            + "         AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM"
                            + "         AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS VL_PAGAMENTO"
                            + " FROM"
                            + "     PLANTAO.PLANTAO_PLANEJADO PP"
                            + " INNER JOIN"
                            + "     CORPORATIVO.PESSOA_FISICA PF"
                            + " ON"
                            + "     PP.ID_PROFISSIONAL = PF.ID"
                            + " INNER JOIN"
                            + "     PLANTAO.LOCAL_ATENDIMENTO_PLANTAO LAP"
                            + " ON"
                            + "     PP.id_local_atendimento_plantao = LAP.id"
                            + " INNER JOIN"
                            + "     CREDENCIAMENTO.LOCAL_ATENDIMENTO LA"
                            + " ON"
                            + "     LAP.id_local_atendimento = la.id"
                            + " WHERE"
                            + "     PP.DT_HR_INICIO >= :inicio"
                            + " AND PP.DT_HR_INICIO <= :fim"
                            + " AND PP.ID_PROFISSIONAL = :idProfissional"
                            + " AND PP.ID_LOCAL_ATENDIMENTO_PLANTAO = :idLocal"
                            + " ORDER BY PP.DT_HR_INICIO ASC"
                            + " ) TAB WHERE TAB.ID_TIPO_PLANTAO = :idTipoPlantao";

            Query nativeQuery = em.createNativeQuery(queryString);
            nativeQuery.setParameter("idLocal", idLocal);
            nativeQuery.setParameter("idProfissional", idProfissional);
            nativeQuery.setParameter("idTipoPlantao", idTipoPlantao);
            nativeQuery.setParameter("inicio", inicio, TemporalType.TIMESTAMP);
            nativeQuery.setParameter("fim", fim, TemporalType.TIMESTAMP);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = nativeQuery.getResultList();

            List<ResumoPlanejadoVO> retorno = new ArrayList<>();

            if (!resultados.isEmpty()) {
                for (Object[] o : resultados) {
                    ResumoPlanejadoVO unidade = new ResumoPlanejadoVO();

                    unidade.setNomePlantao((String) o[1]);
                    unidade.setEntradaProgramada((String) o[2]);
                    unidade.setSaidaProgramada((String) o[3]);

                    BigDecimal valorLocal = (BigDecimal) o[4];

                    unidade.setValorPlanejado(valorLocal == null ? 0f : valorLocal.floatValue());

                    BigDecimal horasProgramadas = (BigDecimal) o[5];
                    Long iPart = (long) horasProgramadas.doubleValue();
                    Double fPart = horasProgramadas.doubleValue() - iPart;
                    unidade.setHorasProgramadas(horasProgramadas.doubleValue());
                    unidade.setHorasProgramadasDescricao(iPart + " hora(s)" + (fPart != 0 ? " e " + Math.round(fPart * 60) + " minuto(s)" : ""));

                    BigDecimal pausasProgramadas = (BigDecimal) o[6];
                    Long iPart2 = (long) pausasProgramadas.doubleValue();
                    Double fPart2 = pausasProgramadas.doubleValue() - iPart2;
                    unidade.setPausasProgramadas(pausasProgramadas.doubleValue());
                    unidade.setPausasProgramadasDescricao(iPart2 + " hora(s)" + (fPart2 != 0 ? " e " + Math.round(fPart2 * 60) + " minuto(s)" : ""));

                    unidade.setTipoPagamento((String) o[7]);

                    unidade.setValorPagamento(((BigDecimal) o[8]).floatValue());

                    retorno.add(unidade);
                }
            }
            return retorno;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscarPlantoesPorDataLocalECategoria(Date dtInicio, Date dtFim, String cnpjLocal, CategoriaEnum categoria) throws DAOException {
        try {
            String jpql = " select pp from PlantaoPlanejado pp			 			                	" +
                    "		join pp.categorias cat														" +
                    "       join fetch  pp.profissional prof                                            " +
                    "		where pp.dataInicio between :dtInicio and :dtFim							" +
                    " 		and pp.localAtendimento.localAtendimento.documento = :cnpjLocal             " +
                    "       and cat.id = :idCategoria 							            			";

            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("dtInicio", dtInicio);
            query.setParameter("dtFim", dtFim);
            query.setParameter("cnpjLocal", cnpjLocal);
            query.setParameter("idCategoria", categoria.getCodigo());

            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<ChaveValorVO> buscarMedicosResponsaveisDoResidente(String cpfResidente, String cnpjLocal) throws DAOException {
        try {
            String jpql = " select distinct  new " + ChaveValorVO.class.getName() + "(pp.profissionalResponsavel.cpf, pp.profissionalResponsavel.nome) from PlantaoPlanejado pp " +
                    "where pp.dataInicio between :inicioPeriodo and :fimPeriodo " +
                    "and pp.localAtendimento.localAtendimento.documento = :cnpjLocal " +
                    "and pp.status.id <> :status " +
                    "and pp.profissional.cpf = :cpfResidente " +
                    "and pp.profissionalResponsavel is not null";

            DateTime agora = new DateTime();

            TypedQuery<ChaveValorVO> query = this.em.createQuery(jpql, ChaveValorVO.class);
            query.setParameter("cpfResidente", cpfResidente.replaceAll("[\\D]", ""));
            query.setParameter("cnpjLocal", cnpjLocal.replaceAll("[\\D]", ""));
            query.setParameter("inicioPeriodo", agora.minusHours(12).toDate());
            query.setParameter("fimPeriodo", agora.plusHours(12).toDate());
            query.setParameter("status", StatusPlantaoEnum.EXCLUIDO.getCodigo());

            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public Long quantidadePlantoesPorUnidadeTipoData(Long idProfissional, Long idLocal, Long idTipoPlantao, Date dtInicio, Date dtFim, Date competencia) throws DAOException {
        try {
            String jpql = " select count(pp) from PlantaoPlanejado pp " +
                    " where pp.localAtendimento.id = :idLocal 		  " +
                    " and pp.profissional.id = :idProfissional 		  " +
                    " and pp.tipoPlantao.id = :idTipoPlantao		  " +
                    " and pp.dataInicio >= :competencia 			  " +
                    " and pp.dataInicio between :dtInicio and :dtFim ";

            TypedQuery<Long> query = this.em.createQuery(jpql, Long.class);
            query.setParameter("idLocal", idLocal);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("idTipoPlantao", idTipoPlantao);
            query.setParameter("dtInicio", dtInicio);
            query.setParameter("dtFim", dtFim);
            query.setParameter("competencia", competencia);

            return query.getSingleResult();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public Long verificarQuantidadeDePlantoesDoProfissionalTipoPeriodo(Long idTipoPlantao, Long idProfissional, Long idLocal, List<Interval> intervalos) throws DAOException {
        try {

            StringBuilder sb = new StringBuilder();
            sb.append(" select count(pp.id) from " + this.clazz.getSimpleName() + " pp " +
                    " where pp.tipoPlantao.id = :idTipoPlantao " +
                    " and pp.profissional.id = :idProfissional " +
                    " and pp.localAtendimento.id = :idLocal "
            );
            if (intervalos != null && !intervalos.isEmpty()) {
                sb.append("and (");
                for (int i = 0; i < intervalos.size(); i++) {
                    sb.append((i > 0 ? " or " : ""));
                    sb.append(" pp.dataInicio between :dtInicio" + i + " and :dtFim" + i);
                }
                sb.append(")");
            }

            TypedQuery<Long> query = this.em.createQuery(sb.toString(), Long.class);
            query.setParameter("idTipoPlantao", idTipoPlantao);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("idLocal", idLocal);

            if (intervalos != null && !intervalos.isEmpty()) {
                for (int i = 0; i < intervalos.size(); i++) {

                    query.setParameter("dtInicio" + i, intervalos.get(i).getStart().toDate(), TemporalType.TIMESTAMP);
                    query.setParameter("dtFim" + i, intervalos.get(i).getEnd().toDate(), TemporalType.TIMESTAMP);

                }
            }

            return query.getSingleResult();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<PlantaoPlanejado> buscaPlantoesPlanejadosPorDatasProfissionalLocal(Long idTipoPlantao, Long idProfissional, Date dtInicio, Date dtFim) throws DAOException {
        try {
            String jpql = " select pp from " + this.clazz.getSimpleName() + " pp " +
                    " where pp.tipoPlantao.id = :idTipoPlantao " +
                    " and pp.profissional.id = :idProfissional " +
                    " and pp.dataInicio between :dtInicio and :dtFim ";
            TypedQuery<PlantaoPlanejado> query = this.em.createQuery(jpql, PlantaoPlanejado.class);
            query.setParameter("idTipoPlantao", idTipoPlantao);
            query.setParameter("idProfissional", idProfissional);
            query.setParameter("dtInicio", dtInicio);
            query.setParameter("dtFim", dtFim);
            return query.getResultList();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<ProfissionalValidaVigenciaEValidaFacilVO> buscaProfissionaSemVigencia(Date iniciCompetencia, Date fimCompetencia, List<Long> idsProfissionais) throws DAOException {

        try {
            String stringQuery = "SELECT *                                                                                "
                    +"FROM                                                                                                "
                    +"    (                                                                                               "
                    +"        SELECT                                                                                      "
                    +"            PP.ID,                                                                                  "
                    +"            PP.DT_HR_INICIO AS PP_INICIO,                                                           "
                    +"            PP.DT_HR_FIM    AS PP_FIM,                                                              "
                    +"            PP.ID_PROFISSIONAL,                                                                     "
                    +"            PP.ID_TIPO_PLANTAO_LOCAL_ATDM,                                                          "
                    +"            PP.ID_TIPO_ATENDIMENTO,                                                                 "
                    +"            PP.DT_HR_INICIO AS COMPETENCIA,                                                         "
                    +"            PP.ID_LOCAL_ATENDIMENTO_PLANTAO,                                                        "
                    +"            LA.APELIDO,                                                                             "
                    +"            PF.NOME,                                                                                "
                    +"            PP.NOME       AS NOME_PLANTAO,                                                          "
                    +"            PP.ID_USUARIO AS ID_USUARIO_PLANEJADOR,                                                 "
                    +"            TPLA.NOME AS TIPO_PLANTAO,                                                              "
                    +"            CEIL(                                                                                   "
                    +"            (                                                                                       "
                    +"                SELECT                                                                              "
                    +"                    MAX(                                                                            "
                    +"                        CASE                                                                        "
                    +"                            WHEN VIG.IN_TIPO_CALCULO_HR = 1                                         "
                    +"                            THEN (VIG.VL_PAGAMENTO * (PP.DT_HR_FIM - PP.DT_HR_INICIO) * 24) - (     "
                    +"                                (                                                                   "
                    +"                                    SELECT                                                          "
                    +"                                        COALESCE(SUM (PPP.DT_HR_FIM - PPP.DT_HR_INICIO) * 24,0)     "
                    +"                                    FROM                                                            "
                    +"                                        PLANTAO.PAUSA_PLANTAO_PLANEJADO PPP                         "
                    +"                                    WHERE                                                           "
                    +"                                        PPP.ID_PLANTAO_PLANEJADO = PP.ID ) * VIG.VL_PAGAMENTO)      "
                    +"                            WHEN VIG.IN_TIPO_CALCULO_HR = 0                                         "
                    +"                            THEN VIG.VL_PAGAMENTO                                                   "
                    +"                        END)                                                                        "
                    +"                FROM                                                                                "
                    +"                    PLANTAO.PROFISSIONAL_TIPO_PLANTAO_VIG VIG                                       "
                    +"                WHERE                                                                               "
                    +"                    VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL                                        "
                    +"                AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM                            "
                    +"                AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM)) AS             "
                    +"            VALOR_PLANEJADO,                                                                        "
                    +"            (                                                                                       "
                    +"                SELECT                                                                              "
                    +"                    TO_CHAR(PJ.NUMERO_CNPJ)                                                         "
                    +"                FROM                                                                                "
                    +"                    PLANTAO.PROFISSIONAL_TIPO_PLANTAO_VIG VIG                                       "
                    +"                INNER JOIN                                                                          "
                    +"                    CORPORATIVO.PESSOA_JURIDICA PJ                                                  "
                    +"                ON                                                                                  "
                    +"                    PJ.ID = VIG.ID_PRESTADOR                                                        "
                    +"                WHERE                                                                               "
                    +"                    VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL                                        "
                    +"                AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM                            "
                    +"                AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS CNPJ_EMPRESA "
                    +"            ,                                                                                       "
                    +"            PA.DT_HR_INICIO AS PA_INICIO,                                                           "
                    +"            PA.DT_HR_FIM    AS PA_FIM,                                                              "
                    +"            PA.VL_APROVADO,                                                                         "
                    +"            PA.ID_USUARIO_LIBERACAO                                                                 "
                    +"        FROM                                                                                        "
                    +"            PLANTAO.PLANTAO_PLANEJADO PP                                                            "
                    +"        INNER JOIN                                                                                  "
                    +"            CORPORATIVO.PESSOA_FISICA PF                                                            "
                    +"        ON                                                                                          "
                    +"            PP.ID_PROFISSIONAL = PF.ID                                                              "
                    +"        INNER JOIN                                                                                  "
                    +"            PLANTAO.LOCAL_ATENDIMENTO_PLANTAO LAP                                                   "
                    +"        ON                                                                                          "
                    +"            PP.id_local_atendimento_plantao = LAP.id                                                "
                    +"        AND LAP.ID_TIPO_PAGAMENTO_UNIDADE = 2                                                       "
                    +"        INNER JOIN                                                                                  "
                    +"            CREDENCIAMENTO.LOCAL_ATENDIMENTO LA                                                     "
                    +"        ON                                                                                          "
                    +"            LAP.id_local_atendimento = la.id                                                        "
                    +"        INNER JOIN                                                                                  "
                    +"             PLANTAO.TIPO_PLANTAO_LOCAL_ATENDIMENTO TPLA                                            "
                    +"         ON                                                                                         "
                    +"             PP.ID_TIPO_PLANTAO_LOCAL_ATDM = TPLA.ID                                                "
                    +"        LEFT JOIN                                                                                   "
                    +"            PLANTAO.PLANTAO_APROVACAO PA                                                            "
                    +"        ON                                                                                          "
                    +"            PP.ID = PA.ID                                                                           "
                    +"        AND PA.ID_SITUACAO_INTEGRACAO_LEGADO = 'A'                                                  "
                    +"                                                                                                    "
                    +"        WHERE                                                                                       "
                    +"            PP.DT_HR_INICIO >= :iniciCompetencia                                                    "
                    +"        AND PP.DT_HR_INICIO <= :fimCompetencia   ";

                    if(idsProfissionais != null && !idsProfissionais.isEmpty()){
                        stringQuery+="AND ID_PROFISSIONAL IN (:profissionais) ";
                    }

                    stringQuery+="        ORDER BY                                                                                    "
                    +"            PP.DT_HR_INICIO) TAB                                                                    "
                    +"WHERE                                                                                               "
                    +"    TAB.VALOR_PLANEJADO IS NULL                                                                     ";


            Query nativeQuery = em.createNativeQuery(stringQuery);
            nativeQuery.setParameter("iniciCompetencia",iniciCompetencia,TemporalType.TIMESTAMP);
            nativeQuery.setParameter("fimCompetencia",fimCompetencia,TemporalType.TIMESTAMP);
            if(idsProfissionais != null && !idsProfissionais.isEmpty()){
                nativeQuery.setParameter("profissionais",idsProfissionais);
            }


            List<ProfissionalValidaVigenciaEValidaFacilVO> profissionaisSemVigencia = new ArrayList<>();
            List<Object[]> resultados = nativeQuery.getResultList();


            if(!resultados.isEmpty()){
                for (Object[] o:resultados) {
                    Long id = o[0] != null ? new Long(o[0].toString()) : null;
                    Date dataInicioPlanejamento = o[1]!= null ? (Date) o[1] : null;
                    Date dataFimPlanejamento = o[2]!= null ? (Date) o[2] : null;
                    Long idProfissional = o[3] != null ? new Long(o[3].toString()) : null;
                    Long idTipoLocalPlantaoAtendimento = o[4] != null ? new Long(o[4].toString()) : null;
                    Long idTipoAtendimento= o[5] != null ? new Long(o[5].toString()) : null;
                    Date competencia = o[6]!= null ? (Date) o[6] : null;
                    Long idLocalAtendimentoPlantao = o[7] != null ? new Long(o[7].toString()) : null;
                    String apelidoUnidade =  o[8] != null ? o[8].toString() : null;
                    String nomeMedico = o[9] != null ? o[9].toString() : null;
                    String nomePlantao = o[10] != null ? o[10].toString() : null;
                    Long idUsuarioPlanejador = o[11] != null ? new Long(o[11].toString()) : null;
                    String tipoPlantao = o[12] != null ? o[12].toString() : null;
                    BigDecimal valorPlanejado = o[13] != null ? new BigDecimal(o[13].toString()) : null;
                    String cnpjEmpresa = o[14] != null ? o[14].toString() : null;
                    Date plantaoAtendimentoInicio = o[15]!= null ? (Date) o[15] : null;
                    Date plantaoAtendimentoFim = o[16]!= null ? (Date) o[16] : null;
                    BigDecimal valorAprovado = o[17] != null ? new BigDecimal(o[17].toString()) : null;
                    Long idUsuarioLiberacao = o[18] != null ? new Long(o[18].toString()) : null;

                    ProfissionalValidaVigenciaEValidaFacilVO profissional = new ProfissionalValidaVigenciaEValidaFacilVO();

                    profissional.setId(id);
                    profissional.setDataInicioPlanejamento(dataInicioPlanejamento);
                    profissional.setDataFimPlanejamento(dataFimPlanejamento);
                    profissional.setIdProfissional(idProfissional);
                    profissional.setIdTipoLocalPlantaoAtendimento(idTipoLocalPlantaoAtendimento);
                    profissional.setIdTipoAtendimento(idTipoAtendimento);
                    profissional.setCompetencia(competencia);
                    profissional.setIdLocalAtendimentoPlantao(idLocalAtendimentoPlantao);
                    profissional.setApelidoUnidade(apelidoUnidade);
                    profissional.setNomeMedico(nomeMedico);
                    profissional.setNomePlantao(nomePlantao);
                    profissional.setIdUsuarioPlanejador(idUsuarioPlanejador);
                    profissional.setTipoPlantao(tipoPlantao);
                    profissional.setValorPlanejado(valorPlanejado);
                    profissional.setCnpjEmpresa(cnpjEmpresa);
                    profissional.setPlantaoAtendimentoInicio(plantaoAtendimentoInicio);
                    profissional.setPlantaoAtendimentoFim(plantaoAtendimentoFim);
                    profissional.setValorAprovado(valorAprovado);
                    profissional.setIdUsuarioLiberacao(idUsuarioLiberacao);
                    profissionaisSemVigencia.add(profissional);
                }
            }
            return profissionaisSemVigencia;

        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    public List<ProfissionalValidaVigenciaEValidaFacilVO> buscaProfissionaisPagamento(Date iniciCompetencia, Date fimCompetencia, List<Long> idsProfissionais) throws DAOException {

        try {
            String stringQuery = "SELECT                                                                                                                                                    "
                    +"    DISTINCT PP.ID_PROFISSIONAL, regexp_replace(LPAD(TO_CHAR(PF.NUMERO_CPF), 11, '0'), '([0-9]{3})([0-9]{3})([0-9]{3})([0-9]{2})','\\1.\\2.\\3-\\4') AS CPF_PROFISSIONAL, "
                    +"    PF.NOME,                                                                                                                                                          "
                    +"    (                                                                                                                                                                 "
                    +"        SELECT                                                                                                                                                        "
                    +"            REGEXP_REPLACE(LPAD(TO_CHAR(PJ.NUMERO_CNPJ), 14, '0'),                                                                                                    "
                    +"            '([0-9]{2})([0-9]{3})([0-9]{3})([0-9]{4})([0-9]{2})' ,'\\1.\\2.\\3/\\4-\\5')                                                                              "
                    +"        FROM                                                                                                                                                          "
                    +"            PROFISSIONAL_TIPO_PLANTAO_VIG VIG                                                                                                                         "
                    +"        INNER JOIN                                                                                                                                                    "
                    +"            CORPORATIVO.PESSOA_JURIDICA PJ                                                                                                                            "
                    +"        ON                                                                                                                                                            "
                    +"            PJ.ID = VIG.ID_PRESTADOR                                                                                                                                  "
                    +"        WHERE                                                                                                                                                         "
                    +"            VIG.ID_PROFISSIONAL = PP.ID_PROFISSIONAL                                                                                                                  "
                    +"        AND PP.DT_HR_INICIO BETWEEN VIG.DT_INICIO AND VIG.DT_FIM                                                                                                      "
                    +"        AND PP.ID_TIPO_PLANTAO_LOCAL_ATDM = VIG.ID_TIPO_PLANTAO_LOCAL_ATDM) AS CNPJ_EMPRESA                                                                           "
                    +"FROM                                                                                                                                                                  "
                    +"    PLANTAO.PLANTAO_PLANEJADO PP                                                                                                                                      "
                    +"INNER JOIN                                                                                                                                                            "
                    +"    CORPORATIVO.PESSOA_FISICA PF                                                                                                                                      "
                    +"ON                                                                                                                                                                    "
                    +"    PP.ID_PROFISSIONAL = PF.ID                                                                                                                                        "
                    +"INNER JOIN                                                                                                                                                            "
                    +"    PLANTAO.LOCAL_ATENDIMENTO_PLANTAO LAP                                                                                                                             "
                    +"ON                                                                                                                                                                    "
                    +"    PP.id_local_atendimento_plantao = LAP.id                                                                                                                          "
                    +"AND LAP.ID_TIPO_PAGAMENTO_UNIDADE = 2                                                                                                                                 "
                    +"INNER JOIN                                                                                                                                                            "
                    +"    CREDENCIAMENTO.LOCAL_ATENDIMENTO LA                                                                                                                               "
                    +"ON                                                                                                                                                                    "
                    +"    LAP.id_local_atendimento = la.id                                                                                                                                  "
                    +"LEFT JOIN                                                                                                                                                             "
                    +"    PLANTAO.PLANTAO_APROVACAO PA                                                                                                                                      "
                    +"ON                                                                                                                                                                    "
                    +"    PP.ID = PA.ID                                                                                                                                                     "
                    +"AND PA.ID_SITUACAO_INTEGRACAO_LEGADO = 'A'                                                                                                                            "
                    +"WHERE                                                                                                                                                                 "
                    +"    PP.DT_HR_INICIO >= :iniciCompetencia                                                                                                                              "
                    +"AND PP.DT_HR_INICIO <= :fimCompetencia    ";
                    if(idsProfissionais != null && !idsProfissionais.isEmpty()){
                        stringQuery += " AND PP.ID_PROFISSIONAL IN (:profissionais)";
                    }
                    stringQuery+=" ORDER BY                                                                                                                                                 "
                    +"PP.ID_PROFISSIONAL ASC";

            Query nativeQuery = em.createNativeQuery(stringQuery);
            nativeQuery.setParameter("iniciCompetencia",iniciCompetencia,TemporalType.TIMESTAMP);
            nativeQuery.setParameter("fimCompetencia",fimCompetencia,TemporalType.TIMESTAMP);
            if(idsProfissionais != null && !idsProfissionais.isEmpty()){
                nativeQuery.setParameter("profissionais",idsProfissionais);
            }


            List<ProfissionalValidaVigenciaEValidaFacilVO> profissionaisEmpresas = new ArrayList<>();
            List<Object[]> resultados = nativeQuery.getResultList();

            if(!resultados.isEmpty()){
                for (Object[] o:resultados){
                    ProfissionalValidaVigenciaEValidaFacilVO profissional = new ProfissionalValidaVigenciaEValidaFacilVO();
                    Long id = o[0] != null ? new Long(o[0].toString()) : null;
                    String cpf = o[1] != null ? o[1].toString() : null;
                    String nome = o[2] != null ? o[2].toString() : null;
                    String cnpj = o[3] != null ? o[3].toString() : null;


                    profissional.setId(id);
                    profissional.setCpf(cpf);
                    profissional.setNomeMedico(nome);
                    profissional.setCnpjEmpresa(cnpj);
                    profissionaisEmpresas.add(profissional);
                }
            }
            return profissionaisEmpresas;
        } catch (Exception e) {
            throw new DAOException(e);
        }


    }

}
